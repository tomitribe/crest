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

import junit.framework.TestCase;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;

import java.util.Map;

/**
 * Variable substitution only available in @Default values
 */
public class DefaultSubstitutionTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testDefaultValues() throws Exception {

        commands.get("doRelyOnDefaults").exec(null, "--format=${user.home}", "--pattern=${user.home}", "--pattern=${user.dir}", "${user.home}", "${user.home}", "${user.dir}");

    }

    public void testExplicitValues() throws Exception {

        commands.get("doAllExplicit").exec(null,
                "--user-home=${user.home}",
                "--directory=${user.home}",
                "--directory=${user.dir}",
                "--format=${user.home}",
                "--pattern=${user.home}",
                "--pattern=${user.dir}",
                "${user.home}",
                "${user.home}",
                "${user.dir}");

    }

    public static class Commands {

        @Command
        public void doRelyOnDefaults(
                @Option("user-home") @Default("${user.home}") final Value defaultParameter,
                @Option("directory") @Default("${user.home}\u0000${user.dir}") final Value[] defaultParameters,
                @Option("format") final Value explicitParameter,
                @Option("pattern") final Value[] explicitParameters,
                final Value explicitArgument,
                final Value[] explicitArguments
        ) {

            { // @Default values are interpreted

                final Value userHome = new Value(System.getProperty("user.home"));
                final Value userDir = new Value(System.getProperty("user.dir"));

                assertEquals(userHome, defaultParameter);
                assertEquals(userHome, defaultParameters[0]);
                assertEquals(userDir, defaultParameters[1]);
            }

            { // Explicit values are NOT interpreted

                final Value userHome = new Value("${user.home}");
                final Value userDir = new Value("${user.dir}");

                assertEquals(userHome, explicitParameter);
                assertEquals(userHome, explicitParameters[0]);
                assertEquals(userDir, explicitParameters[1]);

                assertEquals(userHome, explicitArgument);
                assertEquals(userHome, explicitArguments[0]);
                assertEquals(userDir, explicitArguments[1]);
            }
        }

        @Command
        public void doAllExplicit(
                @Option("user-home") @Default("${user.home}") final Value defaultParameter,
                @Option("directory") @Default("${user.home}\u0000${user.dir}") final Value[] defaultParameters,
                @Option("format") final Value explicitParameter,
                @Option("pattern") final Value[] explicitParameters,
                final Value explicitArgument,
                final Value[] explicitArguments
        ) {

            // Explicit values are NOT interpreted
            final Value userHome = new Value("${user.home}");
            final Value userDir = new Value("${user.dir}");

            assertEquals(userHome, defaultParameter);
            assertEquals(userHome, defaultParameters[0]);
            assertEquals(userDir, defaultParameters[1]);

            assertEquals(userHome, explicitParameter);
            assertEquals(userHome, explicitParameters[0]);
            assertEquals(userDir, explicitParameters[1]);

            assertEquals(userHome, explicitArgument);
            assertEquals(userHome, explicitArguments[0]);
            assertEquals(userDir, explicitArguments[1]);
        }

    }

    public static class Value {
        private final String string;

        public Value(final String string) {
            this.string = string;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Value value = (Value) o;

            return string.equals(value.string);
        }

        @Override
        public int hashCode() {
            return string.hashCode();
        }
    }
}
