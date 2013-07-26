/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import junit.framework.TestCase;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class CmdTest extends TestCase {


    public void test() throws Exception {
        final Map<String, Cmd> commands = Cmd.get(Commands.class);

        {
            final Cmd touch = commands.get("touch");
            assertNotNull(touch);
            touch.exec("/some/file.txt");
        }

        {
            final Cmd ls = commands.get("ls");
            ls.exec("--long=true", "/some/file.txt");
            ls.exec("--long", "/some/file.txt");
        }

        {
            final Cmd tail = commands.get("tail");
            tail.exec("--number=45", "/some/file.txt", "45");
            tail.exec("/some/file.txt", "100");
        }
    }


    public static class Commands {

        @Command
        public static void touch(File file) {
            assertEquals("/some/file.txt", file.getAbsolutePath());
        }

        @Command
        public static void ls(@Option("long") Boolean longform, File file) {
            assertNotNull(longform);
            assertTrue(longform);

            assertEquals("/some/file.txt", file.getAbsolutePath());
        }

        @Command
        public static void tail(@Option("number") @Default("100") int number, File file, int expected) {
            assertEquals(expected, number);
            assertEquals("/some/file.txt", file.getAbsolutePath());
        }
    }
}
