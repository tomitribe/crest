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

import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.crest.api.Err;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class StreamInjectionTest {
    @Test
    public void run() throws Exception {
        assertEquals("123", new Main(Command.class).exec("asserts", "--p1=1", "--p2=2", "--p3=3"));
    }

    @Test
    @Ignore
    public void withArgs() throws Exception {
        assertEquals("orange", new Main(Command.class).exec("withArgs", "orange"));
    }

    @Test
    @Ignore
    public void withArgsWithInput() throws Exception {
        assertEquals("orange", new Main(Command.class).exec("withArgsWithInput", "orange"));
    }

    @Test
    @Ignore
    public void withArgs1() throws Exception {
        assertEquals("orange", new Main(Command.class).exec("withArgs1", "orange"));
    }

    @Test
    public void withArgs2() throws Exception {
        assertEquals("orange", new Main(Command.class).exec("withArgs2", "orange"));
    }

    @Test
    public void helpHidesTheseInternalParams() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream err = new PrintStream(out);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public PrintStream getError() {
                return err;
            }
        });
        try {
            assertEquals("123", new Main(new SystemPropertiesDefaultsContext(), Command.class).exec("asserts", "--p=1", "--p2=2", "--p3=3"));
        } catch (final IllegalArgumentException iae) {
            // we expect this one actually
        }
        Environment.ENVIRONMENT_THREAD_LOCAL.remove();

        final String errorOutput = new String(out.toByteArray());
        assertFalse(errorOutput.contains("InputStream"));
        assertFalse(errorOutput.contains("Environment"));
    }

    @Test
    public void array() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream err = new PrintStream(out);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public PrintStream getError() {
                return err;
            }
        });
        try {
            assertEquals("1234", new Main(new SystemPropertiesDefaultsContext(), Command.class).exec("withArray", "--p=1", "--p2=2", "--p3=3", "--p3=4"));
        } catch (final IllegalArgumentException iae) {
            // we expect this one actually
        }
        Environment.ENVIRONMENT_THREAD_LOCAL.remove();

        final String errorOutput = new String(out.toByteArray());
        assertFalse(errorOutput.contains("InputStream"));
        assertFalse(errorOutput.contains("Environment"));
    }

    public static class Command {
        @org.tomitribe.crest.api.Command
        public static String asserts(@Option("p1") final String p1,
                                     @In final InputStream in,
                                     @Option("p2") final String p2,
                                     @Out final PrintStream out,
                                     @Option("p3") final String p3,
                                     @Err PrintStream err,
                                     Environment environment) {
            assertEquals(environment, Environment.ENVIRONMENT_THREAD_LOCAL.get());
            assertEquals(in, environment.getInput());
            assertEquals(out, environment.getOutput());
            assertEquals(err, environment.getError());
            return p1 + p2 + p3; // just to ensure we dont break param injection and that the method was actually called
        }

        @org.tomitribe.crest.api.Command
        public static String withArray(@Option("p1") final String p1,
                                       @In final InputStream in,
                                       @Option("p2") final String p2,
                                       @Out final PrintStream out,
                                       @Option("p3") final String[] p3,
                                       @Err PrintStream err,
                                       Environment environment) {
            assertEquals(environment, Environment.ENVIRONMENT_THREAD_LOCAL.get());
            assertEquals(in, environment.getInput());
            assertEquals(out, environment.getOutput());
            assertEquals(err, environment.getError());
            return p1 + p2 + asList(p3);
        }

        @org.tomitribe.crest.api.Command
        public static String withArgs(@Out final PrintStream out,
                                      @Err PrintStream err,
                                      Environment environment,
                                      String arg
        ) {
            assertEquals(environment, Environment.ENVIRONMENT_THREAD_LOCAL.get());
            assertEquals(out, environment.getOutput());
            assertEquals(err, environment.getError());
            return arg;
        }

        @org.tomitribe.crest.api.Command
        public static String withArgsWithInput(@In final InputStream in,
                                               @Out final PrintStream out,
                                               @Err PrintStream err,
                                               Environment environment,
                                               String arg
        ) {
            assertEquals(environment, Environment.ENVIRONMENT_THREAD_LOCAL.get());
            assertEquals(in, environment.getInput());
            assertEquals(out, environment.getOutput());
            assertEquals(err, environment.getError());
            return arg;
        }

        @org.tomitribe.crest.api.Command
        public static String withArgs1(@Out final PrintStream out,
                                       @Err PrintStream err,
                                       String arg
        ) {
            final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
            assertEquals(out, environment.getOutput());
            assertEquals(err, environment.getError());
            return arg;
        }

        @org.tomitribe.crest.api.Command
        public static String withArgs2(String arg,
                                       @Err PrintStream err,
                                       @Out final PrintStream out

        ) {
            final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
            assertEquals(out, environment.getOutput());
            assertEquals(err, environment.getError());
            return arg;
        }
    }
}
