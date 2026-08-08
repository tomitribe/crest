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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Valid priorities are greater than 0 and less than 11, exclusive on both
 * ends.  The whole-number endpoints are deliberately not allowed so no
 * interceptor can take the last spot and shut others out: there is always
 * room to slot in before or after any legal priority.
 *
 * Out-of-range priorities fail at deploy time, when the Main is constructed.
 */
public class InterceptorPriorityRangeTest {

    /**
     * Values just inside the walls are legal
     */
    @Test
    public void edgesHaveRoom() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, PointOne, TenNine", main.exec("edges", "start"));
    }

    @Test
    public void zeroRejected() throws Exception {

        try {
            new Main(Foo.class, ZeroInterceptor.class);
            fail("Expected InvalidInterceptorPriorityException");
        } catch (final InvalidInterceptorPriorityException pass) {
            assertEquals("Interceptor " +
                    "org.tomitribe.crest.interceptor.InterceptorPriorityRangeTest$ZeroInterceptor" +
                    " declares @Priority(0.0) which is outside the valid range:" +
                    " greater than 0 and less than 11.  Choose a decimal between the two, e.g. @Priority(5)." +
                    "  The endpoints are excluded so there is always room to slot in:" +
                    " to run before an interceptor at 1 use @Priority(0.9)," +
                    " to run after one at 10 use @Priority(10.1)", pass.getMessage());
        }
    }

    @Test
    public void elevenRejected() throws Exception {

        try {
            new Main(Eleven.class);
            fail("Expected InvalidInterceptorPriorityException");
        } catch (final InvalidInterceptorPriorityException pass) {
            assertTrue(pass.getMessage().contains("@Priority(11.0) which is outside the valid range"));
        }
    }

    @Test
    public void negativeRejected() throws Exception {

        try {
            new Main(Negative.class);
            fail("Expected InvalidInterceptorPriorityException");
        } catch (final InvalidInterceptorPriorityException pass) {
            assertTrue(pass.getMessage().contains("@Priority(-3.0) which is outside the valid range"));
        }
    }

    /**
     * NaN fails the range check like any other out-of-range value.  Left
     * unchecked it would scramble the sort instead of failing loudly.
     */
    @Test
    public void nanRejected() throws Exception {

        try {
            new Main(Nan.class);
            fail("Expected InvalidInterceptorPriorityException");
        } catch (final InvalidInterceptorPriorityException pass) {
            assertTrue(pass.getMessage().contains("@Priority(NaN) which is outside the valid range"));
        }
    }

    public static class Foo {

        @Command(interceptedBy = {TenNineInterceptor.class, PointOneInterceptor.class})
        public static String edges(final String arg) {
            return arg;
        }
    }

    public static class Eleven {

        @Command(interceptedBy = ElevenInterceptor.class)
        public static String eleven(final String arg) {
            return arg;
        }
    }

    public static class Negative {

        @Command(interceptedBy = NegativeInterceptor.class)
        public static String negative(final String arg) {
            return arg;
        }
    }

    public static class Nan {

        @Command(interceptedBy = NanInterceptor.class)
        public static String nan(final String arg) {
            return arg;
        }
    }

    private static Object append(final CrestContext crestContext, final String name) {
        final List<Object> parameters = crestContext.getParameters();
        parameters.set(0, parameters.get(0) + ", " + name);
        return crestContext.proceed();
    }

    @Priority(0.1)
    public static class PointOneInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "PointOne");
        }
    }

    @Priority(10.9)
    public static class TenNineInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return append(crestContext, "TenNine");
        }
    }

    @Priority(0)
    public static class ZeroInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }

    @Priority(11)
    public static class ElevenInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }

    @Priority(-3)
    public static class NegativeInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }

    @Priority(Double.NaN)
    public static class NanInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }
}
