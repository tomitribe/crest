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
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.val.Exists;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class BashCompletionTest {

    @Test
    public void generate() throws IOException {

        assertCompletion("foo", Foo.class);
    }

    private void assertCompletion(final String cmd, final Class<?>... clazzes) throws IOException {
        final URL resource = this.getClass().getClassLoader().getResource("completion/" + cmd + ".sh");
        Assert.assertNotNull(resource);
        final String expected = IO.slurp(resource);

        final Main main = new Main(clazzes);
        Assert.assertEquals(expected, BashCompletion.generate(main, cmd));
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
        public StreamingOutput commit(@Option("username") String username, @Option("password") String password, String[] paths) {
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
}