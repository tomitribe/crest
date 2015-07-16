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
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CommandFailedException;
import org.tomitribe.crest.cmds.Completer;
import org.tomitribe.crest.cmds.builder.ParameterBuilder;
import org.tomitribe.crest.cmds.builder.ParameterBuilders;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.cmds.validator.ParameterValidator;
import org.tomitribe.crest.cmds.validator.ParameterValidators;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Main implements Completer {

    private final Map<String, Cmd> commands = new ConcurrentHashMap<String, Cmd>();

    public Main(final DefaultsContext defaultsContext, final Iterable<Class<?>> classes,
                final List<ParameterBuilder> injectors, final List<ParameterValidator> validators) {
        final Map<Class<?>, ParameterBuilder> injectorMap = ParameterBuilders.map(injectors);

        for (final Class clazz : classes) {
            this.commands.putAll(Commands.get(clazz, defaultsContext, injectorMap, validators));
        }

        installHelp(defaultsContext);
    }

    public Main() {
        this(new SystemPropertiesDefaultsContext(), Commands.load(), ParameterBuilders.DEFAULTS, ParameterValidators.DEFAULTS);
    }

    public Main(final Class<?>... classes) {
        this(Arrays.asList(classes));
    }

    public Main(final DefaultsContext defaultsContext, final Class<?>... classes) {
        this(defaultsContext, Arrays.asList(classes), ParameterBuilders.DEFAULTS, ParameterValidators.DEFAULTS);
    }

    public Main(final Iterable<Class<?>> classes) {
        this(new SystemPropertiesDefaultsContext(), classes, ParameterBuilders.DEFAULTS, ParameterValidators.DEFAULTS);
    }

    public void add(final Cmd cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public void remove(final Cmd cmd) {
        commands.remove(cmd.getName());
    }

    private void installHelp(final DefaultsContext dc) {
        final Map<String, Cmd> stringCmdMap = Commands.get(
                new Help(Main.this.commands), dc, Collections.<Class<?>, ParameterBuilder>emptyMap(), Collections.<ParameterValidator>emptyList());
        for (final Cmd cmd : stringCmdMap.values()) {
            add(cmd);
        }
    }

    public static void main(final String... args) throws Exception {
        new Main().doMain(new SystemEnvironment(), args);
    }

    public void doMain(final Environment env, final String[] args) {
        try {
            main(env, args);
        } catch (final CommandFailedException e) {

            final Throwable cause = e.getCause();
            final Exit exit = cause.getClass().getAnnotation(Exit.class);
            if (exit != null) {

                System.err.println(e.getMessage());
                System.exit(exit.value());

            } else {

                cause.printStackTrace();
                System.exit(-1);

            }

        } catch (final Exception alreadyHandled) {
            System.exit(-1);
        }
    }

    public void main(final Environment env, final String[] args) throws Exception {
        final Environment old = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        Environment.ENVIRONMENT_THREAD_LOCAL.set(env);

        try {
            final Object result = exec(args);

            if (result == null) return;

            final PrintStream out = env.getOutput();

            if (result instanceof StreamingOutput) {

                ((StreamingOutput) result).write(out);

            } else if (result instanceof Iterable) {

                final Iterable iterable = (Iterable) result;

                for (final Object o : iterable) {
                    if (o != null) out.println(o.toString());
                }

            } else if (result instanceof String) {

                final String string = (String) result;

                out.println(string);

                if (!string.endsWith("\n")) out.println();
            } else {

                out.println(result);

            }
        } finally {
            Environment.ENVIRONMENT_THREAD_LOCAL.set(old);
        }
    }

    public Object exec(String... args) throws Exception {
        final List<String> list = processSystemProperties(args);

        final String command = (list.size() == 0) ? "help" : list.remove(0);
        args = list.toArray(new String[list.size()]);

        final Cmd cmd = commands.get(command);

        if (cmd == null) {

            final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();
            err.println("Unknown command: " + command);
            err.println();
            commands.get("help").exec();
            throw new IllegalArgumentException();
        }

        return cmd.exec(args);
    }

    public static List<String> processSystemProperties(final String[] args) {
        final List<String> list = new ArrayList<String>();

        // Read in and apply the properties specified on the command line
        for (final String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String value = arg.substring(arg.indexOf("=") + 1);

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
        final List<String> cmds = new ArrayList<String>();

        if (buffer == null || buffer.length() == 0) {
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
            for (final String command : commands.keySet()) {
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

        for (final String cmd : this.commands.keySet()) {
            if (cmd.equals(commandName)) {
                return this.commands.get(cmd);
            }
        }

        return null;
    }

    public Map<String, Cmd> getCommands() {
        return commands;
    }
}
