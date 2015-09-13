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
import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManyOptionTest extends TestCase{
    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testString() throws Exception {
        commands.get("doString").exec(null, "--foo=2", "--foox=3", "-f=5");
    }

    public void testIllegal() throws Exception {
        final Cmd rsync = commands.get("rsync");

        // Short options cannot be long options
        try {
            rsync.exec(null, "--d");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec(null, "--e");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec(null, "--l");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        // Long options cannot be short options
        // -del is the same as -d -e -l (covered in the test below).
        
        try {
            rsync.exec(null, "-delete-during");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec(null, "-dirs");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec(null, "-links");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec(null, "-rsh");

            fail();
        } catch (IllegalArgumentException pass) {
        }
        
        try {
            rsync.exec(null, "-no-rsh");

            fail();
        } catch (IllegalArgumentException pass) {
        }


    }

    public void testMixedLongAndShort() {
        final Cmd rsync = commands.get("rsync");

        assertCommand(
                rsync.exec(null),
                "rsync{dirs=null, links=null, verbose=null, delete=null}");

        assertCommand(
                rsync.exec(null, "-d", "-e", "-l"),
                "rsync{dirs=true, links=true, verbose=true, delete=null}");

        assertCommand(
                rsync.exec(null, "-del"),
                "rsync{dirs=true, links=true, verbose=true, delete=null}");

        assertCommand(
                rsync.exec(null, "--del"),
                "rsync{dirs=null, links=null, verbose=null, delete=true}");

        assertCommand(
                rsync.exec(null, "--no-del"),
                "rsync{dirs=null, links=null, verbose=null, delete=false}");

        assertCommand(
                rsync.exec(null, "-d", "-e", "-l", "--no-del"),
                "rsync{dirs=true, links=true, verbose=true, delete=false}");


        assertCommand(
                rsync.exec(null, "-del", "--no-del"),
                "rsync{dirs=true, links=true, verbose=true, delete=false}");

    }
    private void assertCommand(Object result, String expected) {
        assertEquals(expected, result);
    }


    public static class Commands {

        @Command
        public void doString(@Option({"foo", "foox", "f"}) final List<String> list) {
            assertNotNull(list);

            final Iterator<String> it = list.iterator();
            assertEquals("2", it.next());
            assertEquals("3", it.next());
            assertEquals("5", it.next());
            assertFalse(it.hasNext());
        }

        @Command
        public String rsync(
                @Option({"d", "dirs"}) Boolean dirs,
                @Option({"l", "links"}) Boolean links,
                @Option({"e", "rsh"}) Boolean verbose,
                @Option({"del", "delete-during"}) Boolean delete) {

            return "rsync{" +
                    "dirs=" + dirs +
                    ", links=" + links +
                    ", verbose=" + verbose +
                    ", delete=" + delete +
                    '}';

        }
    }
}
