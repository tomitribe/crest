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

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class CrestCliTest {
    @Test
    public void cli() throws Exception {
        final String input = "help\ntest\ntest | jgrep 2\nexit";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CrestCli cli = new CrestCli() {
            @Override
            protected CliEnvironment createMainEnvironment(final AtomicReference<InputReader> dynamicInputReaderRef) {
                final CliEnvironment mainEnvironment = super.createMainEnvironment(dynamicInputReaderRef);
                final InputStream is = new ByteArrayInputStream(input.getBytes());
                final PrintStream stdout = new PrintStream(out);
                return new CliEnvironment() {
                    @Override
                    public String readInput(final String prefix) {
                        return mainEnvironment.readInput(prefix);
                    }

                    @Override
                    public String readPassword(final String prefix) {
                        return mainEnvironment.readPassword(prefix);
                    }

                    @Override
                    public PrintStream getOutput() {
                        return stdout;
                    }

                    @Override
                    public PrintStream getError() {
                        return mainEnvironment.getError();
                    }

                    @Override
                    public InputStream getInput() {
                        return is;
                    }

                    @Override
                    public Properties getProperties() {
                        return mainEnvironment.getProperties();
                    }

                    @Override
                    public <T> T findService(final Class<T> type) {
                        return mainEnvironment.findService(type);
                    }
                };
            }

            @Override
            protected void onMainCreated(final Map<String, Cmd> mainCommands) {
                mainCommands.putAll(Commands.get(MyTestCmd.class));
            }

            @Override
            protected String nextPrompt() {
                return "prompt$";
            }
        };
        cli.run();
        assertEquals(
            "prompt$help\n" +
            "Commands: \n" +
            "                       \n" +
            "   exit                \n" +
            "   help                \n" +
            "   jgrep               \n" +
            "   jsed                \n" +
            "   pretty              \n" +
            "   test                \n" +
            "   wc                  \n" +
            "\n" +
            "prompt$test\n" +
            "line1\n" +
            "line 2\n" +
            "end\n" +
            "\n" +
            "prompt$test | jgrep 2\n" +
            "line 2\n" +
            "prompt$exit", new String(out.toByteArray()));
    }

    public static class MyTestCmd {
        @Command
        public static String test() {
            return "line1\nline 2\nend";
        }
    }
}
