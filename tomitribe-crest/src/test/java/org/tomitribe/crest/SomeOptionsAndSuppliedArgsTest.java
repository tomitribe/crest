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

public class SomeOptionsAndSuppliedArgsTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Commands.class);

    public void testByte() throws Exception {
        commands.get("doByte").exec("--p2=9", "3", "7");
    }


    public void testChar() throws Exception {
        commands.get("doChar").exec("--p2=9", "3", "7");
    }


    public void testBoolean() throws Exception {
        commands.get("doBoolean").exec("--p2=true", "false", "true");
    }


    public void testShort() throws Exception {
        commands.get("doShort").exec("--p2=9", "3", "7");
    }


    public void testInt() throws Exception {
        commands.get("doInt").exec("--p2=9", "3", "7");
    }


    public void testLong() throws Exception {
        commands.get("doLong").exec("--p2=9", "3", "7");
    }


    public void testFloat() throws Exception {
        commands.get("doFloat").exec("--p2=9", "3", "7");
    }


    public void testDouble() throws Exception {
        commands.get("doDouble").exec("--p2=9", "3", "7");
    }

    public void testString() throws Exception {
        commands.get("doString").exec("--p2=9", "3", "7");
    }

    public void testURI() throws Exception {
        commands.get("doURI").exec("--p2=9", "3", "7");
    }

    public void testMixed() throws Exception {
        commands.get("doMixed").exec("--p2=9", "3", "7");
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
        public void doByte(@Option("p1") byte parameter1, byte arg1, @Option("p2") byte parameter2, byte arg2) {
            assertEquals(uninitializedByte, parameter1);
            assertEquals(9, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doChar(@Option("p1") char parameter1, char arg1, @Option("p2") char parameter2, char arg2) {
            assertEquals(uninitializedChar, parameter1);
            assertEquals('9', parameter2);
            assertEquals('3', arg1);
            assertEquals('7', arg2);
        }

        @Command
        public void doBoolean(@Option("p1") boolean parameter1, boolean arg1, @Option("p2") boolean parameter2, boolean arg2) {
            assertEquals(uninitializedBoolean, parameter1);
            assertEquals(true, parameter2);
            assertEquals(false, arg1);
            assertEquals(true, arg2);
        }

        @Command
        public void doShort(@Option("p1") short parameter1, short arg1, @Option("p2") short parameter2, short arg2) {
            assertEquals(uninitializedShort, parameter1);
            assertEquals(9, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doInt(@Option("p1") int parameter1, int arg1, @Option("p2") int parameter2, int arg2) {
            assertEquals(uninitializedInt, parameter1);
            assertEquals(9, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doLong(@Option("p1") long parameter1, long arg1, @Option("p2") long parameter2, long arg2) {
            assertEquals(uninitializedLong, parameter1);
            assertEquals(9, parameter2);
            assertEquals(3, arg1);
            assertEquals(7, arg2);
        }

        @Command
        public void doFloat(@Option("p1") float parameter1, float arg1, @Option("p2") float parameter2, float arg2) {
            assertEquals(uninitializedFloat, parameter1);
            assertEquals(9f, parameter2);
            assertEquals(3f, arg1);
            assertEquals(7f, arg2);
        }

        @Command
        public void doDouble(@Option("p1") double parameter1, double arg1, @Option("p2") double parameter2, double arg2) {
            assertEquals(uninitializedDouble, parameter1);
            assertEquals(9d, parameter2);
            assertEquals(3d, arg1);
            assertEquals(7d, arg2);
        }

        @Command
        public void doString(@Option("p1") String parameter1, String arg1, @Option("p2") String parameter2, String arg2) {
            assertEquals(null, parameter1);
            assertEquals("9", parameter2);
            assertEquals("3", arg1);
            assertEquals("7", arg2);
        }

        @Command
        public void doURI(@Option("p1") URI parameter1, URI arg1, @Option("p2") URI parameter2, URI arg2) {
            assertEquals(null, parameter1);
            assertEquals(URI.create("9"), parameter2);
            assertEquals(URI.create("3"), arg1);
            assertEquals(URI.create("7"), arg2);
        }

        @Command
        public void doMixed(@Option("p1") String parameter1, URI arg1, @Option("p2") URI parameter2, String arg2) {
            assertEquals(null, parameter1);
            assertEquals(URI.create("9"), parameter2);
            assertEquals(URI.create("3"), arg1);
            assertEquals("7", arg2);
        }
    }
}
