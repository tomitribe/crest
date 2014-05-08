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
import org.tomitribe.crest.api.Options;

import java.net.URI;
import java.util.Map;

public class OptionsObjectAdvancedArrayArgTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Commands.class);

    public void testSimple() throws Exception {
        assertArguments(commands.get("simple"));
    }

    public void testComplex() throws Exception {
        assertArguments(commands.get("complex"));
    }

    public void testComplex2() throws Exception {
        assertArguments(commands.get("complex2"));
    }

    public void testComplex3() throws Exception {
        assertArguments(commands.get("complex3"));
    }

    public void testComplex4() throws Exception {
        assertArguments(commands.get("complex4"));
    }

    public void testComplex5() throws Exception {
        assertArguments(commands.get("complex5"));
    }

    private void assertArguments(Cmd command) {
        {
            final Value exec = (Value) command.exec("/tmp/dest1", "/tmp/dest2");
            assertEquals(0, exec.sources.length);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }

        {
            final Value exec = (Value) command.exec("/tmp/src", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src"), exec.sources[0]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }

        {
            final Value exec = (Value) command.exec("/tmp/src1", "/tmp/src2", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }

        {
            final Value exec = (Value) command.exec("/tmp/src1", "/tmp/src2", "/tmp/src3", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/src3"), exec.sources[2]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }
        {
            final Value exec = (Value) command.exec("/tmp/src1", "/tmp/src2", "/tmp/src3", "/tmp/src4", "/tmp/dest1", "/tmp/dest2");
            assertEquals(URI.create("/tmp/src1"), exec.sources[0]);
            assertEquals(URI.create("/tmp/src2"), exec.sources[1]);
            assertEquals(URI.create("/tmp/src3"), exec.sources[2]);
            assertEquals(URI.create("/tmp/src4"), exec.sources[3]);
            assertEquals(URI.create("/tmp/dest1"), exec.dest1);
            assertEquals(URI.create("/tmp/dest2"), exec.dest2);
        }
    }

    public static class Commands {

        @Command
        public Value simple(final URI[] sources, @Option("foo") final URI uri, final URI dest1, final URI dest2) {
            return new Value(sources, dest1, dest2);
        }

        @Command
        public Value complex(Sources sources, @Option("foo") final URI uri, final URI dest1, final URI dest2) {
            return new Value(sources.sources, dest1, dest2);
        }

        @Command
        public Value complex2(Sources sources, @Option("foo") final URI uri, final Dest dest1, final URI dest2) {
            return new Value(sources.sources, dest1.dest, dest2);
        }

        @Command
        public Value complex3(Sources sources, @Option("foo") final URI uri, final Dest dest1, final Dest dest2) {
            return new Value(sources.sources, dest1.dest, dest2.dest);
        }

        @Command
        public Value complex4(Sources sources, @Option("foo") final URI uri, final URI dest1, final Dest dest2) {
            return new Value(sources.sources, dest1, dest2.dest);
        }

        @Command
        public Value complex5(final URI[] sources, @Option("foo") final URI uri, final Dest dest1, final Dest dest2) {
            return new Value(sources, dest1.dest, dest2.dest);
        }

        @Options
        public static class Sources {
            final URI[] sources;

            public Sources(final URI[] sources) {
                this.sources = sources;
            }
        }

        @Options
        public static class Dest {
            final URI dest;

            public Dest(URI dest) {
                this.dest = dest;
            }
        }

        @Command
        public void options(Bean bean) {
            assertNotNull(bean);

            assertEquals("color", bean.getKey());
            assertEquals("orange", bean.getValue());
        }

        @Command
        public void args(BeanWithArgs bean) {
            assertNotNull(bean);

            assertEquals("red", bean.getString());
            assertEquals(URI.create("blue://foo"), bean.getUri());
        }


        @Command
        public void optionsAndArgs(BeanWithArgsAndOptions bean) {
            assertNotNull(bean);

            assertEquals("color", bean.getKey());
            assertEquals("orange", bean.getValue());
            assertEquals("red", bean.getString());
            assertEquals(URI.create("blue://foo"), bean.getUri());
        }


        @Command
        public void twoBeans(Bean bean, BeanWithArgs beanWithArgs) {
            options(bean);
            args(beanWithArgs);
        }

        @Command
        public void mixed(@Option("size") int size, BeanWithArgs beanWithArgs, Shape shape, Bean bean, URI uri) {
            options(bean);
            args(beanWithArgs);

            assertEquals(Shape.CIRCLE, shape);
            assertEquals(4, size);
            assertEquals(URI.create("orange://fruit"), uri);
        }
    }


    public static enum Shape {
        SQUARE,
        CIRCLE,
        TRIANGLE;
    }

    @Options
    public static class Bean {

        private final String key;
        private final String value;

        public Bean(@Option("key") final String key, @Option("value") final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    @Options
    public static class BeanWithArgs {

        private final String string;
        private final URI uri;

        public BeanWithArgs(String string, URI uri) {
            this.string = string;
            this.uri = uri;
        }

        public String getString() {
            return string;
        }

        public URI getUri() {
            return uri;
        }
    }

    @Options
    public static class BeanWithArgsAndOptions {

        private final String key;
        private final String value;
        private final String string;
        private final URI uri;

        public BeanWithArgsAndOptions(@Option("key") final String key, @Option("value") final String value, String string, URI uri) {
            this.key = key;
            this.value = value;
            this.string = string;
            this.uri = uri;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getString() {
            return string;
        }

        public URI getUri() {
            return uri;
        }
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
