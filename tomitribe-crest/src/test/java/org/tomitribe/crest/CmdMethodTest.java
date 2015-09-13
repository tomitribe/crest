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
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class CmdMethodTest extends TestCase {
    private static final String SOME_FILE = new File("/tmp/file.txt").getAbsolutePath();

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testGetUsage() {
        assertEquals("ls [options] File", commands.get("ls").getUsage());
        assertEquals("tail [options] File int", commands.get("tail").getUsage());
        assertEquals("set [options]", commands.get("set").getUsage());
        assertEquals("prefixed [options]", commands.get("prefixed").getUsage());
    }

    public void testSupportUserDashPrefixing() {
        Commands.prefixed = false;
        final Cmd cmd = commands.get("prefixed");
        cmd.exec(null, "-value=1", "----value=4");
        assertTrue(Commands.prefixed);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        cmd.help(new PrintStream(out));
        assertEquals(
                "Usage: prefixed [options]" +
                "Options: " +
                "  ----value=<String>    " +
                "     -value=<String>",
                new String(out.toByteArray()).replace(System.getProperty("line.separator"), "").trim());
    }

    public void test() throws Exception {

        {
            final Cmd tail = commands.get("tail");
            tail.exec(null, "--number=45", SOME_FILE, "45");
            tail.exec(null, SOME_FILE, "100");
        }

        // Missing arguments
        try {
            final Cmd tail = commands.get("tail");
            tail.exec(null);
            fail();
        } catch (final IllegalArgumentException e) {

        }

        // Invalid option
        try {
            final Cmd tail = commands.get("tail");
            tail.exec(null, SOME_FILE, "100", "--color");
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    public void testImplicitPrimitiveDefaults() {
        // primitives
        // boolean options default to false
        {
            final Cmd cmd = commands.get("booleanOption");
            assertEquals(false, cmd.exec(null));
            assertEquals(true, cmd.exec(null, "--long"));
        }
    }

    public void testRequiredOption() {
        // Required option
        try {
            final Cmd tail = commands.get("required");
            tail.exec(null);
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    public void testMissingOptionsWithExtraValues() {
        // Wrong arguments, should not be passed in as the two options
        // as they do not have "--key=name" and "--value=thx1138"
        try {
            final Cmd tail = commands.get("tail");
            tail.exec(null, "name", "value");
            fail();
        } catch (final IllegalArgumentException e) {

        }
    }

    public void testBooleanOptions() {
        final Cmd ls = commands.get("ls");
        ls.exec(null, "--long=true", SOME_FILE);
        ls.exec(null, "--long", SOME_FILE);
    }

    public void testFileParameter() {
        final Cmd touch = commands.get("touch");
        assertNotNull(touch);
        touch.exec(null, SOME_FILE);
    }

    public static class Commands {
        private static boolean prefixed;

        @Command
        public static void prefixed(@Option("-value") final String v, @Option("----value") final String four) {
            assertEquals("1", v);
            assertEquals("4", four);
            prefixed = true;
        }

        @Command
        public static void touch(final File file) {
            assertEquals(SOME_FILE, file.getAbsolutePath());
        }

        @Command("set")
        public static void setProperty(@Option("key") final String key, @Option("value") final String value) {
            assertEquals("name", key);
            assertEquals("thx1138", value);
        }

        @Command
        public static void ls(@Option("long") final Boolean longform, final File file) {
            assertNotNull(longform);
            assertTrue(longform);

            assertEquals(SOME_FILE, file.getAbsolutePath());
        }

        @Command
        public static boolean booleanOption(@Option("long") final boolean longform) {
            return longform;
        }

        @Command
        public static void tail(@Option("number") @Default("100") final int number, final File file, final int expected) {
            assertEquals(expected, number);
            assertEquals(SOME_FILE, file.getAbsolutePath());
        }

        @Command
        public static void tar(@Option("x") final File file) {
        }

        @Command
        public static void required(@Option("pass") @Required final String pass) {
        }

        @Command
        public StreamingOutput cat(final File file) {
            Files.exists(file);
            Files.readable(file);
            Files.file(file);

            return new StreamingOutput() {
                @Override
                public void write(final OutputStream os) throws IOException {
                    IO.copy(file, os);
                }
            };
        }
    }


}
