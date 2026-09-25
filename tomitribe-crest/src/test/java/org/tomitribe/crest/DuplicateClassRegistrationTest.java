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

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * The same class can reach Main more than once: a crest-commands.txt
 * appended from several jars, a Loader plus a classpath scan, or a
 * caller listing it twice.  Main must register each class once and
 * must not mistake the repeat for a conflict.
 */
public class DuplicateClassRegistrationTest {

    @Before
    public void reset() {
        CountingInterceptor.calls.set(0);
    }

    @Test
    public void commandClassTwice() throws Exception {
        final Main main = new Main(Greet.class, Greet.class);
        assertEquals("hello", main.exec("greet"));
    }

    @Test
    public void commandGroupTwice() throws Exception {
        final Main main = new Main(Group.class, Group.class);
        assertEquals("went", main.exec("group", "go"));
        assertEquals(1, ((org.tomitribe.crest.cmds.CmdGroup) main.commands.get("group")).getCommands().size());
    }

    /**
     * Before dedupe this threw "interceptor is conflicting" because the
     * custom annotation was put into the interceptor map a second time.
     */
    @Test
    public void interceptorClassTwice() throws Exception {
        final Main main = new Main(Counted.class, CountingInterceptor.class, CountingInterceptor.class);
        assertEquals("counted", main.exec("counted"));
        assertEquals("interceptor must wrap the command exactly once", 1, CountingInterceptor.calls.get());
    }

    @Test
    public void viaBuilder() throws Exception {
        final Main main = Main.builder()
                .command(Greet.class)
                .command(Greet.class)
                .command(CountingInterceptor.class)
                .command(CountingInterceptor.class)
                .command(Counted.class)
                .build();
        assertEquals("hello", main.exec("greet"));
        assertEquals("counted", main.exec("counted"));
        assertEquals(1, CountingInterceptor.calls.get());
    }

    public static class Greet {
        @Command
        public static String greet() {
            return "hello";
        }
    }

    @Command("group")
    public static class Group {
        @Command
        public static String go() {
            return "went";
        }
    }

    public static class Counted {
        @Count
        @Command
        public static String counted() {
            return "counted";
        }
    }

    @CrestInterceptor
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Count {
    }

    @Count
    public static class CountingInterceptor {
        static final AtomicInteger calls = new AtomicInteger();

        @CrestInterceptor
        public Object intercept(final CrestContext context) {
            calls.incrementAndGet();
            return context.proceed();
        }
    }
}
