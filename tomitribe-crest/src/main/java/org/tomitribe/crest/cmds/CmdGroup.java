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
package org.tomitribe.crest.cmds;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.utils.CommandLine;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.interceptor.internal.InternalInterceptor;

public class CmdGroup implements Cmd {

    final String name;
    final Map<String, Cmd> commands = new TreeMap<String, Cmd>();

    public CmdGroup(final Class<?> owner, final Map<String, Cmd> commands) {
        this.name = Commands.name(owner);
        this.commands.putAll(commands);
    }

    @Override
    public String getUsage() {
        return name + " [subcommand] [options]";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object exec(final Map<Class<?>, InternalInterceptor> globalInterceptors, String... rawArgs) {

        if (rawArgs.length == 0) {
            throw report(new IllegalArgumentException("Missing sub-command"));
        }

        final String name = rawArgs[0];
        final Cmd cmd = commands.get(name);

        if (cmd == null) {
            throw report(new IllegalArgumentException("No such sub-command: " + name));
        }

        String[] newArgs = new String[rawArgs.length - 1];
        System.arraycopy(rawArgs, 1, newArgs, 0, newArgs.length);

        return cmd.exec(globalInterceptors, newArgs);
    }

    private <E extends RuntimeException> E report(E e) {
        final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();
        err.println(e.getMessage());
        help(err);
        return e;
    }

    @Override
    public void help(PrintStream out) {
        out.print("Usage: ");
        out.println(getUsage());
        out.println();
        out.println("Sub commands: ");
        out.printf("   %-20s", "");
        out.println();

        final SortedSet<String> strings = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(final String s1, final String s2) {
                assert null != s1;
                assert null != s2;
                return s1.compareTo(s2);
            }
        });

        strings.addAll(commands.keySet());

        for (final String command : strings) {
            out.printf("   %-20s%n", command);
        }
    }
    
    public void help(String subCommand, PrintStream out) {
        final Cmd subCmd = commands.get(subCommand);
        if (subCmd == null) {
            help(out);
        } else {
            subCmd.help(out);
        }
    }

    @Override
    public Collection<String> complete(final String buffer, final int cursorPosition) {

        final List<String> results = new ArrayList<String>();

        try {

            final String commandLine = buffer.substring(0, cursorPosition);
            final String[] args = CommandLine.translateCommandline(commandLine);
            
            // first arg should be the same name as this command
            if (args.length >= 1 && args[0].equals(getName())) {

                if (args.length > 2 || (args.length == 2 && commandLine.endsWith(" "))) {
                    // find the subcommand and delegate completion to it
                    final Cmd cmd = commands.get(args[1]);
                    if (cmd != null) {
                        // need to remove the first command
                        final String subcommand = buffer.replaceAll(getName() + "\\s+(.*)$", "$1");
                        final int diff = buffer.length() - subcommand.length();
                        return cmd.complete(subcommand, cursorPosition - diff);
                    }
                    return results;
                }

                final String prefix;
                if (args.length == 1 && commandLine.endsWith(" ")) {
                    prefix = "";
                } else {
                    prefix = args[1];
                }

                // look at all the possible commands and return those that match
                final Iterator<String> iterator = commands.keySet().iterator();
                while (iterator.hasNext()) {
                    final String commandName = iterator.next();
                    if (commandName.startsWith(prefix)) {
                        results.add(commandName + " ");
                    }
                }
            }
        } catch (Exception e) {
            // quietly fail and return nothing.
            e.printStackTrace();
        }
        return results;
    }
}
