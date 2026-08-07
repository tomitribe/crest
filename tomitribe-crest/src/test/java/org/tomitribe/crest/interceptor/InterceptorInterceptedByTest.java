/*
 * Copyright 2026 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.interceptor;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Binding style: the interceptor class is named directly on the command
 * via {@code @Command(interceptedBy = GreenInterceptor.class)}
 *
 * This is the only support that existed in Crest 0.14 and before
 */
public class InterceptorInterceptedByTest {

    @Test
    public void typical() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("GreenInterceptor", main.exec("green", "foo"));
    }

    /**
     * Interceptors run in the order they are listed in interceptedBy
     */
    @Test
    public void ordered() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, seen by First, seen by Second", main.exec("ordered", "foo"));
    }

    /**
     * The class named in interceptedBy has no @CrestInterceptor method.
     * Resolution is lazy, so the failure surfaces at execution time.
     */
    @Test
    public void interceptorMethodMissing() throws Exception {

        final Main main = new Main(Foo.class);

        try {
            main.exec("broken", "foo");
            fail("Expected InterceptorAnnotationNotFoundException");
        } catch (final InterceptorAnnotationNotFoundException pass) {
            assertTrue(pass.getMessage().startsWith("@CrestInterceptor not found on any methods of class " +
                    "org.tomitribe.crest.interceptor.InterceptorInterceptedByTest$NotAnInterceptor"));
        }
    }

    public static class Foo {

        @Command(interceptedBy = GreenInterceptor.class)
        public static String green(final String arg) {
            return arg;
        }

        @Command(interceptedBy = {FirstInterceptor.class, SecondInterceptor.class})
        public static String ordered(final String arg) {
            return arg;
        }

        @Command(interceptedBy = NotAnInterceptor.class)
        public static String broken(final String arg) {
            return arg;
        }
    }

    public static class GreenInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }

    public static class FirstInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", seen by First");
            return crestContext.proceed();
        }
    }

    public static class SecondInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", seen by Second");
            return crestContext.proceed();
        }
    }

    /**
     * Looks like an interceptor, but has no @CrestInterceptor method
     */
    public static class NotAnInterceptor {

        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }
}
