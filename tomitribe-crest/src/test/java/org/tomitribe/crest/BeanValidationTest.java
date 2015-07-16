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

import org.apache.bval.constraints.NotEmpty;
import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.val.Directory;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class BeanValidationTest extends Assert {

    @Test
    public void invalidOptions() {
        final Map<String, Cmd> cmds = Commands.get(Duplicates.class);

        final Cmd check = cmds.get("check");

        check.exec(new File("").getAbsolutePath());

        try {
            check.exec(new File("/this/does/not/exist/we/hope").getAbsolutePath());
            fail();
        } catch (final ConstraintViolationException e) {
            // no-op
        }
    }


    public static class Duplicates {

        @Command
        public void check(@Directory final File dir) {

        }

        @Command
        public void empty(@NotEmpty final String path) {

        }
    }

}
