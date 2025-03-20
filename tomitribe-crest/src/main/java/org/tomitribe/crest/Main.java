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

import org.tomitribe.crest.api.Editor;
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
import org.tomitribe.crest.table.Formatting;
import org.tomitribe.crest.table.TableInterceptor;

import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
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
    protected final Consumer<Integer> onExit;
    protected final Environment environment;
    protected final String name;
    protected final String version;

    public Main() {
        this(new SystemPropertiesDefaultsContext(), Commands.load(), new SystemEnvironment(), System::exit);
    }

    public Main(final Class<?>... classes) {
        this(Arrays.asList(classes));
    }

    public Main(final DefaultsContext defaultsContext, final Class<?>... classes) {
        this(defaultsContext, Arrays.asList(classes), new SystemEnvironment(), System::exit);
    }

    public Main(final Iterable<Class<?>> classes) {
        this(new SystemPropertiesDefaultsContext(), classes, new SystemEnvironment(), System::exit);
    }

    private Main(final DefaultsContext defaultsContext, final Iterable<Class<?>> classes, final Environment environment,
                 final Consumer<Integer> onExit) {
        this(defaultsContext, classes, environment, onExit, null, null);
    }

    public Main(final DefaultsContext defaultsContext, final Iterable<Class<?>> classes, final Environment environment,
                final Consumer<Integer> onExit, final String name, final String version) {
        this.environment = environment;
        this.onExit = onExit;
        this.version = version;
        this.name = name;

        for (final Class clazz : classes) {
            processClass(defaultsContext, clazz);
        }

        // Built-in formatters
        processClass(defaultsContext, TableInterceptor.class);

        // Built-in commands
        installHelp(defaultsContext);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void processClass(final DefaultsContext defaultsContext, final Class<?> clazz) {
        final Map<String, Cmd> m = Commands.get(clazz, defaultsContext);
        if (!m.isEmpty()) {
            this.commands.putAll(m);
        } else if (clazz.isAnnotationPresent(Editor.class)) {
            final Editor annotation = clazz.getAnnotation(Editor.class);
            try {
                PropertyEditorManager.registerEditor(annotation.value(), clazz);
            } catch (final Exception e) {
                // no-op
            }
        } else {

            final InternalInterceptor internalInterceptor = InternalInterceptor.from(clazz);
            if (interceptors.put(clazz, internalInterceptor) != null) {
                throw new IllegalArgumentException(clazz + " interceptor is conflicting");
            }

            for (final Annotation annotation : clazz.getDeclaredAnnotations()) {
                if (isCustomInterceptorAnnotation(annotation)) {
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


    public void add(final Cmd cmd) {
        commands.put(cmd.getName(), cmd);
    }

    private void installHelp(final DefaultsContext dc) {
        final Map<String, Cmd> stringCmdMap = Commands.get(new Help(Main.this.commands, Main.this.version, Main.this.name), dc);
        for (final Cmd cmd : stringCmdMap.values()) {
            add(cmd);
        }
    }

    public static void main(final String... args) throws Exception {
        builder().build().run(args);
    }

    public void run(final String... args) {

        try {
            main(this.environment, args);
        } catch (final CommandFailedException e) {

            final Throwable cause = e.getCause();

            handle(environment, onExit, cause);

        } catch (final Throwable throwable) {

            handle(environment, onExit, throwable);
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

                final String[][] data = (String[][]) result;

                Formatting.asPrintStream(data).write(out);

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


    /**
     * A convenience builder method that returns a builder populated with java.lang.System
     * providing the default values for Env, Properties, Stdin, Stdout, Stderr
     * and a call to System.exit() for any unsuccessful command executions.
     *
     * This builder method is equal to the following
     * <pre>
     *             Main.builder()
     *                 .properties(System.getProperties())
     *                 .env(System.getenv())
     *                 .out(System.out)
     *                 .err(System.err)
     *                 .in(System.in)
     *                 .exit(System::exit);
     * </pre>
     *
     * If no commands are specified, commands will be discovered via the classic
     * mechanisms: Loader; classpath scan.
     *
     * @return a builder with system defaults
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<String, String> env = System.getenv();
        private PrintStream out = System.out;
        private PrintStream err = System.err;
        private InputStream in = System.in;

        private List<Class<?>> commands = new ArrayList<>();
        private Consumer<Integer> exit = System::exit;
        private Properties properties = System.getProperties();
        private String version;

        private String name;

        /**
         * Specifies a version that Crest will print with help messages
         *
         * If not specified, no version will be printed.
         */
        public Builder version(final String version) {
            this.version = version;
            return this;
        }

        /**
         * Specifies the name crest will use as the root command name
         * in help and options messages.  This should be the name
         * of the executable file that users will execute on the
         * command-line itself.
         *
         * If not specified will default to System.getProperty("cmd")
         * and finally to System.getenv("CMD").
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds the name value pair to the existing environment,
         * overwriting any previous entry with the same name.
         */
        public Builder env(final String name, final String value) {
            env.put(name, value);
            return this;
        }

        /**
         * Entirely replaces all env entries previously added
         * and with the Map instance supplied.  Updates to the
         * map after builder.build() has been executed will be
         * reflected in the environment commands see.
         */
        public Builder env(final Map<String, String> env) {
            this.env = env;
            return this;
        }

        /**
         * Adds the name value pair to the existing properties,
         * overwriting any previous entry with the same name.
         */
        public Builder property(final String name, final String value) {
            properties.put(name, value);
            return this;
        }

        /**
         * Entirely replaces all env entries previously added
         * and with the Map instance supplied.  Updates to the
         * map after builder.build() has been executed will be
         * reflected in the environment commands see.
         */
        public Builder properties(final Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder out(final PrintStream out) {
            this.out = out;
            return this;
        }

        public Builder err(final PrintStream err) {
            this.err = err;
            return this;
        }

        public Builder in(final InputStream in) {
            this.in = in;
            return this;
        }

        /**
         * Adds a @Command class or @CrestInterceptor to the
         * environment.  If no classes are specified via this
         * method then they will be discovered via the classpath.
         * @param commandClass
         * @return
         */
        public Builder command(final Class<?> commandClass) {
            this.commands.add(commandClass);
            return this;
        }

        public Builder exit(final Consumer<Integer> consumer) {
            this.exit = consumer;
            return this;
        }

        public Builder noexit() {
            this.exit = integer -> {
            };
            return this;
        }

        public Main build() {
            try {
                final Iterable<Class<?>> commands = this.commands.size() == 0 ? Commands.load() : this.commands;

                final String name = this.name == null ? lookupName() : this.name;
                final String version = this.version == null ? lookupVersion() : this.version;

                final Environment environment = SystemEnvironment.builder()
                        .out(out)
                        .in(in)
                        .err(err)
                        .properties(properties)
                        .name(name)
                        .version(version)
                        .build();


                return new Main(new SystemPropertiesDefaultsContext(), commands, environment, exit, name, version);
            } catch (final Exception e) {
                throw new MainBuildException(e);
            }
        }

        private String lookupName() {
            {
                final String name = System.getProperty("cmd.name");
                if (name != null) return asFilename(name);
            }

            {
                final String name = System.getProperty("cmd");
                if (name != null) return asFilename(name);
            }

            {
                final String name = System.getenv("CMD_NAME");
                if (name != null) return asFilename(name);
            }

            {
                final String name = System.getenv("CMD");
                if (name != null) return asFilename(name);
            }

            final Manifest manifest = Manifest.get().orElse(null);
            if (manifest == null) return null;

            {
                final String name = manifest.getCommandName();
                if (name != null) return name;
            }

            return null;
        }

        private String lookupVersion() {
            {
                final String version = System.getProperty("cmd.version");
                if (version != null) return asFilename(version);
            }

            {
                final String version = System.getenv("CMD_VERSION");
                if (version != null) return asFilename(version);
            }

            final Manifest manifest = Manifest.get().orElse(null);
            if (manifest == null) return null;

            {
                final String name = manifest.getCommandVersion();
                if (name != null) return name;
            }

            {
                final String name = manifest.getImplementationVersion();
                if (name != null) return name;
            }

            return null;
        }

        private String asFilename(String name) {
            final File file = new File(name);
            return file.getName();
        }

        @Exit(1)
        public static class MainBuildException extends RuntimeException {
            public MainBuildException(final Exception e) {
                super(String.format("Unable to build Main. " + e.getMessage()));
            }
        }
    }
}
