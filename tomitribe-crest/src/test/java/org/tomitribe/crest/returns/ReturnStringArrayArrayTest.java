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
package org.tomitribe.crest.returns;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.Crest;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.test.Java;

public class ReturnStringArrayArrayTest {

    @Test
    public void test() throws Exception {
        final Java.Result result = Crest.jar()
                .command(Color.class)
                .exec("orange");

        Assert.assertEquals("" +
                        " ID    KEY                                             SUBJECT                                                                URL                     \n" +
                        "----- ------ ------------------------------------------------------------------------------------------- ---------------------------------------------\n" +
                        " 123   FOO    Lorem ipsum dolor sit amet, consectetur adipiscing elit                                     https://github.com/tomitribe/crest/         \n" +
                        " 456   BAR    Ut enim ad minim veniam, quis nostrud exercitation ullamco                                  https://github.com/tomitribe/tomitribe-util \n" +
                        "  78   BAZ    Duis  aute  irure  dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat   https://tomitribe.io/projects/hodao         \n" +
                        "              nulla pariatur.                                                                                                                         \n" +
                        "   9   YNAR   Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit   https://tomitribe.io/projects/swizzle       \n" +
                        "              anim id est laborum.                                                                                                                    \n",
                result.getOut());
    }

    public static class Color {

        @Command("orange")
        public String[][] method() {
            final String[][] data = new String[5][];
            data[0] = new String[]{"ID", "KEY", "SUBJECT", "URL"};
            data[1] = new String[]{"123", "FOO", "Lorem ipsum dolor sit amet, consectetur adipiscing elit", "https://github.com/tomitribe/crest/"};
            data[2] = new String[]{"456", "BAR", "Ut enim ad minim veniam, quis nostrud exercitation ullamco", "https://github.com/tomitribe/tomitribe-util"};
            data[3] = new String[]{"78", "BAZ", "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.", "https://tomitribe.io/projects/hodao"};
            data[4] = new String[]{"9", "YNAR", "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", "https://tomitribe.io/projects/swizzle"};
            return data;
        }

    }
}
