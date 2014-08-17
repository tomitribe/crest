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

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.val.Exists;

import junit.framework.TestCase;

public class CompleterTest extends TestCase {

    public void testCompletionEmptyTab() throws Exception {
        final Main main = new Main(Foo.class);
        final Collection<String> candidates = main.complete("", 0);

        assertEquals(4, candidates.size());
        assertTrue(candidates.contains("red"));
        assertTrue(candidates.contains("green"));
        assertTrue(candidates.contains("blue"));
        assertTrue(candidates.contains("help"));
    }

    public void testCompletionPartialWord() throws Exception {
        final Main main = new Main(Foo.class);
        final Collection<String> candidates = main.complete("r", 1);

        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("red"));
    }

    public void testCompletionPartialWordCursorAtTheStart() throws Exception {
        final Main main = new Main(Foo.class);
        final Collection<String> candidates = main.complete("re", 0);

        assertEquals(4, candidates.size());
        assertTrue(candidates.contains("red"));
        assertTrue(candidates.contains("green"));
        assertTrue(candidates.contains("blue"));
        assertTrue(candidates.contains("help"));
    }

    public void testCompletionDelegatesToIndividualCmds() throws Exception {
        final Main main = new Main();
        final TestCmd cmd = new TestCmd();
        main.add(cmd);

        final Collection<String> candidates = main.complete("color ", 6);
        assertEquals(3, candidates.size());
        assertTrue(candidates.contains("red"));
        assertTrue(candidates.contains("green"));
        assertTrue(candidates.contains("blue"));

        assertEquals("color ", cmd.buffer);
        assertEquals(6, cmd.cursorPosition);
    }
    
    public void testCompleteSubcommands() throws Exception {
        final Main main = new Main(Svn.class);
        
        Collection<String> candidates = main.complete("svn ", 4);
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("checkout"));
        assertTrue(candidates.contains("commit"));
        
        candidates = main.complete("svn c", 5);
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("checkout"));
        assertTrue(candidates.contains("commit"));

        candidates = main.complete("svn co", 6);
        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("commit"));
    }

    public void testCompleteOptions() throws Exception {
        final Main main = new Main(Svn.class, Copy.class, Git.class);
        
        Collection<String> candidates = main.complete("svn checkout --", 15);
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("--username"));
        assertTrue(candidates.contains("--password"));
        
        candidates = main.complete("svn checkout --user=test", 19);
        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("--username"));

        candidates = main.complete("svn checkout --user=test", 20);
        assertEquals(0, candidates.size());
        
        candidates = main.complete("svn checkout --user=test", 15);
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("--username"));
        assertTrue(candidates.contains("--password"));


        Collection<String> gitCandidates = main.complete("git push --", 11);
        assertEquals(1, gitCandidates.size());
        assertTrue(gitCandidates.contains("--verbose"));

        gitCandidates = main.complete("git push --verb=test", 15);
        assertEquals(1, gitCandidates.size());
        assertTrue(gitCandidates.contains("--verbose"));

        gitCandidates = main.complete("git push -", 10);
        assertEquals(3, gitCandidates.size());
        assertTrue(gitCandidates.contains("-v"));
        assertTrue(gitCandidates.contains("-u"));
        assertTrue(gitCandidates.contains("--verbose"));

        gitCandidates = main.complete("git push -v=test", 10);
        assertEquals(3, gitCandidates.size());
        assertTrue(gitCandidates.contains("-v"));
        assertTrue(gitCandidates.contains("-u"));
        assertTrue(gitCandidates.contains("--verbose"));

        gitCandidates = main.complete("git push -v=test", 11);
        assertEquals(2, gitCandidates.size());
        assertTrue(gitCandidates.contains("-v"));
        assertTrue(gitCandidates.contains("--verbose"));

    }
    
    public void testCompleteFile() throws Exception {
        final Main main = new Main(Svn.class, Copy.class);
        
        /*Collection<String> candidates = main.complete("copy src/main src/m", 19);
        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("src/main"));
        
        candidates = main.complete("copy src/main src/m ", 20);
        assertEquals(0, candidates.size());
        
        candidates = main.complete("copy s", 6);
        assertTrue(candidates.contains("src"));
        
        candidates = main.complete("copy src/main src/m --f", 23);
        assertEquals(1, candidates.size());
        assertTrue(candidates.contains("--force"));
        
        candidates = main.complete("copy src/main src/main ", 23);
        assertEquals(0, candidates.size());*/
    }
    
    public static class TestCmd implements Cmd {
        public String buffer;
        public int cursorPosition;

        @Override
        public String getUsage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "color";
        }

        @Override
        public Object exec(String... rawArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void help(PrintStream out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> complete(String buffer, int cursorPosition) {
            this.buffer = buffer;
            this.cursorPosition = cursorPosition;
            return Arrays.asList(new String[] { "red", "green", "blue" });
        }
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
}
