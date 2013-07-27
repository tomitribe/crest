/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import junit.framework.TestCase;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.tomitribe.crest.api.Command;

/**
 * @version $Revision$ $Date$
 */
public class MainTest extends TestCase {

    public void test() throws Exception {

        final Main main = new Main(new ClassesArchive(Foo.class));

        try{
            main.exec("red");
            fail("Non-static should not be allowed in this case");
        }catch (IllegalStateException e) {
        }

        assertEquals("green", main.exec("green"));

        try {
            // does not exist
            main.exec("foo");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            // arg does not exist
            main.exec("blue", "foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }


    public static class Foo {

        @Command
        public String red() {
            return "red";
        }

        @Command
        public static String green() {
            return "green";
        }

        @Command
        public static void blue() {
        }
    }

}
