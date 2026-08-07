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
 * Binding style: a custom interceptor annotation (@Blue) is used on the
 * @Command method and does not mention the exact interceptor that implements
 * its functionality.
 *
 * We expect the runtime to scan the list of interceptors available and
 * look for one that is annotated with @Blue.  This is the style used by
 * Crest's own @Table annotation, implemented by TableInterceptor.
 */
public class InterceptorCustomAnnotationDiscoveredTest {

    @Test
    public void interceptorIndirectlyNamed() throws Exception {

        final Main main = new Main(Foo.class, BlueInterceptor.class);

        assertEquals("BlueInterceptor", main.exec("blue", "foo"));
    }

    @Test
    public void interceptorIndirectlyNamedDoesNotResolve() throws Exception {

        final Main main = new Main(Foo.class);

        try {
            main.exec("blue", "foo");
            fail("Expected UnresolvedInterceptorAnnotationException");
        } catch (final UnresolvedInterceptorAnnotationException pass) {
            assertEquals("Custom interceptor annotation " +
                    "@org.tomitribe.crest.interceptor.InterceptorCustomAnnotationDiscoveredTest$Blue did not resolve." +
                    "  Please ensure the implementing class is returned by a org.tomitribe.crest.api.Loader" +
                    " and is also annotated with @Blue", pass.getMessage());
        }
    }

    /**
     * Two interceptor classes claim the same custom annotation.  The binding
     * would be ambiguous, so registration fails at deploy time.
     */
    @Test
    public void conflictingInterceptors() throws Exception {

        try {
            new Main(Foo.class, BlueInterceptor.class, RivalBlueInterceptor.class);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage().endsWith("interceptor is conflicting"));
        }
    }

    public static class Foo {

        @Blue
        @Command
        public static String blue(final String arg) {
            return arg;
        }
    }

    @CrestInterceptor
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Blue {
    }

    @Blue
    public static class BlueInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }

    @Blue
    public static class RivalBlueInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }
}
