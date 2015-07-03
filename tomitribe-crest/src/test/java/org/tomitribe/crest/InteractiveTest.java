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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;
import org.tomitribe.util.Join;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class InteractiveTest {
    @Test
    public void normal() throws Exception {
        normal(Interactive.class, "concat", "--o1=1", "--crest-interactive");
    }

    @Test
    public void grouped() throws Exception {
        normal(Grouped.class, "grouped", "concat", "--o1=1", "--crest-interactive");
    }

    @Test
    public void collection() throws Exception {
        final String eol = System.lineSeparator();
        doTest(
            Interactive.class,
            "a" + eol + "b" + eol + "c" + eol + eol,
            "val (type=String[]): " + // a
            "val (type=String[]): " + // b
            "val (type=String[]): " + // c
            "val (type=String[]): ",
            "a,b,c",
            "join", "--crest-interactive");
    }

    private void normal(final Class<?> cmd, final String... args) throws Exception {
        final String eol = System.lineSeparator();
        doTest(
            cmd,
            // skip o1 since already provided
            "2" + eol +
            eol + // skip value
            "true" + eol,
            "o2 (type=int, default=0): " +
            "o3 (type=Integer): " +
            "o4 (type=boolean, default=false): ",
            "12nulltrue",
            args);
    }

    private void doTest(final Class<?> cmd, final String in, final String out, final String cmdOut, final String... args) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream outStream = new PrintStream(baos);
        final SystemEnvironment env = new SystemEnvironment() {
            @Override
            public InputStream getInput() {
                return bais;
            }

            @Override
            public PrintStream getOutput() {
                return outStream;
            }
        };

        Environment.ENVIRONMENT_THREAD_LOCAL.set(env);
        assertEquals(cmdOut, new Main(cmd).exec(args));
        Environment.ENVIRONMENT_THREAD_LOCAL.remove();
        assertEquals(out, new String(baos.toByteArray()));
    }

    public static class Interactive {
        @Command
        public String concat(@Option("o1") final String o1,
                             @Option("o2") final int o2,
                             @Option("o3") final Integer o3,
                             @Option("o4") @Required final boolean o4) {
            return o1 + o2 + o3 + o4;
        }

        @Command
        public String join(@Option("val") final String[] values) {
            return Join.join(",", values);
        }
    }

    @Command
    public static class Grouped {
        @Command
        public String concat(@Option("o1") final String o1,
                             @Option("o2") final int o2,
                             @Option("o3") final Integer o3,
                             @Option("o4") @Required final boolean o4) {
            return o1 + o2 + o3 + o4;
        }
    }
}
