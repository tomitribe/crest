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

import java.util.Map;

public class ImplicitPrimitiveOptionDefaultsTest extends TestCase {

    private final Map<String, Cmd> commands = Cmd.get(Commands.class);

    public void testByte() throws Exception {
        commands.get("doByte").exec();
    }

    public void testChar() throws Exception {
        commands.get("doChar").exec();
    }

    public void testBoolean() throws Exception {
        commands.get("doBoolean").exec();
    }

    public void testShort() throws Exception {
        commands.get("doShort").exec();
    }

    public void testInt() throws Exception {
        commands.get("doInt").exec();
    }

    public void testLong() throws Exception {
        commands.get("doLong").exec();
    }

    public void testFloat() throws Exception {
        commands.get("doFloat").exec();
    }

    public void testDouble() throws Exception {
        commands.get("doDouble").exec();
    }

    public void testAll() throws Exception {
        commands.get("doAll").exec();
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
        public void doByte(@Option("value") byte value) {
            assertEquals(uninitializedByte, value);
        }

        @Command
        public void doChar(@Option("value") char value) {
            assertEquals(uninitializedChar, value);
        }

        @Command
        public void doBoolean(@Option("value") boolean value) {
            assertEquals(uninitializedBoolean, value);
        }

        @Command
        public void doShort(@Option("value") short value) {
            assertEquals(uninitializedShort, value);
        }

        @Command
        public void doInt(@Option("value") int value) {
            assertEquals(uninitializedInt, value);
        }

        @Command
        public void doLong(@Option("value") long value) {
            assertEquals(uninitializedLong, value);
        }

        @Command
        public void doFloat(@Option("value") float value) {
            assertEquals(uninitializedFloat, value);
        }

        @Command
        public void doDouble(@Option("value") double value) {
            assertEquals(uninitializedDouble, value);
        }

        @Command
        public void doAll(
                @Option("byte") byte byteValue,
                @Option("char") char charValue,
                @Option("boolean") boolean booleanValue,
                @Option("short") short shortValue,
                @Option("int") int intValue,
                @Option("long") long longValue,
                @Option("float") float floatValue,
                @Option("double") double doubleValue
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
