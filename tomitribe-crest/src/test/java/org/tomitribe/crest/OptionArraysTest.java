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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;

import java.net.URI;
import java.util.Map;

public class OptionArraysTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testByte() throws Exception {
        commands.get("doByte").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testBoolean() throws Exception {
        commands.get("doBoolean").exec("--foo=false", "--foo=true", "--foo=false");
    }

    public void testCharacter() throws Exception {
        commands.get("doChar").exec("--foo=D", "--foo=M", "--foo=B");
    }

    public void testShort() throws Exception {
        commands.get("doShort").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testInt() throws Exception {
        commands.get("doInt").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testLong() throws Exception {
        commands.get("doLong").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testFloat() throws Exception {
        commands.get("doFloat").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testDouble() throws Exception {
        commands.get("doDouble").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testString() throws Exception {
        commands.get("doString").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public void testURI() throws Exception {
        commands.get("doURI").exec("--foo=22", "--foo=33", "--foo=55");
    }

    public static class Commands {


        @Command
        public void doByte(@Option("foo") final byte[] array) {
            assertNotNull(array);

            assertEquals(22, array[0]);
            assertEquals(33, array[1]);
            assertEquals(55, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doBoolean(@Option("foo") final boolean[] array) {
            assertNotNull(array);

            assertEquals(false, array[0]);
            assertEquals(true, array[1]);
            assertEquals(false, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doChar(@Option("foo") final char[] array) {
            assertNotNull(array);

            assertEquals('D', array[0]);
            assertEquals('M', array[1]);
            assertEquals('B', array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doShort(@Option("foo") final short[] array) {
            assertNotNull(array);

            assertEquals(22, array[0]);
            assertEquals(33, array[1]);
            assertEquals(55, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doInt(@Option("foo") final int[] array) {
            assertNotNull(array);

            assertEquals(22, array[0]);
            assertEquals(33, array[1]);
            assertEquals(55, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doLong(@Option("foo") final long[] array) {
            assertNotNull(array);

            assertEquals(22, array[0]);
            assertEquals(33, array[1]);
            assertEquals(55, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doFloat(@Option("foo") final float[] array) {
            assertNotNull(array);

            assertEquals(22f, array[0]);
            assertEquals(33f, array[1]);
            assertEquals(55f, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doDouble(@Option("foo") final double[] array) {
            assertNotNull(array);

            assertEquals(22d, array[0]);
            assertEquals(33d, array[1]);
            assertEquals(55d, array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doString(@Option("foo") final String[] array) {
            assertNotNull(array);

            assertEquals("22", array[0]);
            assertEquals("33", array[1]);
            assertEquals("55", array[2]);
            assertEquals(3, array.length);
        }

        @Command
        public void doURI(@Option("foo") final URI[] array) {
            assertNotNull(array);

            assertEquals(URI.create("22"), array[0]);
            assertEquals(URI.create("33"), array[1]);
            assertEquals(URI.create("55"), array[2]);
            assertEquals(3, array.length);
        }
    }
}
