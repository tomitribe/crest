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

import java.util.Map;

public class ImplicitPrimitiveOptionDefaultsTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testByte() throws Exception {
        commands.get("doByte").exec(null);
    }

    public void testChar() throws Exception {
        commands.get("doChar").exec(null);
    }

    public void testBoolean() throws Exception {
        commands.get("doBoolean").exec(null);
    }

    public void testShort() throws Exception {
        commands.get("doShort").exec(null);
    }

    public void testInt() throws Exception {
        commands.get("doInt").exec(null);
    }

    public void testLong() throws Exception {
        commands.get("doLong").exec(null);
    }

    public void testFloat() throws Exception {
        commands.get("doFloat").exec(null);
    }

    public void testDouble() throws Exception {
        commands.get("doDouble").exec(null);
    }

    public void testAll() throws Exception {
        commands.get("doAll").exec(null);
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
        public void doByte(@Option("value") final byte value) {
            assertEquals(uninitializedByte, value);
        }

        @Command
        public void doChar(@Option("value") final char value) {
            assertEquals(uninitializedChar, value);
        }

        @Command
        public void doBoolean(@Option("value") final boolean value) {
            assertEquals(uninitializedBoolean, value);
        }

        @Command
        public void doShort(@Option("value") final short value) {
            assertEquals(uninitializedShort, value);
        }

        @Command
        public void doInt(@Option("value") final int value) {
            assertEquals(uninitializedInt, value);
        }

        @Command
        public void doLong(@Option("value") final long value) {
            assertEquals(uninitializedLong, value);
        }

        @Command
        public void doFloat(@Option("value") final float value) {
            assertEquals(uninitializedFloat, value);
        }

        @Command
        public void doDouble(@Option("value") final double value) {
            assertEquals(uninitializedDouble, value);
        }

        @Command
        public void doAll(
                @Option("byte") final byte byteValue,
                @Option("char") final char charValue,
                @Option("boolean") final boolean booleanValue,
                @Option("short") final short shortValue,
                @Option("int") final int intValue,
                @Option("long") final long longValue,
                @Option("float") final float floatValue,
                @Option("double") final double doubleValue
        ) {
            assertEquals(uninitializedByte, byteValue);
            assertEquals(uninitializedChar, charValue);
            assertEquals(uninitializedBoolean, booleanValue);
            assertEquals(uninitializedShort, shortValue);
            assertEquals(uninitializedInt, intValue);
            assertEquals(uninitializedLong, longValue);
            assertEquals(uninitializedFloat, floatValue);
            assertEquals(uninitializedDouble, doubleValue);
        }
    }
}
