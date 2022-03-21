/*
 * Copyright 2022 Tomitribe and community
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
import static org.junit.Assert.fail;

public class CustomInterceptorAnnotationTest {

    @Test
    public void typical() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("GreenInterceptor", main.exec("green", "foo"));
    }

    @Test
    public void lessCommon() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("YellowInterceptor", main.exec("yellow", "foo"));
    }

    @Test
    public void interceptorDirectlyNamed() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("RedInterceptor", main.exec("red", "foo"));
    }

    @Test
    public void interceptorIndirectlyNamed() throws Exception {

        final Main main = new Main(Foo.class, BlueInterceptor.class);

        assertEquals("BlueInterceptor", main.exec("blue", "foo"));
    }

    @Test
    public void interceptorIndirectlyNamedDoesNotResolve() throws Exception {

        final Main main = new Main(Foo.class);

        try {
            final Object exec = main.exec("blue", "foo");
            fail("Expected UnresolvedInterceptorAnnotationException");
        } catch (UnresolvedInterceptorAnnotationException pass) {
            assertEquals("Custom interceptor annotation " +
                    "@org.tomitribe.crest.interceptor.CustomInterceptorAnnotationTest$Blue did not resolve." +
                    "  Please ensure the implementing class is returned by a org.tomitribe.crest.api.Loader" +
                    " and is also annotated with @Blue", pass.getMessage());
        }
    }

    public static class Foo {

        @Command(interceptedBy = GreenInterceptor.class)
        public static String green(final String arg) {
            return arg;
        }

        @Command
        @CrestInterceptor(YellowInterceptor.class)
        public static String yellow(final String arg) {
            return arg;
        }

        @Red
        @Command
        public static String red(final String arg) {
            return arg;
        }

        @Blue
        @Command
        public static String blue(final String arg) {
            return arg;
        }
    }

    /**
     * This is the only support that existed in Crest 0.14 and before
     */
    public static class GreenInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }

    /**
     * It is possible this was an untested feature of Crest 0.14 and before
     * Here the @CrestInterceptor method is used on the @Command and explicitly
     * names the interceptor class.  Use of this style is not recommended.
     */
    public static class YellowInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, this.getClass().getSimpleName());
            return crestContext.proceed();
        }
    }

    /**
     * In this scenario the custom interceptor annotation (@Red)
     * explicitly references the intended interceptor, RedInterceptor
     *
     * We expect the runtime to see @Red is annotated with @CrestInterceptor
     * and to directly resolve that to RedInterceptor
     */
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

    /**
     * In this scenario the custom interceptor annotation (@Blue)
     * does not mention the exact interceptor that implements its functionality
     *
     * We expect the runtime to scan the list of interceptors available and
     * look for one that is annotated with @Blue
     */
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


}
