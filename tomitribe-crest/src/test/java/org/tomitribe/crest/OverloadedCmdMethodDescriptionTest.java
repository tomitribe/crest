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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.util.PrintString;

import java.util.Map;

public class OverloadedCmdMethodDescriptionTest extends Assert {

    public static class OverloadedCommands {

        @Command
        public String process(@Option("file") final String file) {
            return "file:" + file;
        }

        @Command(description = "Process input data")
        public String process(@Option("file") final String file,
                              @Option("verbose") final boolean verbose) {
            return "file:" + file + " verbose:" + verbose;
        }

        @Command
        public String process(@Option("file") final String file,
                              @Option("verbose") final boolean verbose,
                              @Option("format") final String format) {
            return "file:" + file + " format:" + format;
        }
    }

    @Test
    public void descriptionFromAnyOverload() {
        final Map<String, Cmd> commands = Commands.get(OverloadedCommands.class);
        final Cmd cmd = commands.get("process");

        assertNotNull(cmd);
        assertEquals("Process input data", cmd.getDescription());
    }

    @Test
    public void overloadedDescriptionInListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(OverloadedCommands.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   help      %n" +
                "   process   Process input data%n"), out.toString());
    }
}
