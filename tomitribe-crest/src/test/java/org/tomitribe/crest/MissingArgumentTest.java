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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Loader;
import org.tomitribe.crest.test.Java;

import java.util.Iterator;

public class MissingArgumentTest {

    @Test
    public void missingArgument() {

        final Java.Result result = Crest.jar()
                .command(Greeting.class)
                .exec("hello");

        Assert.assertEquals("Missing argument: String\n" +
                "\n" +
                "Usage: hello  String\n" +
                "\n", result.getErr());
    }


    public static class Greeting {
        @Command
        public String hello(final String name) {
            return String.format("Hello, %s!", name);
        }
    }

    public static class Commands implements Loader {
        @Override
        public Iterator<Class<?>> iterator() {
            return Loader.of(Greeting.class);
        }
    }

}
