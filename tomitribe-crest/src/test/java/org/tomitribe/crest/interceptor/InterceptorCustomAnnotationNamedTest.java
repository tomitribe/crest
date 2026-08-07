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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Binding style: a custom interceptor annotation (@Red) is used on the
 * @Command method and explicitly references the intended interceptor,
 * RedInterceptor.
 *
 * We expect the runtime to see @Red is annotated with @CrestInterceptor
 * and to directly resolve that to RedInterceptor
 */
public class InterceptorCustomAnnotationNamedTest {

    @Test
    public void interceptorDirectlyNamed() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("RedInterceptor", main.exec("red", "foo"));
    }

    /**
     * The class named by the custom annotation has no @CrestInterceptor method.
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
                    "org.tomitribe.crest.interceptor.InterceptorCustomAnnotationNamedTest$NotAnInterceptor"));
        }
    }

    public static class Foo {

        @Red
        @Command
        public static String red(final String arg) {
            return arg;
        }

        @Broken
        @Command
        public static String broken(final String arg) {
            return arg;
        }
    }

    @CrestInterceptor(RedInterceptor.class)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Red {
    }

    public static class RedInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }

    @CrestInterceptor(NotAnInterceptor.class)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Broken {
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
