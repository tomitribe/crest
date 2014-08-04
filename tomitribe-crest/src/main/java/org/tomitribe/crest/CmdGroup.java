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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CmdGroup implements Cmd {

    final String name;
    final Map<String, Cmd> commands = new TreeMap<String, Cmd>();

    public CmdGroup(final Class<?> owner, final Map<String, Cmd> commands) {
        this.name = Commands.name(owner);
        this.commands.putAll(commands);
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object exec(String... rawArgs) {

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

        return cmd.exec(newArgs);
    }

    private <E extends RuntimeException> E report(E e) {
        final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();
        err.println(e.getMessage());
        help(err);
        return e;
    }

    @Override
    public void help(PrintStream out) {

    }

    @Override
    public Collection<String> complete(final String buffer, final int cursorPosition) {
        
        final List<String> results = new ArrayList<String>();
        
        try {
        
            final String commandLine = buffer.substring(0, cursorPosition);
            final String[] args = CommandLine.translateCommandline(commandLine);
            
            // first arg should be the same name as this command
            if (args.length >= 1 && args[0].equals(getName())) {
                
                // 
                if (args.length > 2 || (args.length == 2 && commandLine.endsWith(" "))) {
                    // find the subcommand and delegate completion to it
                    final Cmd cmd = commands.get(args[1]);
                    if (cmd != null) {
                        // need to remove the first command
                        final String subcommand = buffer.replaceAll(getName() + "\\s+(.*)$", "$1");
                        final int diff = buffer.length() - subcommand.length();
                        return cmd.complete(subcommand, cursorPosition - diff);
                    }
                } else {
                    final String prefix;
                    if (args.length == 1 && commandLine.endsWith(" ")) {
                        prefix = "";
                    } else {
                        prefix = args[1];
                    }
                    
                    // look at all the possible commands and return those that match
                    final Iterator<String> iterator = commands.keySet().iterator();
                    while (iterator.hasNext()) {
                        final String commandName = (String) iterator.next();
                        if (commandName.startsWith(prefix)) {
                            results.add(commandName);
                        }
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
