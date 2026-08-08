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
import org.tomitribe.crest.api.interceptor.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Interceptors run in ascending {@link Priority} order: the lower the value,
 * the earlier (outermost) the interceptor runs.  Unannotated interceptors
 * default to 5.  Equal priorities run in declaration order.
 *
 * Priority is a double so a new interceptor can always be slotted between
 * two existing priorities without renumbering.
 */
public class InterceptorPriorityTest {

    /**
     * Declared in scrambled order, executed in priority order.  Between the
     * existing 6 and 7 a 6.1 is slotted in, and between 6 and 6.1
     * a 6.09 — no renumbering required.
     */
    @Test
    public void sortedByPriority() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, Six, SixOhNine, SixOne, Seven", main.exec("scrambled", "start"));
    }

    /**
     * An interceptor with no @Priority defaults to 5, the middle of the
     * space, so annotated interceptors can slot in on either side
     */
    @Test
    public void defaultPriorityIsFive() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, Four, Unprioritized, Six", main.exec("defaulted", "start"));
    }

    /**
     * Equal priorities keep declaration order
     */
    @Test
    public void tiesKeepDeclarationOrder() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, TiedB, TiedA", main.exec("tied", "start"));
    }

    /**
     * Priority orders the whole chain regardless of which binding style
     * contributed each interceptor.  Here a custom annotation binding
     * runs before an interceptedBy binding because its priority is lower.
     */
    @Test
    public void prioritySpansBindingStyles() throws Exception {

        final Main main = new Main(Mixed.class, EarlyInterceptor.class);

        assertEquals("start, Early, Seven", main.exec("mixed", "start"));
    }

    public static class Foo {

        @Command(interceptedBy = {SevenInterceptor.class, SixOneInterceptor.class, SixInterceptor.class, SixOhNineInterceptor.class})
        public static String scrambled(final String arg) {
            return arg;
        }

        @Command(interceptedBy = {SixInterceptor.class, UnprioritizedInterceptor.class, FourInterceptor.class})
        public static String defaulted(final String arg) {
            return arg;
        }

        @Command(interceptedBy = {TiedBInterceptor.class, TiedAInterceptor.class})
        public static String tied(final String arg) {
            return arg;
        }
    }

    public static class Mixed {

        @Early
        @Command(interceptedBy = SevenInterceptor.class)
        public static String mixed(final String arg) {
            return arg;
        }
    }

    private static Object append(final CrestContext crestContext, final String name) {
        final List<Object> parameters = crestContext.getParameters();
        parameters.set(0, parameters.get(0) + ", " + name);
        return crestContext.proceed();
    }

    @Priority(4)
    public static class FourInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "Four");
        }
    }

    @Priority(6)
    public static class SixInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "Six");
        }
    }

    @Priority(6.09)
    public static class SixOhNineInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "SixOhNine");
        }
    }

    @Priority(6.1)
    public static class SixOneInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "SixOne");
        }
    }

    @Priority(7)
    public static class SevenInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "Seven");
        }
    }

    public static class UnprioritizedInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "Unprioritized");
        }
    }

    @Priority(3)
    public static class TiedAInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "TiedA");
        }
    }

    @Priority(3)
    public static class TiedBInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "TiedB");
        }
    }

    @CrestInterceptor
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Early {
    }

    @Early
    @Priority(1)
    public static class EarlyInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "Early");
        }
    }
}
