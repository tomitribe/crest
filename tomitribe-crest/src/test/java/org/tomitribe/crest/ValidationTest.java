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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;

import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class ValidationTest extends Assert {

    @Test
    public void duplicateOptions() {
        try {
            final Map<String, Cmd> map = Commands.get(Duplicates.class);
            fail();
        } catch (final Exception e) {
        }
    }

    @Test
    public void defaultOnArg() {

        try {
            final Map<String, Cmd> map = Commands.get(DefaultUse.class);
            fail();
        } catch (final Exception e) {
        }
    }


    public static class Duplicates {

        @Command
        public void color(@Option("red") final String s, @Option("red") final String s1) {

        }
    }

    public static class DefaultUse {

        @Command
        public void color(@Default("red") final String s1) {

        }
    }
}
