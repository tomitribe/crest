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

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ArgListsTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Commands.class);

    public void testByte() throws Exception {
        commands.get("doByte").exec("2", "3", "5");
    }

    public void testBoolean() throws Exception {
        commands.get("doBoolean").exec("false", "true", "false");
    }

    public void testCharacter() throws Exception {
        commands.get("doCharacter").exec("2", "3", "5");
    }

    public void testShort() throws Exception {
        commands.get("doShort").exec("2", "3", "5");
    }

    public void testInt() throws Exception {
        commands.get("doInt").exec("2", "3", "5");
    }

    public void testLong() throws Exception {
        commands.get("doLong").exec("2", "3", "5");
    }

    public void testFloat() throws Exception {
        commands.get("doFloat").exec("2", "3", "5");
    }

    public void testDouble() throws Exception {
        commands.get("doDouble").exec("2", "3", "5");
    }

    public void testString() throws Exception {
        commands.get("doString").exec("2", "3", "5");
    }

    public void testURI() throws Exception {
        commands.get("doURI").exec("2", "3", "5");
    }

    public static class Commands {

        @Command
        public void doByte(final List<Byte> list) {
            assertNotNull(list);

            final Iterator<Byte> it = list.iterator();
            assertEquals((Byte) (byte) 2, it.next());
            assertEquals((Byte) (byte) 3, it.next());
            assertEquals((Byte) (byte) 5, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doBoolean(final List<Boolean> list) {
            assertNotNull(list);

            final Iterator<Boolean> it = list.iterator();
            assertEquals(Boolean.FALSE, it.next());
            assertEquals(Boolean.TRUE, it.next());
            assertEquals(Boolean.FALSE, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doCharacter(final List<Character> list) {
            assertNotNull(list);

            final Iterator<Character> it = list.iterator();
            assertEquals((Character) '2', it.next());
            assertEquals((Character) '3', it.next());
            assertEquals((Character) '5', it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doShort(final List<Short> list) {
            assertNotNull(list);

            final Iterator<Short> it = list.iterator();
            assertEquals((Short) (short) 2, it.next());
            assertEquals((Short) (short) 3, it.next());
            assertEquals((Short) (short) 5, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doInt(final List<Integer> list) {
            assertNotNull(list);

            final Iterator<Integer> it = list.iterator();
            assertEquals((Integer) (int) 2, it.next());
            assertEquals((Integer) (int) 3, it.next());
            assertEquals((Integer) (int) 5, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doLong(final List<Long> list) {
            assertNotNull(list);

            final Iterator<Long> it = list.iterator();
            assertEquals((Long) (long) 2, it.next());
            assertEquals((Long) (long) 3, it.next());
            assertEquals((Long) (long) 5, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doFloat(final List<Float> list) {
            assertNotNull(list);

            final Iterator<Float> it = list.iterator();
            assertEquals((Float) (float) 2, it.next());
            assertEquals((Float) (float) 3, it.next());
            assertEquals((Float) (float) 5, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doDouble(final List<Double> list) {
            assertNotNull(list);

            final Iterator<Double> it = list.iterator();
            assertEquals((Double) (double) 2, it.next());
            assertEquals((Double) (double) 3, it.next());
            assertEquals((Double) (double) 5, it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doString(final List<String> list) {
            assertNotNull(list);

            final Iterator<String> it = list.iterator();
            assertEquals("2", it.next());
            assertEquals("3", it.next());
            assertEquals("5", it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public void doURI(final List<URI> list) {
            assertNotNull(list);

            final Iterator<URI> it = list.iterator();
            assertEquals(URI.create("2"), it.next());
            assertEquals(URI.create("3"), it.next());
            assertEquals(URI.create("5"), it.next());
            assertFalse(it.hasNext());
        }
    }
}
