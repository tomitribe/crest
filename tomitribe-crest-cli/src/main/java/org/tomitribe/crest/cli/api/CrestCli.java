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
import jline.console.completer.Completer;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cli.api.interceptor.interactive.AskMissingParameters;
import org.tomitribe.crest.cli.impl.CliEnv;
import org.tomitribe.crest.cli.impl.CommandParser;
import org.tomitribe.crest.cli.impl.command.Streams;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;

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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CrestCli {
    private static volatile Runnable exitHook;

    @Command
    public static void exit() {
        if (exitHook != null) {
            exitHook.run();
        }
        throw new ExitException();
    }

    // using all defaults
    public static void main(final String[] args) throws Exception {
        new CrestCli().run(args);
    }

    public void run(final String... args) throws Exception {
        final AtomicReference<InputReader> inputReaderRef = new AtomicReference<InputReader>();
        final CliEnvironment env = createMainEnvironment(inputReaderRef);

        final DefaultsContext ctx = new SystemPropertiesDefaultsContext();
        final Main main = newMain(ctx);

        final History history;
        final InputReader readerFacade;
        if (args == null || args.length == 0) {
            final ConsoleReader reader = new ConsoleReader(env.getInput(), env.getOutput());
            final File historyFile = cliHistoryFile();
            history = historyFile != null && historyFile.isFile() ? new FileHistory(historyFile) : new MemoryHistory();
            reader.setHistory(history);

            reader.addCompleter(new Completer() {
                @Override
                public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
                    candidates.addAll(main.complete(buffer, cursor));
                    return buffer.lastIndexOf(" ", cursor) + 1; // TODO: enhance it
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
                public void close() throws Exception {
                    // no-op
                }
            };
        } else {
            history = null;
            readerFacade = new FileInputReader(args["-f".equals(args[0]) ? 1 : 0]);
        }
        inputReaderRef.set(readerFacade);

        final int nThreads = Integer.getInteger("crest.cli.pipping.threads", 4);
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
            while ((line = readerFacade.readLine(nextPrompt())) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    final CommandParser.Command[] commands = parser.toArgs(line);
                    if (commands.length > nThreads) {
                        throw new IllegalArgumentException("Current implementation doesn't support more than " + nThreads + " piped commands.");
                    }

                    if (commands.length == 1) {
                        try {
                            main.main(env, commands[0].getArgs());
                        } catch (final Exception error) {
                            if (!ExitException.class.isInstance(error.getCause())) {
                                error.printStackTrace(env.getError());
                            }
                        }
                    } else { // should move to a common module
                        // execute tasks piping them
                        final InputStream[] ins = new InputStream[commands.length];
                        final OutputStream[] outs = new OutputStream[commands.length];
                        for (int i = 0; i < commands.length; i++) { // allocate
                            ins[i] = i == 0 ? env.getInput() : new PipedInputStream();
                            outs[i] = i == commands.length - 1 ? env.getOutput() : new PipedOutputStream();
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
                                        main.main(pipeEnvironment(env, ins[idx], out), commands[idx].getArgs());
                                    } catch (final Exception error) {
                                        error.printStackTrace(env.getError());
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
                        env.getOutput().flush();
                    }
                } catch (final Exception error) {
                    error.printStackTrace(env.getError());
                    env.getError().flush();
                }
            }
        } catch (final ExitException ee) {
            // no-op: exit
        } finally {
            exitHook.run();
            exitHook = null;
        }
    }

    protected Main newMain(final DefaultsContext ctx) {
        return new Main() {
            {
                // pipeable commands
                commands.putAll(Commands.get(Streams.class, ctx));

                // built-in interceptors
                commands.putAll(Commands.get(AskMissingParameters.class, ctx));

                // hook for user extensions
                onMainCreated(commands);

                // ensure we can quit
                if (!commands.containsKey("exit")) { // add exit command
                    commands.putAll(Commands.get(CrestCli.class, ctx));
                }
            }
        };
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

    protected void onMainCreated(final Map<String, Cmd> mainCommands) {
        // no-op
    }

    protected CliEnvironment pipeEnvironment(final CliEnvironment env, final InputStream in, final PrintStream out) {
        return new CliEnvironment() {
            @Override
            public String readInput(final String prefix) {
                return env.readInput(prefix);
            }

            @Override
            public String readPassword(final String prefix) {
                return env.readPassword(prefix);
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

    protected CliEnvironment createMainEnvironment(final AtomicReference<InputReader> dynamicInputReaderRef) {
        return new CliEnv() {
            @Override
            public String readInput(final String prefix) {
                try {
                    return dynamicInputReaderRef.get().readLine(prefix);
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public String readPassword(final String prefix) {
                try {
                    return dynamicInputReaderRef.get().readPassword(prefix);
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    protected interface InputReader extends AutoCloseable {
        String readLine(String prompt) throws IOException;

        String readPassword(String prompt) throws IOException;
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
        public void close() throws Exception {
            reader.close();
        }
    }

    private static class ExitException extends RuntimeException {
    }
}
