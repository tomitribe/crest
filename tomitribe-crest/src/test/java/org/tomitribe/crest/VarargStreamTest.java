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

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Out;

import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class VarargStreamTest {
    @Test
    public void run() throws Exception {
        assertEquals("123truetrue", new Main(TheCmd.class).exec("root", "result", "--some=1", "--other=2", "3"));
        assertEquals("1234truetrue", new Main(TheCmd.class).exec("root", "result", "--some=1", "--other=2", "3", "4"));
    }

    @Command("root")
    public static class TheCmd {
        @Command
        public static String result(@Option("some") final String some,
                                    @Option("other") final String other,
                                    @In final InputStream in,
                                    @Out final PrintStream out,
                                    final String[] value) {
            return some + other + value[0] + (value.length > 1 ? value[1] : "") + (in != null) + (out != null);
        }
    }
}
