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
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.tomitribe.crest.api.Command;

/**
 * @version $Revision$ $Date$
 */
public class MainTest extends TestCase {

    public void test() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("green", main.exec("green"));

        try {
            // does not exist
            main.exec("foo");
            fail();
        } catch (final IllegalArgumentException e) {
        }

        try {
            // arg does not exist
            main.exec("blue", "foo");
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    public void testHelp() throws Exception {

        final Main main = new Main(Foo.class);
        final Cmd help = main.commands.get("help");

        final String ln = System.getProperty("line.separator");
        assertEquals("Commands: " + ln + "                       " + ln + "   blue                " + ln
                + "   green               " + ln + "   help                " + ln + "   red                 " + ln,
                help.exec());

    }

    public void testCompletionEmptyTab() throws Exception {
        final Main main = new Main(Foo.class);
        final Collection<String> candidates = main.complete("", 0);

        assertEquals(4, candidates.size());
        assertTrue(candidates.contains("red"));
        assertTrue(candidates.contains("green"));
        assertTrue(candidates.contains("blue"));
        assertTrue(candidates.contains("help"));
    }

    public void testCompletionPartialWord() throws Exception {
        final Main main = new Main(Foo.class);
        final Collection<String> candidates = main.complete("r", 1);

        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("red"));
    }

    public void testCompletionPartialWordCursorAtTheStart() throws Exception {
        final Main main = new Main(Foo.class);
        final Collection<String> candidates = main.complete("re", 0);

        assertEquals(4, candidates.size());
        assertTrue(candidates.contains("red"));
        assertTrue(candidates.contains("green"));
        assertTrue(candidates.contains("blue"));
        assertTrue(candidates.contains("help"));
    }

    public void testCompletionDelegatesToIndividualCmds() throws Exception {
        final Main main = new Main();
        final TestCmd cmd = new TestCmd();
        main.add(cmd);

        final Collection<String> candidates = main.complete("color ", 6);
        assertEquals(3, candidates.size());
        assertTrue(candidates.contains("red"));
        assertTrue(candidates.contains("green"));
        assertTrue(candidates.contains("blue"));

        assertEquals("", cmd.buffer);
        assertEquals(0, cmd.cursorPosition);
    }

    public static class TestCmd implements Cmd {
        public String buffer;
        public int cursorPosition;

        @Override
        public String getUsage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "color";
        }

        @Override
        public Object exec(String... rawArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void help(PrintStream out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> complete(String buffer, int cursorPosition) {
            this.buffer = buffer;
            this.cursorPosition = cursorPosition;
            return Arrays.asList(new String[] { "red", "green", "blue" });
        }
    }

    public static class Foo {

        @Command
        public String red() {
            return "red";
        }

        @Command
        public static String green() {
            return "green";
        }

        @Command
        public static void blue() {
        }
    }

}
