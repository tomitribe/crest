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
import org.tomitribe.crest.cmds.Cmd;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * An interceptor can declare @Option parameters and @Options beans on its
 * @CrestInterceptor method.  The options merge into the spec of every
 * command the interceptor is bound to: they parse from the command line,
 * appear in help, and are passed to the interceptor at execution — the
 * command method does not need to declare them.
 */
public class InterceptorOptionsTest {

    @Test
    public void optionReachesInterceptor() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, amount=5", main.exec("echo", "--amount=5", "foo"));
    }

    @Test
    public void defaultApplies() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, amount=2", main.exec("echo", "foo"));
    }

    /**
     * The command declares the same option with the same type and default.
     * One option in the parse and help, both declarers receive the value.
     */
    @Test
    public void sharedWithCommand() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, amount=7/7", main.exec("shared", "--amount=7", "foo"));
    }

    /**
     * An @Options bean parameter works just as it would on a command method
     */
    @Test
    public void optionsBean() throws Exception {

        final Main main = new Main(Foo.class);

        assertEquals("foo, window=5-50", main.exec("windowed", "--offset=5", "--limit=50", "foo"));
    }

    @Test
    public void helpShowsInterceptorOptions() throws Exception {

        final TestMain main = new TestMain(Foo.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        main.cmd("echo").help(new PrintStream(out));

        final String help = out.toString();
        assertTrue("help should list the interceptor's option:\n" + help, help.contains("--amount"));
    }

    /**
     * Same option name, different types.  Nothing could work: fail at
     * deploy time naming both parties.
     */
    @Test
    public void typeConflict() throws Exception {

        try {
            new Main(TypeConflict.class);
            fail("Expected InterceptorOptionConflictException");
        } catch (final InterceptorOptionConflictException pass) {
            assertTrue(pass.getMessage().contains("\"amount\""));
            assertTrue(pass.getMessage().contains("the types differ (java.lang.Integer vs java.lang.String)"));
        }
    }

    /**
     * Same option name and type but different defaults.  The single help
     * entry could not tell the truth for both, so this too is a conflict.
     */
    @Test
    public void defaultConflict() throws Exception {

        try {
            new Main(DefaultConflict.class);
            fail("Expected InterceptorOptionConflictException");
        } catch (final InterceptorOptionConflictException pass) {
            assertTrue(pass.getMessage().contains("the defaults differ (2 vs 9)"));
        }
    }

    /**
     * Interceptor methods cannot declare positional parameters — a
     * positional argument on an invisible participant could not be
     * understood by anyone reading the command
     */
    @Test
    public void plainParameterRejected() throws Exception {

        try {
            new Main(Plain.class);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage().contains("may only declare @Option parameters," +
                    " @Options beans and one CrestContext parameter"));
        }
    }

    public static class Foo {

        @Command(interceptedBy = AmountInterceptor.class)
        public static String echo(final String arg) {
            return arg;
        }

        @Command(interceptedBy = AmountInterceptor.class)
        public static String shared(final String arg, @Option("amount") @Default("2") final Integer amount) {
            return arg + "/" + amount;
        }

        @Command(interceptedBy = WindowInterceptor.class)
        public static String windowed(final String arg) {
            return arg;
        }
    }

    public static class AmountInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("amount") @Default("2") final Integer amount) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", amount=" + amount);
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

    public static class WindowInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, final Window window) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", window=" + window.offset + "-" + window.limit);
            return crestContext.proceed();
        }
    }

    public static class TypeConflict {

        @Command(interceptedBy = AmountInterceptor.class)
        public static String conflicted(final String arg, @Option("amount") final String amount) {
            return arg + amount;
        }
    }

    public static class DefaultConflict {

        @Command(interceptedBy = AmountInterceptor.class)
        public static String conflicted(final String arg, @Option("amount") @Default("9") final Integer amount) {
            return arg + amount;
        }
    }

    public static class Plain {

        @Command(interceptedBy = PlainParameterInterceptor.class)
        public static String plain(final String arg) {
            return arg;
        }
    }

    public static class PlainParameterInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, final String notAllowed) {
            return crestContext.proceed();
        }
    }

    public static class TestMain extends Main {

        public TestMain(final Class<?>... classes) {
            super(classes);
        }

        public Cmd cmd(final String name) {
            return commands.get(name);
        }
    }
}
