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
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;

import java.util.Map;

public class BooleanOptionsTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void test() throws Exception {

        assertEquals(true, commands.get("primitive").exec("--action"));
        assertEquals(false, commands.get("primitive").exec("--no-action"));

        assertEquals(true, commands.get("primitive2").exec("--action"));
        assertEquals(false, commands.get("primitive2").exec("--no-action"));

        assertEquals(true, commands.get("primitive3").exec("--action"));
        assertEquals(false, commands.get("primitive3").exec("--no-action"));

        assertEquals(true, commands.get("object").exec("--action"));
        assertEquals(false, commands.get("object").exec("--no-action"));

        assertEquals(true, commands.get("object2").exec("--action"));
        assertEquals(false, commands.get("object2").exec("--no-action"));

        assertEquals(true, commands.get("object3").exec("--action"));
        assertEquals(false, commands.get("object3").exec("--no-action"));
    }

    public static class Commands {

        @Command
        public boolean primitive(@Option("action") @Default("true") final boolean action) {
            return action;
        }

        @Command
        public boolean primitive2(@Option("action") @Default("false") final boolean action) {
            return action;
        }

        @Command
        public boolean primitive3(@Option("action") final boolean action) {
            return action;
        }

        @Command
        public Boolean object(@Option("action") @Default("true") final Boolean action) {
            return action;
        }

        @Command
        public Boolean object2(@Option("action") @Default("false") final Boolean action) {
            return action;
        }

        @Command
        public Boolean object3(@Option("action") final Boolean action) {
            return action;
        }

    }
}
