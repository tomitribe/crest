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
        commands.get("doString").exec("--foo=2", "--foox=3", "-f=5");
    }

    /**
     * We must get this test to pass
     */
    public void testIllegal() throws Exception {
        final Cmd rsync = commands.get("rsync");

        // Short options cannot be long options
        try {
            rsync.exec("--d");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec("--e");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec("--l");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        // Long options cannot be short options
        try {
            rsync.exec("-del");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec("-delete-during");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec("-dirs");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec("-links");

            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            rsync.exec("-rsh");

            fail();
        } catch (IllegalArgumentException pass) {
        }

    }

    /**
     * Optional.  We don't have to support stringing short options together
     * as is allowed in posix.  Nice to have.  Could easily be done after 1.0.
     *
     * We do have to make the above Illegal, however or we'll never be able
     * support it without potentially breaking someone.  If we make
     * "-del" illegal we can add posix stringing support any time.
     */
    @Ignore
    public void testMixedLongAndShort() {
        final Cmd rsync = commands.get("rsync");

        assertCommand(
                rsync.exec(),
                "rsync{dirs=null, links=null, verbose=null, delete=null}");

        assertCommand(
                rsync.exec("-d", "-e", "-l"),
                "rsync{dirs=true, links=true, verbose=true, delete=null}");

        assertCommand(
                rsync.exec("-del"),
                "rsync{dirs=true, links=true, verbose=true, delete=null}");

        assertCommand(
                rsync.exec("--del"),
                "rsync{dirs=null, links=null, verbose=null, delete=true}");

        assertCommand(
                rsync.exec("--no-del"),
                "rsync{dirs=null, links=null, verbose=null, delete=false}");

        assertCommand(
                rsync.exec("-d", "-e", "-l", "--no-del"),
                "rsync{dirs=true, links=true, verbose=true, delete=false}");


        assertCommand(
                rsync.exec("-del", "--no-del"),
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
