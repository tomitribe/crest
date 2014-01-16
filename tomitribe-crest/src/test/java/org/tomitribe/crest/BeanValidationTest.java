/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import org.apache.bval.constraints.NotEmpty;
import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.val.Directory;
import org.tomitribe.crest.val.Writable;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class BeanValidationTest extends Assert {

    @Test
    public void invalidOptions() {
        final Map<String,Cmd> cmds = Cmd.get(Duplicates.class);

        final Cmd check = cmds.get("check");

        check.exec(new File("").getAbsolutePath());

        try {
            check.exec(new File("/this/does/not/exist/we/hope").getAbsolutePath());
            fail();
        } catch (ConstraintViolationException e) {
        }
    }


    public static class Duplicates {

        @Command
        public void check(@Directory File dir) {

        }

        @Command
        public void empty(@NotEmpty String path) {

        }
    }

}
