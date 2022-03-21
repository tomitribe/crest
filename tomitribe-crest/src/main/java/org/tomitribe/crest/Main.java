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
package org.tomitribe.crest;

import org.tomitribe.crest.api.Exit;
import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CommandFailedException;
import org.tomitribe.crest.cmds.Completer;
import org.tomitribe.crest.cmds.HelpPrintedException;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;
import org.tomitribe.crest.interceptor.internal.InternalInterceptor;
import org.tomitribe.crest.table.Border;
import org.tomitribe.crest.table.Data;
import org.tomitribe.crest.table.Table;
import org.tomitribe.crest.table.TableInterceptor;
import org.tomitribe.crest.term.Screen;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Main implements Completer {

    protected final Map<String, Cmd> commands = new ConcurrentHashMap<>();
    protected final Map<Class<?>, InternalInterceptor> interceptors = new HashMap<>();

    public Main() {
        this(new SystemPropertiesDefaultsContext(), Commands.load());
    }

    public Main(final Class<?>... classes) {
        this(Arrays.asList(classes));
    }

    public Main(final DefaultsContext defaultsContext, final Class<?>... classes) {
        this(defaultsContext, Arrays.asList(classes));
    }

    public Main(final DefaultsContext defaultsContext, final Iterable<Class<?>> classes) {
        for (final Class clazz : classes) {
            processClass(defaultsContext, clazz);
        }

        processClass(defaultsContext, TableInterceptor.class);
        installHelp(defaultsContext);
    }

    public void processClass(final DefaultsContext defaultsContext, final Class<?> clazz) {
        final Map<String, Cmd> m = Commands.get(clazz, defaultsContext);
        if (!m.isEmpty()) {
            this.commands.putAll(m);
        } else {

            final InternalInterceptor internalInterceptor = InternalInterceptor.from(clazz);
            if (interceptors.put(clazz, internalInterceptor) != null) {
                throw new IllegalArgumentException(clazz + " interceptor is conflicting");
            }

            for (final Annotation annotation : clazz.getDeclaredAnnotations()) {
                if (isCustomInterceptorAnnotation(annotation)){
                    if (interceptors.put(annotation.annotationType(), internalInterceptor) != null) {
                        throw new IllegalArgumentException(clazz + " interceptor is conflicting");
                    }
                }
            }
        }
    }

    private static boolean isCustomInterceptorAnnotation(final Annotation annotation) {
        for (final Annotation declaredAnnotation : annotation.annotationType().getDeclaredAnnotations()) {
            if (declaredAnnotation instanceof CrestInterceptor) {
                return true;
            }
        }
        return false;
    }

    public Main(final Iterable<Class<?>> classes) {
        this(new SystemPropertiesDefaultsContext(), classes);
    }

    public void add(final Cmd cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public void remove(final Cmd cmd) {
        commands.remove(cmd.getName());
    }

    private void installHelp(final DefaultsContext dc) {
        final Map<String, Cmd> stringCmdMap = Commands.get(new Help(Main.this.commands), dc);
        for (final Cmd cmd : stringCmdMap.values()) {
            add(cmd);
        }
    }

    public static void main(final String... args) throws Exception {
        final Environment env = new SystemEnvironment();
        final Consumer<Integer> onExit = System::exit;

        main(env, onExit, args);
    }

    /**
     * Added additional method for greater testability, including knowing if exit
     * is properly called with the correct value.
     */
    public static void main(final Environment env, final Consumer<Integer> onExit, final String... args) {
        try {
            final Main main = new Main();
            main.main(env, args);
        } catch (final CommandFailedException e) {

            final Throwable cause = e.getCause();

            handle(env, onExit, cause);

        } catch (final Throwable throwable) {

            handle(env, onExit, throwable);
        }
    }

    private static void handle(final Environment env, final Consumer<Integer> onExit, final Throwable cause) {
        final Exit exit = cause.getClass().getAnnotation(Exit.class);
        final int code = (exit != null) ? exit.value() : -1;

        if (cause instanceof HelpPrintedException) {

            // these are already handled via message + help
            onExit.accept(code);

        } else if (exit != null) {

            env.getError().println(cause.getMessage());
            onExit.accept(exit.value());

        } else {

            cause.printStackTrace(env.getError());
            onExit.accept(-1);

        }
    }

    public void main(final Environment env, final String... args) throws Exception {
        final Environment old = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        Environment.ENVIRONMENT_THREAD_LOCAL.set(env);

        try {
            final Object result = exec(args);

            if (result == null) return;

            final PrintStream out = env.getOutput();

            if (result instanceof StreamingOutput) {

                ((StreamingOutput) result).write(out);

            } else if (result instanceof PrintOutput) {

                ((PrintOutput) result).write(out);

            } else if (result instanceof Stream) {

                ((Stream<?>) result)
                        .map(o -> o == null ? "" : o)
                        .map(Object::toString)
                        .forEach(out::println);

            } else if (result instanceof Iterable) {

                final Iterable iterable = (Iterable) result;

                for (final Object o : iterable) {
                    if (o != null) out.println(o.toString());
                }

            } else if (result instanceof String) {

                final String string = (String) result;

                out.print(string);

                if (!string.endsWith("\n")) out.println();

            } else if (result instanceof String[][]) {

                final int guess = Screen.guessWidth();
                final int width = guess > 0 ? guess : 150;

                final Data data = new Data((String[][]) result, true);
                final Table table = new Table(data, Border.asciiCompact().build(), width);

                table.format(out);

            } else {

                out.println(result);

            }
        } finally {
            Environment.ENVIRONMENT_THREAD_LOCAL.set(old);
        }
    }

    public Object exec(String... args) throws Exception {
        final List<String> list = processSystemProperties(args);

        final String command = (list.isEmpty()) ? "help" : list.remove(0);
        args = list.toArray(new String[list.size()]);

        if (command.equals("_completion")) {
            return BashCompletion.generate(this, args);
        }

        final Cmd cmd = commands.get(command);

        if (cmd == null) {

            final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();
            err.println("Unknown command: " + command);
            err.println();
            commands.get("help").exec(interceptors);
            throw new IllegalArgumentException();
        }

        return cmd.exec(interceptors, args);
    }

    public static List<String> processSystemProperties(final String[] args) {
        final List<String> list = new ArrayList<>();

        // Read in and apply the properties specified on the command line
        for (final String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf('='));
                final String value = arg.substring(arg.indexOf('=') + 1);

                final Properties properties = Environment.ENVIRONMENT_THREAD_LOCAL.get().getProperties();
                properties.setProperty(name, value);
            } else {
                list.add(arg);
            }
        }

        return list;
    }

    @Override
    public Collection<String> complete(final String buffer, final int cursorPosition) {
        final List<String> cmds = new ArrayList<>();

        if (buffer == null || buffer.isEmpty()) {
            final Set<String> cmd = commands.keySet();
            for (final String s : cmd) {
                cmds.add(s + " ");
            }
        } else {

            if (buffer.substring(0, cursorPosition).contains(" ")) {
                final Cmd cmd = getCmd(buffer);

                if (cmd != null) {
                    return cmd.complete(buffer, cursorPosition);
                }
            }

            final String prefix = buffer.substring(0, cursorPosition);
            Iterator<String> iterator = commands.keySet().iterator();
            while (iterator.hasNext()) {
                final String command = iterator.next();
                if (command.startsWith(prefix)) {
                    cmds.add(command + " ");
                }
            }
        }

        Collections.sort(cmds);
        return cmds;
    }

    private Cmd getCmd(String buffer) {
        final String commandName = buffer.replaceAll("^(\\w*).*?$", "$1");
        final Iterator<String> iterator = this.commands.keySet().iterator();

        while (iterator.hasNext()) {
            String cmd = iterator.next();
            if (cmd.equals(commandName)) {
                return this.commands.get(cmd);
            }
        }

        return null;
    }
}
