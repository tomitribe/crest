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
import org.tomitribe.crest.api.validation.Validation;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.HelpPrintedException;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.val.BuiltInValidation;
import org.tomitribe.crest.val.Directory;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.singletonList;

/**
 * @version $Revision$ $Date$
 */
public class BeanValidationTest extends Assert {

    @Test
    public void invalidBuiltIn() {
        final Map<String, Cmd> cmds = Commands.get(BuiltIn.class);
        final Cmd check = cmds.get("check");
        check.exec(null, new File("").getAbsolutePath());
        try {
            check.exec(null, new File("/this/does/not/exist/we/hope").getAbsolutePath());
            fail();
        } catch (final HelpPrintedException e) {
            assertEquals(
                    singletonList("'/this/does/not/exist/we/hope' is not a directory"),
                    new BuiltInValidation(null, null)
                            .messages(e.getCause())
                            .orElseThrow(IllegalStateException::new));
        }
    }


    @Test
    public void invalidOptions() {
        final Map<String, Cmd> cmds = Commands.get(Duplicates.class);

        final Cmd check = cmds.get("check");

        check.exec(null, new File("").getAbsolutePath());

        try {
            check.exec(null, new File("/this/does/not/exist/we/hope").getAbsolutePath());
            fail();
        } catch (final HelpPrintedException e) {
            Assert.assertEquals(ConstraintViolationException.class, e.getCause().getClass());
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


    public static class BuiltIn {
        @Command
        public void check(@CrestDirectory final File dir) {
        }
    }

    @Target(PARAMETER)
    @Retention(RUNTIME)
    @Validation(CrestDirectory.Impl.class)
    public @interface CrestDirectory {
        class Impl implements Consumer<File> {
            @Override
            public void accept(final File file) {
                if (!file.isDirectory()) {
                    throw new IllegalStateException("'" + file + "' is not a directory");
                }
            }
        }
    }
}
