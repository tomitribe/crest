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
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class CmdMethodTest extends TestCase {
    private static final String SOME_FILE = new File("/tmp/file.txt").getAbsolutePath();

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Commands.class);

    public void testGetUsage() {
        assertEquals("ls [options] File", commands.get("ls").getUsage());
        assertEquals("tail [options] File int", commands.get("tail").getUsage());
        assertEquals("set [options]", commands.get("set").getUsage());
    }

    public void test() throws Exception {

        {
            final Cmd tail = commands.get("tail");
            tail.exec("--number=45", SOME_FILE, "45");
            tail.exec(SOME_FILE, "100");
        }

        // Missing arguments
        try {
            final Cmd tail = commands.get("tail");
            tail.exec();
            fail();
        } catch (final IllegalArgumentException e) {

        }

        // Invalid option
        try {
            final Cmd tail = commands.get("tail");
            tail.exec(SOME_FILE, "100", "--color");
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    public void testImplicitPrimitiveDefaults() {
        // primitives
        // boolean options default to false
        {
            final Cmd cmd = commands.get("booleanOption");
            assertEquals(false, cmd.exec());
            assertEquals(true, cmd.exec("--long"));
        }
    }

    public void testRequiredOption() {
        // Required option
        try {
            final Cmd tail = commands.get("required");
            tail.exec();
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    public void testMissingOptionsWithExtraValues() {
        // Wrong arguments, should not be passed in as the two options
        // as they do not have "--key=name" and "--value=thx1138"
        try {
            final Cmd tail = commands.get("tail");
            tail.exec("name", "value");
            fail();
        } catch (final IllegalArgumentException e) {

        }
    }

    public void testBooleanOptions() {
        final Cmd ls = commands.get("ls");
        ls.exec("--long=true", SOME_FILE);
        ls.exec("--long", SOME_FILE);
    }

    public void testFileParameter() {
        final Cmd touch = commands.get("touch");
        assertNotNull(touch);
        touch.exec(SOME_FILE);
    }

    public static class Commands {

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
