/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;

/**
 * @version $Revision$ $Date$
 */
public class SystemPropertiesTest extends Assert {

    @Test
    public void dashD() throws Exception {

        final Main main = new Main(new ClassesArchive(Orange.class));

        assertEquals("round", main.exec("property", "-Dshape=round", "shape"));
    }

    @Test
    public void systemPropertyDefaults() throws Exception {

        final Main main = new Main(new ClassesArchive(Orange.class));

        final String user = System.getProperty("user.name");
        final String home = System.getProperty("user.dir");

        assertEquals(String.format("%s - %s", "joe", "/tmp/cool"), main.exec("defaults", "--user=joe", "--home=/tmp/cool"));
        assertEquals(String.format("%s - %s", user, home), main.exec("defaults"));
    }

    public static class Orange {

        @Command
        public String property(String name) {
            return System.getProperty(name);
        }

        @Command
        public String defaults(@Option("user") @Default("${user.name}") String user,
                               @Option("home") @Default("${user.dir}") File home) {
            return String.format("%s - %s", user, home.getAbsolutePath());
        }
    }
}
