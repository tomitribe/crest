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
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Table;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class TableInterceptorTest extends TestCase {

    public void test() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("red", main.exec("red", "--fields=circle square"));

    }

    public static class Foo {

        @Command
        @Table
        public String red(@Option("fields") @Default("issueKey summary status.name") final String fields,
                   @Option("sort") @Default("issueType.name priority.name status.name") final String sort) {
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
