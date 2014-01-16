/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import junit.framework.TestCase;
import org.tomitribe.crest.api.Command;

import java.io.File;

/**
 * @version $Revision$ $Date$
 */
public class VarArgsTest extends TestCase {


    public void test() throws Exception {
        final Cmd touch = Cmd.get(Commands.class).get("ls");
        assertNotNull(touch);
        touch.exec("/some/orange.txt", "/some/green.txt", "/some/yellow.txt");
    }

    public static class Commands {

        @Command
        public static void ls(File... file) {
            assertEquals("/some/orange.txt", file[0].getAbsolutePath());
            assertEquals("/some/green.txt", file[1].getAbsolutePath());
            assertEquals("/some/yellow.txt", file[2].getAbsolutePath());
            assertEquals(3, file.length);
        }
    }
}
