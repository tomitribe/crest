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
import org.tomitribe.crest.api.GlobalOptions;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.util.Join;

/**
 * @version $Revision$ $Date$
 */
public class GlobalOptionsTest extends TestCase {

    public void test() throws Exception {

        final Main main = new Main(Bar.class, Foo.class);

        assertEquals("green Bar{orange='null', yellow=true}", main.exec("--yellow", "green"));
    }

    public void test2() throws Exception {

        final Main main = new Main(Bar.class, Foo.class);

        assertEquals("green Bar{orange='null', yellow=null}", main.exec("green"));
    }

    public void testHelp() throws Exception {

        final Main main = new Main(Foo.class, Bar.class);
        final Cmd help = main.commands.get("help");

        assertEquals(
                String.format("Options: %n" +
                        "  --orange=<String>        %n" +
                        "  --yellow=<Boolean>       %n" +
                        "%n" +
                        "Commands: %n" +
                        "                       %n" +
                        "   blue                %n" +
                        "   green               %n" +
                        "   help                %n" +
                        "   red                 %n"),
                help.exec(null));

    }

    public static class Foo {

        @Command
        public String red() {
            final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
            return "red " + Join.join("\n", environment.getGlobalOptions());
        }

        @Command
        public static String green() {
            final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
            return "green " + Join.join("\n", environment.getGlobalOptions());
        }

        @Command
        public static String blue() {
            final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
            return "blue " + Join.join("\n", environment.getGlobalOptions());
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
