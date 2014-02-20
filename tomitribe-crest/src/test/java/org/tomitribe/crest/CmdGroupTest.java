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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Join;

import java.util.Map;

public class CmdGroupTest extends Assert {


    private final Map<String, Cmd> commands = Commands.get(TestCommands.class);

    @Test
    public void testGetUsage() throws Exception {

        final Cmd help = commands.get("help");

        assertEquals(
                "help [options]\n" +
                        "help [options] String\n" +
                        "help [options] String String",
                help.getUsage());
    }

    @Test
    public void testGetName() throws Exception {
        final Cmd help = commands.get("help");

        assertEquals("help", help.getName());
    }

    @Test
    public void testExec() throws Exception {
        final Cmd help = commands.get("help");

        assertEquals("help1 42", help.exec("--level=42"));

        assertEquals("help2 en foo", help.exec("--lang=en", "foo"));
        assertEquals("help2 null foo", help.exec("foo"));

        assertEquals("help3 en false foo bar", help.exec("--lang=en", "foo", "bar"));
        assertEquals("help3 en true foo bar", help.exec("--lang=en", "--colors", "foo", "bar"));
    }

    public static class TestCommands {

        @Command
        public String help(@Option("level") final int i) {
            return "help1 " + Join.join(" ", i);
        }

        @Command
        public String help(@Option("lang") final String language, final String s) {
            return "help2 " + Join.join(" ", language, s);
        }

        @Command
        public String help(@Option("lang") final String language, @Option("colors") final boolean b, final String s, final String s2) {
            return "help3 " + Join.join(" ", language, b, s, s2);
        }
    }
}
