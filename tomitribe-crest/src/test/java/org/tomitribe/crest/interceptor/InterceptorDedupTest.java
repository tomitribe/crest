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

/**
 * An interceptor reachable through several bindings on the same command
 * method still runs exactly once.
 */
public class InterceptorDedupTest {

    /**
     * The same interceptor class listed twice in interceptedBy
     */
    @Test
    public void repeatedInterceptedBy() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, intercepted", main.exec("repeated", "foo"));
    }

    /**
     * The same interceptor class bound via interceptedBy and @CrestInterceptor
     */
    @Test
    public void mixedStyles() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, intercepted", main.exec("mixed", "foo"));
    }

    /**
     * One interceptor class carrying two custom annotations, both used on
     * the method.  The two annotations resolve to the same registered
     * interceptor, which must still run only once.
     */
    @Test
    public void aliasedAnnotations() throws Exception {

        final Main main = new Main(Foo.class, LoggingMeteringInterceptor.class);

        assertEquals("foo, intercepted", main.exec("aliased", "foo"));
    }

    /**
     * Either alias alone binds the interceptor
     */
    @Test
    public void singleAlias() throws Exception {

        final Main main = new Main(Foo.class, LoggingMeteringInterceptor.class);

        assertEquals("foo, intercepted", main.exec("single", "foo"));
    }

    public static class Foo {

        @Command(interceptedBy = {CountingInterceptor.class, CountingInterceptor.class})
        public static String repeated(final String arg) {
            return arg;
        }

        @Command(interceptedBy = CountingInterceptor.class)
        @CrestInterceptor(CountingInterceptor.class)
        public static String mixed(final String arg) {
            return arg;
        }

        @Logged
        @Metered
        @Command
        public static String aliased(final String arg) {
            return arg;
        }

        @Logged
        @Command
        public static String single(final String arg) {
            return arg;
        }
    }

    public static class CountingInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", intercepted");
            return crestContext.proceed();
        }
    }

    @CrestInterceptor
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Logged {
    }

    @CrestInterceptor
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Metered {
    }

    @Logged
    @Metered
    public static class LoggingMeteringInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", intercepted");
            return crestContext.proceed();
        }
    }
}
