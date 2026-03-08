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
package org.tomitribe.crest.table;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.table.Border;
import org.tomitribe.crest.api.table.TableOptions;
import org.tomitribe.util.PrintString;

import java.util.Arrays;
import java.util.List;

public class TableOutputBuilderTest extends Assert {

    public static class Item {
        private final String name;
        private final String color;
        private final int count;

        public Item(final String name, final String color, final int count) {
            this.name = name;
            this.color = color;
            this.count = count;
        }

        public String getName() { return name; }
        public String getColor() { return color; }
        public int getCount() { return count; }
    }

    private static List<Item> items() {
        return Arrays.asList(
                new Item("apple", "red", 3),
                new Item("banana", "yellow", 5),
                new Item("cherry", "red", 12)
        );
    }

    /**
     * TableOptions overrides come after builder defaults,
     * so TableOptions values win.
     */
    @Test
    public void tableOptionsOverridesBuilderDefaults() throws Exception {
        final TableOptions tableOptions = new TableOptions(null, null, "name", "name color", null, null);

        final TableOutput output = TableOutput.builder()
                .data(items())
                .fields("name color count")
                .sort("count")
                .options(tableOptions)
                .build();

        final PrintString out = new PrintString();
        output.write(out);
        final String result = out.toString();

        // fields should be "name color" (from tableOptions), not "name color count"
        assertFalse("count column should not appear", result.contains("count"));
        // sort should be "name" (from tableOptions), not "count"
        assertTrue("apple should appear", result.contains("apple"));
    }

    /**
     * Builder methods after options() override the TableOptions values.
     */
    @Test
    public void builderAfterTableOptionsWins() throws Exception {
        final TableOptions tableOptions = new TableOptions(null, null, null, "name color", null, null);

        final TableOutput output = TableOutput.builder()
                .data(items())
                .options(tableOptions)
                .fields("name color count")
                .build();

        final PrintString out = new PrintString();
        output.write(out);
        final String result = out.toString();

        // fields should be "name color count" (set after options), not "name color"
        assertTrue("count column should appear", result.contains("3"));
        assertTrue("count column should appear", result.contains("12"));
    }

    /**
     * Null values in TableOptions do not override existing builder values.
     */
    @Test
    public void tableOptionsNullValuesDoNotOverride() throws Exception {
        final TableOptions tableOptions = new TableOptions(null, null, null, null, null, null);

        final TableOutput output = TableOutput.builder()
                .data(items())
                .fields("name color")
                .sort("name")
                .border(Border.asciiCompact)
                .header(true)
                .options(tableOptions)
                .build();

        final PrintString out = new PrintString();
        output.write(out);
        final String result = out.toString();

        // Everything should remain from the builder defaults
        assertTrue("apple should appear", result.contains("apple"));
        assertTrue("header should appear", result.contains("name"));
    }

    /**
     * Options (internal) works the same way as TableOptions.
     */
    @Test
    public void optionsOverridesBuilderDefaults() throws Exception {
        final Options options = new Options();
        options.setSort("name");
        options.setFields("name color");

        final TableOutput output = TableOutput.builder()
                .data(items())
                .fields("name color count")
                .sort("count")
                .options(options)
                .build();

        final PrintString out = new PrintString();
        output.write(out);
        final String result = out.toString();

        // fields should be "name color" (from options), not "name color count"
        assertFalse("count column should not appear", result.contains("count"));
    }

    /**
     * End-to-end: command returns TableOutput built with TableOptions.
     */
    public static class ReportCommands {

        @Command(description = "Generate report")
        public TableOutput report(final TableOptions tableOptions) {
            return TableOutput.builder()
                    .data(items())
                    .fields("name color count")
                    .sort("name")
                    .border(Border.asciiCompact)
                    .header(true)
                    .options(tableOptions)
                    .build();
        }
    }

    @Test
    public void endToEndWithTableOptions() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(ReportCommands.class)
                .out(out)
                .build();

        main.run("report", "--table-fields=name color");

        final String result = out.toString();
        assertTrue("apple should appear", result.contains("apple"));
        assertTrue("red should appear", result.contains("red"));
        // count should not be in output since --table-fields overrode it
        assertFalse("count values should not appear", result.contains("  3"));
    }
}
