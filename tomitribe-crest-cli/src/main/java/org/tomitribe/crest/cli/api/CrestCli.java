/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.cli.api;

import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.completer.Completer;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.cli.api.interceptor.interactive.InteractiveMissingParameters;
import org.tomitribe.crest.cli.impl.CliEnv;
import org.tomitribe.crest.cli.impl.CommandParser;
import org.tomitribe.crest.cli.impl.command.Streams;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.util.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CrestCli {
    private static volatile Runnable exitHook;

    @Command
    public static void exit() {
        if (exitHook != null) {
            exitHook.run();
        }
        Environment.ENVIRONMENT_THREAD_LOCAL.remove();
        throw new ExitException();
    }

    @Command
    public static void history(final CliEnvironment env, @Out final PrintStream out) {
        for (final History.Entry entry : env.history()) {
            out.println(String.format("[%3d] %s", entry.index() + 1, entry.value()));
        }
    }

    @Command
    public static void clear(final CliEnvironment env) {
        env.reader().clear();
    }

    // using all defaults
    public static void main(final String[] args) throws Exception {
        new CrestCli().run(args);
    }

    private CliEnvironment mainEnvironment;

    public void run(final String... args) throws Exception {
        final AtomicReference<InputReader> inputReaderRef = new AtomicReference<InputReader>();
        final AtomicReference<History> historyRef = new AtomicReference<History>();
        mainEnvironment = createMainEnvironment(inputReaderRef, historyRef);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(mainEnvironment);

        final DefaultsContext ctx = new SystemPropertiesDefaultsContext();
        final Main main = newMain(ctx);

        final Map<String, String> aliasesMapping = new HashMap<String, String>();
        final File aliases = aliasesFile();
        if (aliases != null && aliases.isFile()) {
            aliasesMapping.putAll(Map.class.cast(IO.readProperties(aliases)));
        }

        final InputReader readerFacade;
        final History history;
        if (args == null || args.length == 0) {
            final ConsoleReader reader = new ConsoleReader(mainEnvironment.getInput(), mainEnvironment.getOutput());
            reader.setHandleUserInterrupt(true);

            final File historyFile = cliHistoryFile();
            history = historyFile != null && historyFile.isFile() ? new FileHistory(historyFile) : new MemoryHistory();
            reader.setHistory(history);

            reader.addCompleter(new Completer() {
                @Override
                public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
                    candidates.addAll(main.complete(buffer, cursor));
                    return buffer.lastIndexOf(' ', cursor) + 1; // TODO: enhance it
                }
            });

            readerFacade = new InputReader() {
                @Override
                public String readLine(final String prompt) throws IOException {
                    return reader.readLine(prompt);
                }

                @Override
                public String readPassword(final String prompt) throws IOException {
                    return reader.readLine(prompt, '*');
                }

                @Override
                public void clear() {
                    try {
                        reader.clearScreen();
                    } catch (final IOException e) {
                        throw new IllegalStateException(e);
                    }
                }

                @Override
                public void close() throws Exception {
                    // no-op
                }
            };
        } else {
            history = null;
            readerFacade = new FileInputReader(args["-f".equals(args[0]) ? 1 : 0]);
        }
        inputReaderRef.set(readerFacade);
        historyRef.set(history);

        final int nThreads = Integer.getInteger("crest.cli.pipping.threads", 16);
        final ExecutorService es = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
            private final ThreadGroup group;
            private final AtomicInteger threadNumber = new AtomicInteger();

            {
                final SecurityManager s = System.getSecurityManager();
                group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            }

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(group, r, "cli-" + threadNumber.incrementAndGet(), 0);
                if (t.isDaemon()) {
                    t.setDaemon(false);
                }
                if (t.getPriority() != Thread.NORM_PRIORITY) {
                    t.setPriority(Thread.NORM_PRIORITY);
                }
                return t;
            }
        });

        exitHook = new Runnable() {
            @Override
            public void run() {
                beforeExit();
                if (FileHistory.class.isInstance(history)) {
                    try {
                        FileHistory.class.cast(history).flush();
                    } catch (final IOException e) {
                        // no-op
                    }
                }
                try {
                    readerFacade.close();
                } catch (final Exception e) {
                    // no-op
                }
                es.shutdownNow();
            }
        };

        final CommandParser parser = new CommandParser();
        try {
            String line;
            boolean quit;
            do {
                quit = true;
                try {
                    while ((line = readerFacade.readLine(nextPrompt())) != null) {
                        if (line.trim().isEmpty() || line.startsWith("#")) {
                            continue;
                        }

                        final String actualCmd = aliasesMapping.get(line.trim());
                        if (actualCmd != null) {
                            line = actualCmd;
                        }

                        line = transformCommand(line);

                        final long start;
                        if (line.startsWith("time ")) {
                            start = System.nanoTime();
                            line = line.substring("time ".length());
                        } else {
                            start = -1;
                        }

                        try {
                            final CommandParser.Command[] commands = parser.toArgs(line);
                            if (commands.length == 1) {
                                try {
                                    main.main(mainEnvironment, commands[0].getArgs());
                                } catch (final Exception error) {
                                    if (ExitException.class.isInstance(error.getCause())) {
                                        break;
                                    }
                                    error.printStackTrace(mainEnvironment.getError());
                                    throw error;
                                }
                            } else { // should move to a common module
                                if (nThreads < commands.length) {
                                    throw new IllegalArgumentException("We dont support more than " + nThreads + " pipping commands, use -Dcrest.cli.pipping.threads to update it");
                                }

                                // execute tasks piping them
                                final InputStream[] ins = new InputStream[commands.length];
                                final OutputStream[] outs = new OutputStream[commands.length];
                                for (int i = 0; i < commands.length; i++) { // allocate
                                    ins[i] = i == 0 ? mainEnvironment.getInput() : new PipedInputStream();
                                    outs[i] = i == commands.length - 1 ? mainEnvironment.getOutput() : new PipedOutputStream();
                                }
                                for (int i = 0; i < commands.length; i++) { // wire
                                    if (PipedInputStream.class.isInstance(ins[i])) {
                                        PipedInputStream.class.cast(ins[i]).connect(PipedOutputStream.class.cast(outs[i - 1]));
                                    }
                                }

                                final Collection<Future<?>> tasks = new ArrayList<Future<?>>(commands.length);

                                for (int i = 0; i < commands.length; i++) {
                                    final int idx = i;
                                    final PrintStream out = new PrintStream(outs[idx]);
                                    tasks.add(es.submit(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                main.main(pipeEnvironment(mainEnvironment, ins[idx], out), commands[idx].getArgs());
                                            } catch (final Exception error) {
                                                error.printStackTrace(mainEnvironment.getError());
                                            } finally {
                                                if (PipedInputStream.class.isInstance(ins[idx])) {
                                                    try {
                                                        ins[idx].close();
                                                    } catch (final IOException e) {
                                                        // no-op
                                                    }
                                                }
                                                try {
                                                    if (PipedOutputStream.class.isInstance(outs[idx])) {
                                                        outs[idx].close();
                                                    } else {
                                                        outs[idx].flush();
                                                    }
                                                } catch (final IOException e) {
                                                    // no-op
                                                }
                                            }
                                        }
                                    }));
                                }

                                // wait end of the global command
                                for (final Future<?> t : tasks) {
                                    try {
                                        t.get();
                                    } catch (final InterruptedException e) {
                                        Thread.interrupted();
                                    } catch (final ExecutionException e) {
                                        // no-op
                                    }
                                }
                                mainEnvironment.getOutput().flush();
                            }
                        } catch (final Exception error) {
                            error.printStackTrace(mainEnvironment.getError());
                            mainEnvironment.getError().flush();
                        } finally {
                            if (start > 0) {
                                final long end = System.nanoTime();
                                final long sec = TimeUnit.NANOSECONDS.toSeconds(end - start);
                                final long msec = TimeUnit.NANOSECONDS.toMillis((end - start) - TimeUnit.SECONDS.toNanos(sec));
                                mainEnvironment.getOutput().println("Time " + sec + "s " + msec + "ms");
                            }
                        }
                    }
                } catch (final UserInterruptException uie) {
                    quit = false;
                }
            } while (!quit);
        } finally {
            if (exitHook != null) {
                exitHook.run();
                exitHook = null;
            }
        }
    }

    protected String transformCommand(final String line) {
        return line;
    }

    protected Main newMain(final DefaultsContext ctx) {
        final Main main = new Main();
        main.processClass(ctx, Streams.class);
        main.processClass(ctx, InteractiveMissingParameters.class);
        main.processClass(ctx, CrestCli.class);
        onMainCreated(ctx, main);
        return main;
    }

    protected void onMainCreated(final DefaultsContext ctx, final Main main) {
        // no-op
    }

    protected File aliasesFile() {
        return null;
    }

    protected File cliHistoryFile() {
        return null;
    }

    protected String nextPrompt() {
        return System.getProperty("user.name") + "@" + System.getProperty("java.version") + " $ ";
    }

    protected void beforeExit() {
        // no-op
    }

    protected CliEnvironment pipeEnvironment(final CliEnvironment env, final InputStream in, final PrintStream out) {
        return new CliEnvironment() {
            @Override
            public History history() {
                return env.history();
            }

            @Override
            public InputReader reader() {
                return env.reader();
            }

            @Override
            public Map<String, ?> userData() {
                return env.userData();
            }

            @Override
            public InputStream getInput() {
                return in;
            }

            @Override
            public PrintStream getOutput() {
                return out;
            }

            @Override
            public PrintStream getError() {
                return env.getError();
            }

            @Override
            public Properties getProperties() {
                return env.getProperties();
            }

            @Override
            public <T> T findService(final Class<T> type) {
                return env.findService(type);
            }
        };
    }

    // java 8 would have use Supplier which is cleaner
    protected CliEnvironment createMainEnvironment(final AtomicReference<InputReader> dynamicInputReaderRef,
                                                   final AtomicReference<History> dynamicHistoryAtomicReference) {
        final Map<String, ?> data = new HashMap<String, Object>();
        return new CliEnv() {
            @Override
            public History history() {
                return dynamicHistoryAtomicReference.get();
            }

            @Override
            public InputReader reader() {
                return dynamicInputReaderRef.get();
            }

            @Override
            public Map<String, ?> userData() {
                return data;
            }
        };
    }

    protected static class FileInputReader implements InputReader {
        private final BufferedReader reader;

        public FileInputReader(final String file) {
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String readLine(final String prompt) throws IOException {
            return reader.readLine();
        }

        @Override
        public String readPassword(final String prompt) throws IOException {
            throw new IllegalArgumentException("Can't read password when input is a file");
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public void close() throws Exception {
            reader.close();
        }
    }

    private static class ExitException extends RuntimeException {
    }
}
