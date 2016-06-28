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

import jline.console.history.History;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.contexts.DefaultsContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class CrestCliTest {
    @Test
    public void cli() throws Exception {
        final String input = "help\n" + "test\n" + "test | jgrep 2\n" + "exit";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CrestCli cli = newTestCli(input, out, null);
        cli.run();
        assertEquals(
            portable("prompt$help\n" +
            "Commands: \n" +
            "                       \n" +
            "   clear               \n" +
            "   exit                \n" +
            "   help                \n" +
            "   history             \n" +
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
            "prompt$exit"), portable(new String(out.toByteArray())));
    }

    private CrestCli newTestCli(final String input, final ByteArrayOutputStream out, final File alias) {
        return new CrestCli() {
                @Override
                protected CliEnvironment createMainEnvironment(final AtomicReference<InputReader> dynamicInputReaderRef,
                                                               final AtomicReference<History> historyAtomicReference) {
                    final CliEnvironment mainEnvironment = super.createMainEnvironment(dynamicInputReaderRef, historyAtomicReference);
                    final InputStream is = new ByteArrayInputStream(input.getBytes());
                    final PrintStream stdout = new PrintStream(out);
                    return new CliEnvironment() {
                        @Override
                        public History history() {
                            return historyAtomicReference.get();
                        }

                        @Override
                        public InputReader reader() {
                            return dynamicInputReaderRef.get();
                        }

                        @Override
                        public Map<String, ?> userData() {
                            return mainEnvironment.userData();
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
                protected void onMainCreated(final DefaultsContext ctx, final Main main) {
                    main.processClass(ctx, MyTestCmd.class);
                }

                @Override
                protected String nextPrompt() {
                    return "prompt$";
                }

                @Override
                protected File aliasesFile() {
                    return alias;
                }
            };
    }

    @Test
    public void alias() throws Exception {
        final File file = new File("target/clitest/alias.txt");
        file.getParentFile().mkdirs();
        final FileWriter w = new FileWriter(file);
        w.write("al=test");
        w.close();

        final String input = "al\n" + "exit";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        newTestCli(input, out, file).run();
        assertEquals(
            "prompt$al\n" +
            "line1\n" +
            "line 2\n" +
            "end\n" +
            "\n" +
            "prompt$exit", portable(new String(out.toByteArray())));
    }

    @Test
    public void time() throws Exception {
        final String input = "time test\n" + "exit";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        newTestCli(input, out, null).run();
        assertEquals(
            portable("prompt$time test\n" +
            "line1\n" +
            "line 2\n" +
            "end\n" +
            "\n" +
            "Time 0s Xms\n" +
            "prompt$exit"), portable(new String(out.toByteArray()).replaceAll("[0-9]+ms", "Xms")));
    }

    private static String portable(final String raw) {
        return raw.replace("\r", "");
    }

    public static class MyTestCmd {
        @Command
        public static String test() {
            return "line1\n" + "line 2\n" + "end";
        }
    }
}
