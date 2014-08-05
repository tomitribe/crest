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
import org.tomitribe.crest.cmds.Cmd;

import java.util.Map;

public class ExplicitPrimitiveOptionDefaultsTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

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

        private byte defaultByte = (byte) 42;
        private char defaultChar = 'D';
        private boolean defaultBoolean = true;
        private short defaultShort = 1024;
        private int defaultInt = 3301976;
        private long defaultLong = 10000000000l;
        private float defaultFloat = 10.0f;
        private double defaultDouble = 20.0f;


        @Command
        public void doByte(@Option("value") @Default("42") final byte value) {
            assertEquals(defaultByte, value);
        }

        @Command
        public void doChar(@Option("value") @Default("D") final char value) {
            assertEquals(defaultChar, value);
        }

        @Command
        public void doBoolean(@Option("value") @Default("true") final boolean value) {
            assertEquals(defaultBoolean, value);
        }

        @Command
        public void doShort(@Option("value") @Default("1024") final short value) {
            assertEquals(defaultShort, value);
        }

        @Command
        public void doInt(@Option("value") @Default("3301976") final int value) {
            assertEquals(defaultInt, value);
        }

        @Command
        public void doLong(@Option("value") @Default("10000000000") final long value) {
            assertEquals(defaultLong, value);
        }

        @Command
        public void doFloat(@Option("value") @Default("10.0") final float value) {
            assertEquals(defaultFloat, value);
        }

        @Command
        public void doDouble(@Option("value") @Default("20.0") final double value) {
            assertEquals(defaultDouble, value);
        }

        @Command
        public void doAll(
                @Option("byte") @Default("42") final byte byteValue,
                @Option("char") @Default("D") final char charValue,
                @Option("boolean") @Default("true") final boolean booleanValue,
                @Option("short") @Default("1024") final short shortValue,
                @Option("int") @Default("3301976") final int intValue,
                @Option("long") @Default("10000000000") final long longValue,
                @Option("float") @Default("10.0") final float floatValue,
                @Option("double") @Default("20.0") final double doubleValue
        ) {
            assertEquals(defaultByte, byteValue);
            assertEquals(defaultChar, charValue);
            assertEquals(defaultBoolean, booleanValue);
            assertEquals(defaultShort, shortValue);
            assertEquals(defaultInt, intValue);
            assertEquals(defaultLong, longValue);
            assertEquals(defaultFloat, floatValue);
            assertEquals(defaultDouble, doubleValue);
        }

    }
}
