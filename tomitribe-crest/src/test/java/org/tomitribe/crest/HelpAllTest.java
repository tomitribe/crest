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
import org.tomitribe.util.PrintString;

public class HelpAllTest extends Assert {

    @Command(value = "config", description = "Manage configuration")
    public static class ConfigCommands {

        @Command(description = "Set a config value")
        public String set(@Option("key") final String key,
                          @Option("value") final String value) {
            return "set:" + key + ":" + value;
        }

        @Command(description = "Get a config value")
        public String get(@Option("key") final String key) {
            return "get:" + key;
        }
    }

    @Command(value = "quote", description = "Manage quotes")
    public static class QuoteCommands {

        @Command(description = "Create a quote")
        public String create(@Option("name") final String name) {
            return "create:" + name;
        }

        @Command(description = "Remove a quote")
        public String remove(@Option("id") final String id) {
            return "remove:" + id;
        }
    }

    @Command(value = "quote line-item", description = "Manage line items")
    public static class QuoteLineItemCommands {

        @Command(description = "Add a line item")
        public String add(@Option("product") final String product) {
            return "add:" + product;
        }

        @Command(description = "Delete a line item")
        public String delete(@Option("id") final String id) {
            return "delete:" + id;
        }
    }

    @Test
    public void rootRecursiveListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigCommands.class)
                .command(QuoteCommands.class)
                .command(QuoteLineItemCommands.class)
                .out(out)
                .build();

        main.run("help", "--all");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   config get               Get a config value   %n" +
                "   config set               Set a config value   %n" +
                "   quote create             Create a quote       %n" +
                "   quote line-item add      Add a line item      %n" +
                "   quote line-item delete   Delete a line item   %n" +
                "   quote remove             Remove a quote       %n" +
                "%n" +
                "Help: %n" +
                "%n" +
                "   help <command>   Show detailed help for a command%n"), out.toString());
    }

    @Test
    public void subtreeRecursiveListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigCommands.class)
                .command(QuoteCommands.class)
                .command(QuoteLineItemCommands.class)
                .out(out)
                .build();

        main.run("help", "quote", "--all");

        assertEquals(String.format(
                "Sub-commands:%n" +
                        "%n" +
                        "   quote create             Create a quote       %n" +
                        "   quote line-item add      Add a line item      %n" +
                        "   quote line-item delete   Delete a line item   %n" +
                        "   quote remove             Remove a quote       %n" +
                        "%n" +
                        "Help: %n" +
                        "%n" +
                        "   help <command>   Show detailed help for a command%n"), out.toString());
    }

    @Test
    public void deepSubtreeRecursiveListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigCommands.class)
                .command(QuoteCommands.class)
                .command(QuoteLineItemCommands.class)
                .out(out)
                .build();

        main.run("help", "quote", "line-item", "--all");

        assertEquals(String.format(
                "Sub-commands:%n" +
                        "%n" +
                        "   quote line-item add      Add a line item      %n" +
                        "   quote line-item delete   Delete a line item   %n" +
                        "%n" +
                        "Help: %n" +
                        "%n" +
                        "   help <command>   Show detailed help for a command%n"), out.toString());
    }

    @Test
    public void noAllStillWorks() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ConfigCommands.class)
                .command(QuoteCommands.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   config   Manage configuration   %n" +
                "   help                            %n" +
                "   quote    Manage quotes          %n" +
                "%n" +
                "Help: %n" +
                "%n" +
                "   help --all       List all commands recursively%n" +
                "   help <command>   Show detailed help for a command%n"), out.toString());
    }

    @Test
    public void allOnLeafShowsManual() {
        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final Main main = Main.builder()
                .command(ConfigCommands.class)
                .out(out)
                .err(err)
                .build();

        main.run("help", "config", "set", "--all");

        // Graceful degradation: --all on a leaf shows the normal help
        assertTrue("Expected usage output, got: " + out.toString(),
                out.toString().contains("Usage:"));
    }

    @Test
    public void deepArbitraryNesting() {
        // Verify varargs handles depth beyond the previous 3-level limit
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(QuoteLineItemCommands.class)
                .out(out)
                .build();

        main.run("help", "quote", "line-item", "add");

        assertTrue("Expected manual for 'add', got: " + out.toString(),
                out.toString().contains("Usage:"));
        assertTrue("Expected --product option in output, got: " + out.toString(),
                out.toString().contains("--product"));
    }
}
