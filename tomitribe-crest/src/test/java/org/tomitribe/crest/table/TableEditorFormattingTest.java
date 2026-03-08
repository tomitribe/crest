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
import org.tomitribe.crest.api.Editor;
import org.tomitribe.crest.api.table.Border;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.util.PrintString;

import java.beans.PropertyEditorSupport;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TableEditorFormattingTest extends Assert {

    @Test
    public void editorUsedForTableCellFormatting() throws Exception {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(EventCommands.class)
                .command(InstantEditor.class)
                .out(out)
                .build();

        main.run("events");

        final String result = out.toString();

        // The InstantEditor formats as "yyyy-MM-dd HH:mm" in UTC
        assertTrue("Should contain formatted date", result.contains("2025-03-08 14:30"));
        assertTrue("Should contain formatted date", result.contains("2025-06-15 09:00"));
        // Should NOT contain the default Instant.toString() format
        assertFalse("Should not contain ISO format", result.contains("2025-03-08T14:30:00Z"));
        assertFalse("Should not contain ISO format", result.contains("2025-06-15T09:00:00Z"));
    }

    @Test
    public void editorRegisteredViaLoad() throws Exception {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(EventCommands.class)
                .load(InstantEditor.class)
                .out(out)
                .build();

        main.run("events");

        final String result = out.toString();

        // The InstantEditor formats as "yyyy-MM-dd HH:mm" in UTC
        assertTrue("Should contain formatted date", result.contains("2025-03-08 14:30"));
        assertTrue("Should contain formatted date", result.contains("2025-06-15 09:00"));
        // Should NOT contain the default Instant.toString() format
        assertFalse("Should not contain ISO format", result.contains("2025-03-08T14:30:00Z"));
        assertFalse("Should not contain ISO format", result.contains("2025-06-15T09:00:00Z"));
    }

    @Test
    public void noEditorFallsBackToToString() throws Exception {
        // Without registering the editor, Instant.toString() should be used
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(EventCommands.class)
                .out(out)
                .build();

        main.run("events");

        final String result = out.toString();

        // Without the editor, default toString is used
        assertTrue("Should contain ISO format", result.contains("2025-03-08T14:30:00Z"));
    }

    public static class Event {
        private final String name;
        private final Instant timestamp;

        public Event(final String name, final Instant timestamp) {
            this.name = name;
            this.timestamp = timestamp;
        }

        public String getName() { return name; }
        public Instant getTimestamp() { return timestamp; }
    }

    public static class EventCommands {

        @Command
        @Table(fields = "name timestamp", sort = "name")
        public Stream<Event> events() {
            return Arrays.asList(
                    new Event("deploy", Instant.parse("2025-03-08T14:30:00Z")),
                    new Event("release", Instant.parse("2025-06-15T09:00:00Z"))
            ).stream();
        }
    }

    @Editor(Instant.class)
    public static class InstantEditor extends PropertyEditorSupport {
        private static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(ZoneOffset.UTC);

        @Override
        public void setAsText(final String text) throws IllegalArgumentException {
            setValue(Instant.parse(text));
        }

        @Override
        public String getAsText() {
            final Instant instant = (Instant) getValue();
            return instant != null ? FMT.format(instant) : "";
        }
    }
}
