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

public class CmdMethodDescriptionTest extends Assert {

    public static class AnnotatedDescriptions {

        @Command(description = "Add a new item")
        public void add(@Option("name") final String name) { }

        @Command(description = "Remove an existing item")
        public void remove(@Option("name") final String name) { }

        @Command
        public void list() { }
    }

    @Test
    public void annotationDescription() {
        final Map<String, Cmd> commands = Commands.get(AnnotatedDescriptions.class);

        assertEquals("Add a new item", commands.get("add").getDescription());
        assertEquals("Remove an existing item", commands.get("remove").getDescription());
    }

    @Test
    public void noAnnotationDescriptionReturnsNull() {
        final Map<String, Cmd> commands = Commands.get(AnnotatedDescriptions.class);

        assertNull(commands.get("list").getDescription());
    }

    @Test
    public void annotationDescriptionInListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(AnnotatedDescriptions.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   add      Add a new item%n" +
                "   help     %n" +
                "   list     %n" +
                "   remove   Remove an existing item%n"), out.toString());
    }

    // --- javadoc as description fallback ---

    public static class JavadocDescriptions {

        /**
         * Deploy the application to the server.
         */
        @Command
        public void deploy(@Option("target") final String target) { }

        /**
         * Compile source files into bytecode. This is the second sentence.
         */
        @Command
        public void compile(@Option("source") final String source) { }

        /**
         * This javadoc should be ignored.
         */
        @Command(description = "Start the server")
        public void start() { }

        @Command
        public void stop() { }
    }

    @Test
    public void javadocAsDescription() {
        final Map<String, Cmd> commands = Commands.get(JavadocDescriptions.class);

        assertEquals("Deploy the application to the server.", commands.get("deploy").getDescription());
    }

    @Test
    public void javadocFirstSentenceOnly() {
        final Map<String, Cmd> commands = Commands.get(JavadocDescriptions.class);

        assertEquals("Compile source files into bytecode", commands.get("compile").getDescription());
    }

    @Test
    public void annotationTakesPriorityOverJavadoc() {
        final Map<String, Cmd> commands = Commands.get(JavadocDescriptions.class);

        assertEquals("Start the server", commands.get("start").getDescription());
    }

    @Test
    public void noJavadocNoAnnotationReturnsNull() {
        final Map<String, Cmd> commands = Commands.get(JavadocDescriptions.class);

        assertNull(commands.get("stop").getDescription());
    }

    @Test
    public void javadocDescriptionInListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(JavadocDescriptions.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   compile   Compile source files into bytecode%n" +
                "   deploy    Deploy the application to the server.%n" +
                "   help      %n" +
                "   start     Start the server%n" +
                "   stop      %n"), out.toString());
    }

    // --- no description at all ---

    public static class NoDescriptions {

        @Command
        public void deploy() { }
    }

    @Test
    public void noDescriptionReturnsNull() {
        final Map<String, Cmd> commands = Commands.get(NoDescriptions.class);

        assertNull(commands.get("deploy").getDescription());
    }

    @Test
    public void noDescriptionInListing() {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(NoDescriptions.class)
                .out(out)
                .build();

        main.run("help");

        assertEquals(String.format("Commands: %n" +
                "%n" +
                "   deploy   %n" +
                "   help     %n"), out.toString());
    }
}
