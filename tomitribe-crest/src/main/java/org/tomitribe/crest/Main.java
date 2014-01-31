/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest;

import org.tomitribe.crest.api.StreamingOutput;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    final Map<String, Cmd> commands = new ConcurrentHashMap<String, Cmd>();

    public Main() {
        this(new SystemPropertiesDefaultsContext(), Commands.load());
    }

    public Main(Class<?>... classes) {
        this(Arrays.asList(classes));
    }

    public Main(DefaultsContext defaultsContext, Class<?>... classes) {
        this(defaultsContext, Arrays.asList(classes));
    }

    public Main(DefaultsContext defaultsContext, Iterable<Class<?>> classes) {
        for (Class clazz : classes) {
            this.commands.putAll(Commands.get(clazz, defaultsContext));
        }

        installHelp(defaultsContext);
    }

    public Main(final Iterable<Class<?>> classes) {
        this(new SystemPropertiesDefaultsContext(), classes);
    }

    public void add(Cmd cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public void remove(Cmd cmd) {
        commands.remove(cmd.getName());
    }

    private void installHelp(DefaultsContext dc) {
        final Map<String, Cmd> stringCmdMap = Commands.get(new Help(Main.this.commands), dc);
        for (Cmd cmd : stringCmdMap.values()) {
            add(cmd);
        }
    }

    public static void main(String... args) throws Exception {
        try {
            final Environment env = new SystemEnvironment();
            final Main main = new Main();
            main.main(env, args);
        } catch (CommandFailedException e) {
            e.getCause().printStackTrace();
            System.exit(-1);
        } catch (Exception alreadyHandled) {
            System.exit(-1);
        }
    }

    public void main(Environment env, String[] args) throws Exception {
        final Environment old = Environment.local.get();
        Environment.local.set(env);

        try {
            final Object result = exec(args);
            if (result instanceof StreamingOutput) {
                ((StreamingOutput) result).write(env.getOutput());
            } else if (result instanceof String) {
                env.getOutput().println(result);
                final String string = (String) result;
                if (!string.endsWith("\n")) {
                    env.getOutput().println();
                }
            }
        } finally {
            Environment.local.set(old);
        }
    }


    public Object exec(String... args) throws Exception {
        final List<String> list = processSystemProperties(args);

        final String command = (list.size() == 0) ? "help" : list.remove(0);
        args = list.toArray(new String[list.size()]);

        final Cmd cmd = commands.get(command);

        if (cmd == null) {

            final PrintStream err = Environment.local.get().getError();
            err.println("Unknown command: " + command);
            err.println();
            commands.get("help").exec();
            throw new IllegalArgumentException();
        }

        return cmd.exec(args);
    }

    public static List<String> processSystemProperties(String[] args) {
        final List<String> list = new ArrayList<String>();

        // Read in and apply the properties specified on the command line
        for (String arg : args) {
            if (arg.startsWith("-D")) {

                final String name = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String value = arg.substring(arg.indexOf("=") + 1);

                final Properties properties = Environment.local.get().getProperties();
                properties.setProperty(name, value);
            } else {
                list.add(arg);
            }
        }

        return list;
    }

}
