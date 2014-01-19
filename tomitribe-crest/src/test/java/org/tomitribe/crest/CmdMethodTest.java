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

import junit.framework.TestCase;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.util.Files;
import org.tomitribe.crest.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class CmdMethodTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Commands.class);

    public void test() throws Exception {

        {
            final Cmd tail = commands.get("tail");
            tail.exec("--number=45", "/some/file.txt", "45");
            tail.exec("/some/file.txt", "100");
        }

        // Missing arguments
        try {
            final Cmd tail = commands.get("tail");
            tail.exec();
            fail();
        } catch (IllegalArgumentException e) {

        }

        // Invalid option
        try {
            final Cmd tail = commands.get("tail");
            tail.exec("/some/file.txt", "100", "--color");
            fail();
        } catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
        }
    }

    public void testMissingOptionsWithExtraValues() {
        // Wrong arguments, should not be passed in as the two options
        // as they do not have "--key=name" and "--value=thx1138"
        try {
            final Cmd tail = commands.get("tail");
            tail.exec("name", "value");
            fail();
        } catch (IllegalArgumentException e) {

        }
    }

    public void testBooleanOptions() {
        final Cmd ls = commands.get("ls");
        ls.exec("--long=true", "/some/file.txt");
        ls.exec("--long", "/some/file.txt");
    }

    public void testFileParameter() {
        final Cmd touch = commands.get("touch");
        assertNotNull(touch);
        touch.exec("/some/file.txt");
    }

    public static class Commands {

        @Command
        public static void touch(File file) {
            assertEquals("/some/file.txt", file.getAbsolutePath());
        }

        @Command
        public static void set(@Option("key") String key, @Option("value") String value) {
            assertEquals("name", key);
            assertEquals("thx1138", value);
        }

        @Command
        public static void ls(@Option("long") Boolean longform, File file) {
            assertNotNull(longform);
            assertTrue(longform);

            assertEquals("/some/file.txt", file.getAbsolutePath());
        }

        @Command
        public static boolean booleanOption(@Option("long") boolean longform) {
            return longform;
        }

        @Command
        public static void tail(@Option("number") @Default("100") int number, File file, int expected) {
            assertEquals(expected, number);
            assertEquals("/some/file.txt", file.getAbsolutePath());
        }

        @Command
        public static void tar(@Option("x") File file) {
        }

        @Command
        public static void required(@Option("pass") @Required String pass) {
        }

        @Command
        public StreamingOutput cat(final File file) {
            Files.exists(file);
            Files.readable(file);
            Files.file(file);

            return new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException {
                    IO.copy(file, os);
                }
            };
        }
    }


}
