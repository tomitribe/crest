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
import org.tomitribe.crest.api.Option;

import java.net.URI;
import java.util.Map;

public class DefaultOptionsAndSuppliedArgsTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Commands.class);

    public void testByte() throws Exception {
        commands.get("doByte").exec("3", "7");
    }


    public void testChar() throws Exception {
        commands.get("doChar").exec("3", "7");
    }


    public void testBoolean() throws Exception {
        commands.get("doBoolean").exec("false", "true");
    }


    public void testShort() throws Exception {
        commands.get("doShort").exec("3", "7");
    }


    public void testInt() throws Exception {
        commands.get("doInt").exec("3", "7");
    }


    public void testLong() throws Exception {
        commands.get("doLong").exec("3", "7");
    }


    public void testFloat() throws Exception {
        commands.get("doFloat").exec("3", "7");
    }


    public void testDouble() throws Exception {
        commands.get("doDouble").exec("3", "7");
    }

    public void testString() throws Exception {
        commands.get("doString").exec("3", "7");
    }

    public void testURI() throws Exception {
        commands.get("doURI").exec("3", "7");
    }

    public void testMixed() throws Exception {
        commands.get("doMixed").exec("3", "7");
    }

    public static class Commands {
        private byte uninitializedByte;
        private char uninitializedChar;
        private boolean uninitializedBoolean;
        private short uninitializedShort;
        private int uninitializedInt;
        private long uninitializedLong;
        private float uninitializedFloat;
        private double uninitializedDouble;

        @Command
        public void doByte(@Option("p1") final byte parameter1, final byte arg1, @Option("p2") final byte parameter2, final byte arg2) {
            assertEquals(uninitializedByte, parameter1);
            assertEquals(uninitializedByte, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doChar(@Option("p1") final char parameter1, final char arg1, @Option("p2") final char parameter2, final char arg2) {
            assertEquals(uninitializedChar, parameter1);
            assertEquals(uninitializedChar, parameter2);
            assertEquals('3', arg1);
            assertEquals('7', arg2);
        }

        @Command
        public void doBoolean(@Option("p1") final boolean parameter1, final boolean arg1, @Option("p2") final boolean parameter2, final boolean arg2) {
            assertEquals(uninitializedBoolean, parameter1);
            assertEquals(uninitializedBoolean, parameter2);
            assertEquals(false, arg1);
            assertEquals(true, arg2);
        }

        @Command
        public void doShort(@Option("p1") final short parameter1, final short arg1, @Option("p2") final short parameter2, final short arg2) {
            assertEquals(uninitializedShort, parameter1);
            assertEquals(uninitializedShort, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doInt(@Option("p1") final int parameter1, final int arg1, @Option("p2") final int parameter2, final int arg2) {
            assertEquals(uninitializedInt, parameter1);
            assertEquals(uninitializedInt, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doLong(@Option("p1") final long parameter1, final long arg1, @Option("p2") final long parameter2, final long arg2) {
            assertEquals(uninitializedLong, parameter1);
            assertEquals(uninitializedLong, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doFloat(@Option("p1") final float parameter1, final float arg1, @Option("p2") final float parameter2, final float arg2) {
            assertEquals(uninitializedFloat, parameter1);
            assertEquals(uninitializedFloat, parameter2);
            assertEquals(3f, arg1);
            assertEquals(7f, arg2);
        }

        @Command
        public void doDouble(@Option("p1") final double parameter1, final double arg1, @Option("p2") final double parameter2, final double arg2) {
            assertEquals(uninitializedDouble, parameter1);
            assertEquals(uninitializedDouble, parameter2);
            assertEquals(3d, arg1);
            assertEquals(7d, arg2);
        }

        @Command
        public void doString(@Option("p1") final String parameter1, final String arg1, @Option("p2") final String parameter2, final String arg2) {
            assertEquals(null, parameter1);
            assertEquals(null, parameter2);
            assertEquals("3", arg1);
            assertEquals("7", arg2);
        }

        @Command
        public void doURI(@Option("p1") final URI parameter1, final URI arg1, @Option("p2") final URI parameter2, final URI arg2) {
            assertEquals(null, parameter1);
            assertEquals(null, parameter2);
            assertEquals(URI.create("3"), arg1);
            assertEquals(URI.create("7"), arg2);
        }

        @Command
        public void doMixed(@Option("p1") final String parameter1, final URI arg1, @Option("p2") final URI parameter2, final String arg2) {
            assertEquals(null, parameter1);
            assertEquals(null, parameter2);
            assertEquals(URI.create("3"), arg1);
            assertEquals("7", arg2);
        }
    }
}
