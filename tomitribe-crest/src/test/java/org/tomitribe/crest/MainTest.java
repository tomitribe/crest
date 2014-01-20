/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest;

import junit.framework.TestCase;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Join;

/**
 * @version $Revision$ $Date$
 */
public class MainTest extends TestCase {

    public void test() throws Exception {

        final Main main = new Main(new ClassesArchive(Foo.class));

        assertEquals("green", main.exec("green"));

        try {
            // does not exist
            main.exec("foo");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            // arg does not exist
            main.exec("blue", "foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testHelp() throws Exception {

        final Main main = new Main(new ClassesArchive(Foo.class));
        final Cmd help = main.commands.get("help");

        assertEquals(
                "Commands: \n" +
                        "                       \n" +
                        "   help                \n" +
                        "   red                 \n" +
                        "   blue                \n" +
                        "   green               \n",
                help.exec());

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

}
