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
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.Priority;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CrestContext.getOptions() is the mutable option namespace of the
 * invocation: one converted value per option name, shared by every
 * declarer.  Replacing a value is seen by every interceptor later in the
 * chain and by the command itself.  Where the command method declares the
 * option, the entry is a live view over the same storage as
 * getParameters().
 */
public class InterceptorOptionsMutationTest {

    /**
     * A low-priority interceptor clamps --amount.  The higher-priority
     * interceptor and the command both see the clamped value.
     */
    @Test
    public void replacementVisibleDownstream() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, clamped, audit=100/100", main.exec("transfer", "--amount=5000", "start"));
    }

    /**
     * An interceptor can read options it did not declare — the map holds
     * the invocation's complete option namespace
     */
    @Test
    public void readWithoutDeclaring() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, saw=us-east/us-east", main.exec("deploy", "start"));
    }

    /**
     * Command-declared options are one storage with two keys: a write
     * through getParameters() reads back through getOptions() and vice
     * versa
     */
    @Test
    public void liveView() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start, ok/final", main.exec("live", "--region=supplied", "start"));
    }

    /**
     * Replacing a constituent option rebuilds the bean derived from it, so
     * the command's bean reflects the final values
     */
    @Test
    public void beanConstituentRebuild() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("start/3-99", main.exec("windowed", "--offset=3", "start"));
    }

    /**
     * Writes are type checked against the declared option type
     */
    @Test
    public void putTypeChecked() throws Exception {

        final Main main = new Main(Foo.class);

        try {
            main.exec("badput", "start");
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage().contains("Option \"amount\" is declared as java.lang.Integer" +
                    " and cannot be set to an instance of java.lang.String"));
        }
    }

    /**
     * The key set is the command's declared option namespace — values may
     * be replaced, entries may not be invented
     */
    @Test
    public void putUnknownOption() throws Exception {

        final Main main = new Main(Foo.class);

        try {
            main.exec("unknownput", "start");
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage().contains("No such option \"bogus\""));
        }
    }

    public static class Foo {

        @Command(interceptedBy = {ClampInterceptor.class, AuditInterceptor.class})
        public static String transfer(final String arg, @Option("amount") final Integer amount) {
            return arg + "/" + amount;
        }

        @Command(interceptedBy = SpyInterceptor.class)
        public static String deploy(final String arg, @Option("region") @Default("us-east") final String region) {
            return arg + "/" + region;
        }

        @Command(interceptedBy = LiveViewInterceptor.class)
        public static String live(final String arg, @Option("region") final String region) {
            return arg + "/" + region;
        }

        @Command(interceptedBy = LimitInterceptor.class)
        public static String windowed(final String arg, final Window window) {
            return arg + "/" + window.offset + "-" + window.limit;
        }

        @Command(interceptedBy = BadPutInterceptor.class)
        public static String badput(final String arg, @Option("amount") final Integer amount) {
            return arg + "/" + amount;
        }

        @Command(interceptedBy = UnknownPutInterceptor.class)
        public static String unknownput(final String arg) {
            return arg;
        }
    }

    @Priority(2)
    public static class ClampInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("amount") final Integer amount) {
            if (amount != null && amount > 100) {
                crestContext.getOptions().put("amount", 100);
                final List<Object> parameters = crestContext.getParameters();
                parameters.set(0, parameters.get(0) + ", clamped");
            }
            return crestContext.proceed();
        }
    }

    @Priority(6)
    public static class AuditInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("amount") final Integer amount) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", audit=" + amount);
            return crestContext.proceed();
        }
    }

    /**
     * Declares no options at all, yet can read the command's
     */
    public static class SpyInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", saw=" + crestContext.getOptions().get("region"));
            return crestContext.proceed();
        }
    }

    public static class LiveViewInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();

            parameters.set(1, "override");
            final boolean listToMap = "override".equals(crestContext.getOptions().get("region"));

            crestContext.getOptions().put("region", "final");
            final boolean mapToList = "final".equals(parameters.get(1));

            parameters.set(0, parameters.get(0) + (listToMap && mapToList ? ", ok" : ", broken"));
            return crestContext.proceed();
        }
    }

    public static class LimitInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            crestContext.getOptions().put("limit", 99);
            return crestContext.proceed();
        }
    }

    public static class BadPutInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            crestContext.getOptions().put("amount", "not a number");
            return crestContext.proceed();
        }
    }

    public static class UnknownPutInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            crestContext.getOptions().put("bogus", 1);
            return crestContext.proceed();
        }
    }

    @Options
    public static class Window {

        private final Integer offset;
        private final Integer limit;

        public Window(@Option("offset") @Default("0") final Integer offset,
                      @Option("limit") @Default("10") final Integer limit) {
            this.offset = offset;
            this.limit = limit;
        }
    }
}
