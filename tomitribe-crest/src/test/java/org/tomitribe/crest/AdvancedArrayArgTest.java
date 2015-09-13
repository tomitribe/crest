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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;

import java.net.URI;
import java.util.Map;

public class AdvancedArrayArgTest extends TestCase {


    public static class OneExtra {

        @Command
        public Value copy(final URI[] sources, @Option("foo") final URI uri, final URI dest) {
            return new Value(sources, dest);
        }

        public static class Value {
            private final URI[] sources;
            private final URI dest;

            public Value(final URI[] sources, final URI dest) {
                this.sources = sources;
                this.dest = dest;
            }
        }
    }

    public void test() throws Exception {
        final Cmd copy = Commands.get(OneExtra.class).get("copy");

        {
            final OneExtra.Value exec = (OneExtra.Value) copy.exec(null, "/tmp/dest");
            assertEquals(0, exec.sources.length);
            assertEquals(URI.create("/tmp/dest"), exec.dest);
        }

        {
            final OneExtra.Value exec = (OneExtra.Value) copy.exec(null, "/tmp/src", "/tmp/dest");
            assertEquals(URI.create("/tmp/src"), exec.sources[0]);
            assertEquals(URI.create("/tmp/dest"), exec.dest);
        }

        {
            final OneExtra.Value exec = (OneExtra.Value) copy.exec(null, "/tmp/src1", "/tmp/src2", "/tmp/dest");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/dest"), exec.dest);
        }

        {
            final OneExtra.Value exec = (OneExtra.Value) copy.exec(null, "/tmp/src1", "/tmp/src2", "/tmp/src3", "/tmp/dest");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/src3"), exec.sources[2]);
            assertEquals(URI.create("/tmp/dest"), exec.dest);
        }
        {
            final OneExtra.Value exec = (OneExtra.Value) copy.exec(null, "/tmp/src1", "/tmp/src2", "/tmp/src3", "/tmp/src4", "/tmp/dest");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/src3"), exec.sources[2]);
            assertEquals(URI.create("/tmp/src4"), exec.sources[3]);
            assertEquals(URI.create("/tmp/dest"), exec.dest);
        }

        copy.exec(null, "/tmp/src");
    }

    public static class TwoExtra {

        @Command
        public Value copy(final URI[] sources, @Option("foo") final URI uri, final URI dest1, final URI dest2) {
            return new Value(sources, dest1, dest2);
        }

        public static class Value {
            private final URI[] sources;
            private final URI dest1;
            private final URI dest2;

            public Value(final URI[] sources, final URI dest1, final URI dest2) {
                this.sources = sources;
                this.dest1 = dest1;
                this.dest2 = dest2;
            }
        }
    }

    public void testTwoExtra() throws Exception {
        final Cmd copy = Commands.get(TwoExtra.class).get("copy");

        {
            final TwoExtra.Value exec = (TwoExtra.Value) copy.exec(null, "/tmp/dest1", "/tmp/dest2");
            assertEquals(0, exec.sources.length);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }

        {
            final TwoExtra.Value exec = (TwoExtra.Value) copy.exec(null, "/tmp/src", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src"), exec.sources[0]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }

        {
            final TwoExtra.Value exec = (TwoExtra.Value) copy.exec(null, "/tmp/src1", "/tmp/src2", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }

        {
            final TwoExtra.Value exec = (TwoExtra.Value) copy.exec(null, "/tmp/src1", "/tmp/src2", "/tmp/src3", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/src3"), exec.sources[2]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }
        {
            final TwoExtra.Value exec = (TwoExtra.Value) copy.exec(null, "/tmp/src1", "/tmp/src2", "/tmp/src3", "/tmp/src4", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/src3"), exec.sources[2]);
            assertEquals(URI.create("/tmp/src4"), exec.sources[3]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }
    }


    public static class RequiredList {

        @Command
        public void copy(@Required final URI[] sources, final URI dest) {
        }
    }

    public void testRequired() throws Exception {
        final Map<String, Cmd> commands = Commands.get(RequiredList.class);
        final Cmd copy = commands.get("copy");
        copy.exec(null, "one", "two");

        try {
            copy.exec(null, "one");
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }


}
