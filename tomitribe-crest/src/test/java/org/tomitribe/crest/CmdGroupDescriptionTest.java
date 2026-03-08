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

public class CmdGroupDescriptionTest extends Assert {

    @Command(value = "config", description = "Manage configuration")
    public static class ConfigGroup {

        @Command(description = "Set a config value")
        public void set(@Option("key") final String key, @Option("value") final String value) { }

        @Command(description = "Get a config value")
        public void get(@Option("key") final String key) { }
    }

    @Test
    public void groupDescription() {
        final Map<String, Cmd> commands = Commands.get(ConfigGroup.class);
        final Cmd configGroup = commands.get("config");

        assertNotNull(configGroup);
        assertEquals("Manage configuration", configGroup.getDescription());
    }

    @Test
    public void groupDescriptionInListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigGroup.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   config   Manage configuration%n" +
                "   help     %n"), out.toString());
    }

    @Test
    public void subCommandDescriptionInListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigGroup.class)
                .out(out)
                .build();

        main.run("help", "config");

        assertEquals(String.format("Usage: config [subcommand] [options]%n" +
                "%n" +
                "Sub commands: %n" +
                "%n" +
                "   get   Get a config value%n" +
                "   set   Set a config value%n"), out.toString());
    }

    // --- CmdGroup merge: description from either class ---

    @Command("db")
    public static class DbCommandsNoDescription {

        @Command(description = "Run a query")
        public String query(@Option("sql") final String sql) {
            return sql;
        }
    }

    @Command(value = "db", description = "Database operations")
    public static class DbCommandsWithDescription {

        @Command(description = "Show database status")
        public String status() {
            return "ok";
        }
    }

    @Test
    public void mergedGroupDescriptionFromSecondClass() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(DbCommandsNoDescription.class)
                .command(DbCommandsWithDescription.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   db     Database operations%n" +
                "   help   %n"), out.toString());
    }

    @Test
    public void mergedGroupDescriptionFromFirstClass() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(DbCommandsWithDescription.class)
                .command(DbCommandsNoDescription.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   db     Database operations%n" +
                "   help   %n"), out.toString());
    }

    @Test
    public void mergedGroupSubCommandDescriptions() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(DbCommandsNoDescription.class)
                .command(DbCommandsWithDescription.class)
                .out(out)
                .build();

        main.run("help", "db");

        assertEquals(String.format("Usage: db [subcommand] [options]%n" +
                "%n" +
                "Sub commands: %n" +
                "%n" +
                "   query    Run a query%n" +
                "   status   Show database status%n"), out.toString());
    }
}
