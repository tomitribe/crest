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
package org.tomitribe.crest.cli.impl;

import java.util.ArrayList;
import java.util.List;

public class CommandParser { // designed as a class in case we add config
    public Command[] toArgs(final String line) {
        if (line == null || line.length() == 0) {
            throw new IllegalArgumentException("Empty command.");
        }

        final List<Command> commands = new ArrayList<>();
        final List<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();

        char waitChar = ' ';
        boolean copyNextChar = false;
        boolean inEscaped = false;
        for (final char c : line.toCharArray()) {
            if (copyNextChar) {
                current.append(c);
                copyNextChar = false;
            } else if (waitChar == c) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                waitChar = ' ';
                inEscaped = false;
            } else {
                switch (c) {
                    case '"':
                    case '\'':
                        if (!inEscaped) {
                            waitChar = c;
                            inEscaped = true;
                            break;
                        } else {
                            current.append(c);
                        }
                        break;

                    case '\\':
                        copyNextChar = true;
                        break;

                    case '|':
                        flush(commands, result, current);
                        break;

                    default:
                        current.append(c);
                }
            }
        }

        if (waitChar != ' ') {
            throw new IllegalStateException("Missing closing " + Character.toString(waitChar));
        }

        flush(commands, result, current);
        return commands.toArray(new Command[commands.size()]);
    }

    private void flush(final List<Command> commands, final List<String> result, final StringBuilder current) {
        if (current.length() > 0) {
            result.add(current.toString());
            current.setLength(0);
        }
        if (!result.isEmpty()) {
            commands.add(new Command(result.toArray(new String[result.size()])));
            result.clear();
        }
    }

    public static final class Command {
        private String[] args;

        private Command(final String[] strings) {
            this.args = strings;
        }

        public String[] getArgs() {
            return args;
        }
    }
}

