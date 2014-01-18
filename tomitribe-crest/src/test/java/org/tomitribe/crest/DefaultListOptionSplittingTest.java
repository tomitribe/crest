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

import java.util.Map;

/**
 * There is an order of precedence for splitting the list in an @Default value string
 *
 * Order is:
 *
 *  1. \u0000
 *  2. \t
 *  3. ,
 *
 * The first one found is the one used.
 *
 * List creation happens before variable substitution to ensure
 * splitting rules are not affected.
 *
 */
public class DefaultListOptionSplittingTest extends TestCase {

    private final Map<String, Cmd> commands = Cmd.get(Commands.class);

    public void testComma() throws Exception {
        commands.get("onComma").exec();
    }

    public void testTab() throws Exception {
        commands.get("onTab").exec();
    }

    public void testUnicode() throws Exception {
        commands.get("onUnicode").exec();
    }

    public void testOnlyBeforeSubstition() throws Exception {

        System.setProperty("value1", "2,3");
        System.setProperty("value2", "5,7");

        commands.get("onlyBeforeSubstition").exec();
    }

    public static class Commands {

        @Command
        public void onComma(@Option("value") @Default("2,3,5") String[] values) {
            assertNotNull(values);
            assertEquals(3, values.length);

            assertEquals("2", values[0]);
            assertEquals("3", values[1]);
            assertEquals("5", values[2]);
        }

        @Command
        public void onTab(@Option("value") @Default("2,3\t5,7\t11,13") String[] values) {
            assertNotNull(values);
            assertEquals(3, values.length);

            assertEquals("2,3", values[0]);
            assertEquals("5,7", values[1]);
            assertEquals("11,13", values[2]);
        }

        @Command
        public void onUnicode(@Option("value") @Default("2,3\t5,7\t11,13\u000017\t19,23\u000029") String[] values) {
            assertNotNull(values);
            assertEquals(3, values.length);

            assertEquals("2,3\t5,7\t11,13", values[0]);
            assertEquals("17\t19,23", values[1]);
            assertEquals("29", values[2]);
        }

        @Command
        public void onlyBeforeSubstition(@Option("value") @Default("${value1},${value2}") String[] values) {
            assertNotNull(values);
            assertEquals(2, values.length);

            assertEquals("2,3", values[0]);
            assertEquals("5,7", values[1]);
        }

    }
}
