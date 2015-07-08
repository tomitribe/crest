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
import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;

import static org.junit.Assert.assertEquals;

public class BenInjectionPrefixTest {
    @Test
    public void execute() throws Exception {
        assertEquals("12345", new Main(TheCmd.class).exec("exec", "--p1=1", "--p2=2", "--pref.p2=4", "--p3=5"));
    }

    @Options
    public static class Params {
        private final String p1;
        private final int p2;

        public Params(@Option("p1") final String p1, @Option("p2") final int p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    public static class TheCmd {
        @Command
        public static String exec(final Params paramNoPrefix,

                                  @Option("pref.")
                                  @Defaults(@Defaults.DefaultMapping(name = "p1", value = "3"))
                                  final Params paramPrefixed,

                                  @Option("p3") final int p3) {
            return paramNoPrefix.p1 + paramNoPrefix.p2 + paramPrefixed.p1 + paramPrefixed.p2 + p3;
        }
    }
}
