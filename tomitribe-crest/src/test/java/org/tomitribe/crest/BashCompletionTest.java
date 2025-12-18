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
import org.tomitribe.crest.api.GlobalOptions;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.val.Exists;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class BashCompletionTest {

    @Test
    public void happyPath() throws IOException {
        assertCompletion("foo", Foo.class);
    }

    @Test
    public void optionsBooleanImpliedDefault() throws IOException {
        assertCompletion("booleanoption", Copy.class);
    }

    @Test
    public void enums() throws IOException {
        assertCompletion("enums", Enums.class);
    }

    @Test
    public void defaults() throws IOException {
        assertCompletion("defaults", Defaults.class);
    }

    @Test
    public void overloaded() throws IOException {
        assertCompletion("overloaded", Overloaded.class);
    }

    @Test
    public void groups() throws IOException {
        assertCompletion("groups", Svn.class);
    }

    @Test
    public void globalFlags() throws IOException {
        assertCompletion("globalFlags", Svn.class, Bar.class);
    }

    private void assertCompletion(final String cmd, final Class<?>... clazzes) throws IOException {
        final URL resource = this.getClass().getClassLoader().getResource("completion/" + cmd + ".sh");
        Assert.assertNotNull(resource);
        final String expected = IO.slurp(resource);

        final Main main = new Main(clazzes);
        final String actual = BashCompletion.generate(main, cmd).replaceAll("\r\n", "\n");
        Assert.assertEquals(expected, actual);
    }

    public static class Foo {

        @Command
        public String red() {
            return "red";
        }

        @Command
        public static String green() {
            return "green";
        }

        @Command
        public static void blue() {
        }
    }

    public enum Shape {
        CIRCLE,
        SQUARE,
        TRIANGLE
    }

    public static class Enums {

        @Command
        public String red(@Option("time") final TimeUnit unit) {
            return "red";
        }

        @Command
        public static String green(@Option("time") final TimeUnit unit, @Option("shape") final Shape shape) {
            return "green";
        }

        @Command
        public static void blue() {
        }
    }

    public static class Defaults {

        @Command
        public String objects(@Option("oURI") final URI oURI,
                              @Option("oURL") final URL oURL,
                              @Option("oByte") final Byte oByte,
                              @Option("oCharacter") final Character oCharacter,
                              @Option("oShort") final Short oShort,
                              @Option("oInteger") final Integer oInteger,
                              @Option("oLong") final Long oLong,
                              @Option("oFloat") final Float oFloat,
                              @Option("oDouble") final Double oDouble) {
            return "red";
        }

        @Command
        public String primitives(@Option("oByte") final byte oByte,
                                 @Option("oBoolean") final boolean oBoolean,
                                 @Option("oCharacter") final char oCharacter,
                                 @Option("oShort") final short oShort,
                                 @Option("oInteger") final int oInteger,
                                 @Option("oLong") final long oLong,
                                 @Option("oFloat") final float oFloat,
                                 @Option("oDouble") final double oDouble) {
            return "red";
        }
    }

    public static class Copy {

        @Command
        public void copy(@Exists File source, @Exists File dest, @Option("force") boolean forceOverwrite) {
            throw new UnsupportedOperationException();
        }
    }

    @Command
    public static class Svn {

        @Command
        public StreamingOutput checkout(@Option("username") String username, @Option("password") String password, URI source) {
            throw new UnsupportedOperationException();
        }

        @Command
        public StreamingOutput commit(@Option("message") String message, @Option("password") String password, String[] paths) {
            throw new UnsupportedOperationException();
        }
    }

    @Command
    public static class Git {

        @Command
        public StreamingOutput push(@Option({"verbose", "v"}) String verbose, @Option("u") String upstream) {
            throw new UnsupportedOperationException();
        }


    }

    public static class Overloaded {
        @Command
        public StreamingOutput push(@Option({"verbose", "v"}) String verbose, @Option("u") String upstream) {
            throw new UnsupportedOperationException();
        }

        @Command
        public StreamingOutput push() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Dashes and colons mess with out completer
     * TODO: Fix the issue and write a test
     */
    @Command("revision-control")
    public static class Dashes {

        @Command("commit-code")
        public StreamingOutput commit(@Option("user-name") String username, @Option("pass-word") String password, String[] paths) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Dashes and colons mess with out completer
     * TODO: Fix the issue and write a test
     */
    @Command("revision_control")
    public static class Underscores {

        @Command("commit_code")
        public StreamingOutput commit(@Option("user_name") String username, @Option("pass_word") String password, String[] paths) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Dashes and colons mess with out completer
     * TODO: Fix the issue and write a test
     */
    @Command("revision:control")
    public static class Colons {

        @Command("commit:code")
        public StreamingOutput commit(@Option("user:name") String username, @Option("pass:word") String password, String[] paths) {
            throw new UnsupportedOperationException();
        }

    }

    @GlobalOptions
    public static class Bar {
        private final String orange;
        private final Boolean yellow;

        public Bar(@Option("orange") final String orange, @Option("yellow") final Boolean yellow) {
            this.orange = orange;
            this.yellow = yellow;
        }

        public String getOrange() {
            return orange;
        }

        public Boolean getYellow() {
            return yellow;
        }

        @Override
        public String toString() {
            return "Bar{" +
                    "orange='" + orange + '\'' +
                    ", yellow=" + yellow +
                    '}';
        }
    }
}