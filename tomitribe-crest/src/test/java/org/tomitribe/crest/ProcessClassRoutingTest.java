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
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.cmds.processors.InvalidCommandUsageException;
import org.tomitribe.crest.interceptor.InterceptorAnnotationNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Main.processClass decides whether a class is a command holder or an
 * interceptor.  A class with a class-level @Command or a public @Command
 * method must never fall into the interceptor branch.
 */
public class ProcessClassRoutingTest {

    /**
     * Class-level @Command with no @Command methods.  This is the shape the
     * crest-maven-plugin descriptor scan lists.  It must not be mistaken
     * for an interceptor.
     */
    @Test
    public void classLevelCommandWithNoMethods() {
        try {
            new Main(ClassLevelOnly.class);
            fail("Expected IllegalArgumentException");
        } catch (final InterceptorAnnotationNotFoundException wrong) {
            fail("Class-level @Command was routed to the interceptor branch: " + wrong.getMessage());
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage(), pass.getMessage().startsWith("No @Command methods found on"));
        }
    }

    @Test
    public void classLevelCommandWithMethods() throws Exception {
        final Main main = new Main(Group.class);
        assertEquals("went", main.exec("group", "go"));
    }

    /**
     * Commands.get uses getMethods(), so public @Command methods inherited
     * from a parent count.  isCommand must agree or the subclass is routed
     * to the interceptor branch.
     */
    @Test
    public void inheritedCommandMethods() throws Exception {
        final Main main = new Main(Child.class);
        assertEquals("parent", main.exec("parent"));
    }

    /**
     * A non-public @Command method is invisible to getMethods(), so the class
     * has no commands at runtime.  It must be reported as a mistake rather
     * than routed to the interceptor branch.
     */
    @Test
    public void privateCommandMethodOnly() {
        assertNonPublicRejected(PrivateOnly.class);
    }

    @Test
    public void protectedCommandMethodOnly() {
        assertNonPublicRejected(ProtectedOnly.class);
    }

    @Test
    public void packagePrivateCommandMethodOnly() {
        assertNonPublicRejected(PackagePrivateOnly.class);
    }

    private static void assertNonPublicRejected(final Class<?> clazz) {
        try {
            new Main(clazz);
            fail("Expected InvalidCommandUsageException");
        } catch (final InterceptorAnnotationNotFoundException wrong) {
            fail("Non-public @Command was routed to the interceptor branch: " + wrong.getMessage());
        } catch (final InvalidCommandUsageException pass) {
            assertEquals(1, pass.getViolations().size());
            assertTrue(pass.getMessage(), pass.getMessage().startsWith("Invalid @Command usage on class"));
        }
    }

    /**
     * A class with only a @CrestInterceptor method still routes to the
     * interceptor branch and binds normally.
     */
    @Test
    public void interceptorStillRoutesToInterceptorBranch() throws Exception {
        final Main main = new Main(Group.class, Tagger.class);
        assertEquals("went", main.exec("group", "go"));
    }

    @Command
    public static class ClassLevelOnly {
        public static String notACommand() {
            return "nope";
        }
    }

    @Command("group")
    public static class Group {
        @Command
        public static String go() {
            return "went";
        }
    }

    public static class Parent {
        @Command
        public static String parent() {
            return "parent";
        }
    }

    public static class Child extends Parent {
    }

    public static class PrivateOnly {
        @Command
        private static String hidden() {
            return "hidden";
        }
    }

    public static class ProtectedOnly {
        @Command
        protected static String hidden() {
            return "hidden";
        }
    }

    public static class PackagePrivateOnly {
        @Command
        static String hidden() {
            return "hidden";
        }
    }

    public static class Tagger {
        @CrestInterceptor
        public Object intercept(final CrestContext context) {
            return context.proceed();
        }
    }
}
