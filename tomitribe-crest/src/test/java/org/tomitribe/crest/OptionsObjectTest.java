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
import org.tomitribe.crest.cmds.Cmd;

import java.net.URI;
import java.util.Map;

public class OptionsObjectTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testBean() throws Exception {
        commands.get("options").exec(null, "--key=color", "--value=orange");
        commands.get("options").exec(null, "--value=orange", "--key=color");
    }

    public void testBeanWithArgs() throws Exception {
        commands.get("args").exec(null, "red", "blue://foo");
    }

    public void testOptionsAndArgs() throws Exception {
        commands.get("optionsAndArgs").exec(null, "--key=color", "--value=orange", "red", "blue://foo");
        commands.get("optionsAndArgs").exec(null, "--value=orange", "red", "blue://foo", "--key=color");
    }

    public void testTwoBeans() throws Exception {
        commands.get("twoBeans").exec(null, "--key=color", "--value=orange", "red", "blue://foo");
        commands.get("twoBeans").exec(null, "--key=color", "red", "--value=orange", "blue://foo");
    }

    public void testMixed() throws Exception {
        commands.get("mixed").exec(null, "--size=4", "red", "blue://foo", "CIRCLE", "--key=color", "--value=orange", "orange://fruit");
        commands.get("mixed").exec(null, "red", "blue://foo", "CIRCLE", "orange://fruit", "--key=color", "--value=orange", "--size=4");
        commands.get("mixed").exec(null, "red", "--value=orange", "--size=4", "--key=color", "blue://foo", "CIRCLE", "orange://fruit");
    }

    public static class Commands {

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
}
