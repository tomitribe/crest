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
 * Binding style: {@code @CrestInterceptor(YellowInterceptor.class)} is used
 * directly on the @Command method and explicitly names the interceptor class.
 *
 * It is possible this was an untested feature of Crest 0.14 and before.
 * Use of this style is not recommended.
 */
public class InterceptorMethodAnnotationTest {

    @Test
    public void lessCommon() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("YellowInterceptor", main.exec("yellow", "foo"));
    }

    /**
     * When @CrestInterceptor is used directly on an @Command method the class
     * value is required.  There is no annotation to scan for, so nothing can
     * be resolved.  The failure surfaces at deploy time.
     */
    @Test
    public void classValueMissing() throws Exception {

        try {
            new Main(Bare.class);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage().startsWith("Use of @CrestInterceptor on an @Command method " +
                    "requires the class value to be supplied."));
        }
    }

    public static class Foo {

        @Command
        @CrestInterceptor(YellowInterceptor.class)
        public static String yellow(final String arg) {
            return arg;
        }
    }

    public static class Bare {

        @Command
        @CrestInterceptor
        public static String bare(final String arg) {
            return arg;
        }
    }

    public static class YellowInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }
}
