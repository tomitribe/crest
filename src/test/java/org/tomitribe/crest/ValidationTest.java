/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;

import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class ValidationTest extends Assert {

    @Test
    public void duplicateOptions() {

        try {
            final Map<String, Cmd> map = Cmd.get(Duplicates.class);
            fail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class Duplicates {

        @Command
        public void color(@Option("red") String s, @Option("red") String s1) {

        }
    }
}
