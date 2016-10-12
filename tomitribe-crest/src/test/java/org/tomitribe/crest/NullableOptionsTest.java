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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NullableOptionsTest {
    @Parameterized.Parameters(name = "exec {0} == {1}")
    public static Iterable<Object[]> data() {
        return asList(
                new Object[]{asList("--p1=1", "--p2=2", "--pref.p2=4", "--p3=5"), "12345"},
                new Object[]{emptyList(), "null1null2"},
                new Object[]{asList("--p1=1", "--pref.p2=4", "--p3=5"), "10345"},
                new Object[]{asList("--p2=0", "--pref.p2=4", "--p3=5"), "null1345"}
        );
    }

    @Parameterized.Parameter
    public List<String> args;

    @Parameterized.Parameter(1)
    public String expected;

    @Test
    public void asserts() throws Exception {
        final Collection<String> allArgs = new ArrayList<>();
        allArgs.add("exec");
        allArgs.addAll(args);
        assertEquals(expected, new Main(TheCmd.class).exec(allArgs.toArray(new String[allArgs.size()])));
    }

    @Options(nillable = true)
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
            return (paramNoPrefix == null ? "null1" : paramNoPrefix.p1 + paramNoPrefix.p2) + (paramPrefixed == null ? "null2" : paramPrefixed.p1 + paramPrefixed.p2 + p3);
        }
    }
}
