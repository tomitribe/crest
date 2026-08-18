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

import org.junit.After;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Err;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * An interceptor method may declare everything a command method can inject
 * except positional arguments: @Option parameters, @Options beans, and the
 * injected parameters — @In InputStream, @Out and @Err PrintStream,
 * Environment, and registered services.  Positional parameters remain
 * rejected at deploy time.
 */
public class InterceptorInjectionsTest {

    @After
    public void reset() {
        Environment.ENVIRONMENT_THREAD_LOCAL.remove();
    }

    /**
     * @Option + CrestContext + @Out PrintStream on the same interceptor method
     */
    @Test
    public void outReachesInterceptor() throws Exception {

        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        final PrintStream outStream = new PrintStream(captured);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public PrintStream getOutput() {
                return outStream;
            }
        });

        final Main main = new Main(Foo.class);

        assertEquals("foo", main.exec("echo", "--amount=5", "foo"));

        outStream.flush();
        assertEquals("amount=5\n", captured.toString());
    }

    @Test
    public void errReachesInterceptor() throws Exception {

        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        final PrintStream errStream = new PrintStream(captured);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public PrintStream getError() {
                return errStream;
            }
        });

        final Main main = new Main(Foo.class);

        assertEquals("foo", main.exec("warned", "foo"));

        errStream.flush();
        assertEquals("warning issued\n", captured.toString());
    }

    @Test
    public void inReachesInterceptor() throws Exception {

        final InputStream piped = new ByteArrayInputStream("piped".getBytes(UTF_8));
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public InputStream getInput() {
                return piped;
            }
        });

        final Main main = new Main(Foo.class);

        assertEquals("foo, stdin=piped", main.exec("piped", "foo"));
    }

    @Test
    public void environmentReachesInterceptor() throws Exception {

        final Environment environment = new SystemEnvironment();
        Environment.ENVIRONMENT_THREAD_LOCAL.set(environment);

        final Main main = new Main(Foo.class);

        assertEquals("foo, sawEnvironment=true", main.exec("environmental", "foo"));
    }

    @Test
    public void serviceReachesInterceptor() throws Exception {

        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment(
                Collections.<Class<?>, Object>singletonMap(Greeting.class, new Greeting("hello"))));

        final Main main = new Main(Services.class);

        assertEquals("foo, greeting=hello", main.exec("greeted", "foo"));
    }

    /**
     * Every injectable kind on one method, deliberately scrambled: @Err
     * first, the CrestContext in the middle, options and streams
     * interleaved.  Declaration order must not matter.
     */
    @Test
    public void mixedOrder() throws Exception {

        final ByteArrayOutputStream outCaptured = new ByteArrayOutputStream();
        final PrintStream outStream = new PrintStream(outCaptured);
        final ByteArrayOutputStream errCaptured = new ByteArrayOutputStream();
        final PrintStream errStream = new PrintStream(errCaptured);
        final InputStream piped = new ByteArrayInputStream("piped".getBytes(UTF_8));
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public PrintStream getOutput() {
                return outStream;
            }

            @Override
            public PrintStream getError() {
                return errStream;
            }

            @Override
            public InputStream getInput() {
                return piped;
            }
        });

        final Main main = new Main(Foo.class);

        assertEquals("foo, window=5-50", main.exec("mixed", "--amount=7", "--offset=5", "--limit=50", "foo"));

        outStream.flush();
        errStream.flush();
        assertEquals("mixed amount=7 stdin=piped\n", outCaptured.toString());
        assertEquals("mixed err\n", errCaptured.toString());
    }

    /**
     * The injected parameters never merge into the command's option
     * namespace, so they must not appear in help
     */
    @Test
    public void helpHidesInjectedParams() throws Exception {

        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment());

        final InterceptorOptionsTest.TestMain main = new InterceptorOptionsTest.TestMain(Foo.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        main.cmd("mixed").help(new PrintStream(out));

        final String help = out.toString();
        assertTrue("help should list the interceptor's option:\n" + help, help.contains("--amount"));
        assertTrue("help should list the bean's options:\n" + help, help.contains("--offset"));
        assertTrue("help must not show injected streams:\n" + help, !help.contains("PrintStream"));
        assertTrue("help must not show injected streams:\n" + help, !help.contains("InputStream"));
    }

    /**
     * Positional parameters remain rejected: an interceptor is an invisible
     * participant and cannot claim arguments from the command line
     */
    @Test
    public void positionalParameterStillRejected() throws Exception {

        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment());

        try {
            new Main(Positional.class);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException pass) {
            assertTrue(pass.getMessage(), pass.getMessage().contains("may not declare positional parameters"));
            assertTrue("the error must say how to recover:\n" + pass.getMessage(),
                    pass.getMessage().contains("@Option"));
        }
    }

    /**
     * An @Options bean whose constructor takes @Out works in an interceptor
     * just as it does in a command
     */
    @Test
    public void optionsBeanWithInjectedStream() throws Exception {

        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        final PrintStream outStream = new PrintStream(captured);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment() {
            @Override
            public PrintStream getOutput() {
                return outStream;
            }
        });

        final Main main = new Main(Foo.class);

        assertEquals("foo", main.exec("reported", "--label=speed", "foo"));

        outStream.flush();
        assertEquals("label=speed\n", captured.toString());
    }

    public static class Foo {

        @Command(interceptedBy = OutInterceptor.class)
        public static String echo(final String arg) {
            return arg;
        }

        @Command(interceptedBy = ErrInterceptor.class)
        public static String warned(final String arg) {
            return arg;
        }

        @Command(interceptedBy = InInterceptor.class)
        public static String piped(final String arg) {
            return arg;
        }

        @Command(interceptedBy = EnvironmentInterceptor.class)
        public static String environmental(final String arg) {
            return arg;
        }

        @Command(interceptedBy = MixedInterceptor.class)
        public static String mixed(final String arg) {
            return arg;
        }

        @Command(interceptedBy = ReportingInterceptor.class)
        public static String reported(final String arg) {
            return arg;
        }
    }

    public static class OutInterceptor {

        @CrestInterceptor
        public Object intercept(@Option("amount") @Default("2") final Integer amount,
                                final CrestContext crestContext,
                                @Out final PrintStream out) {
            out.println("amount=" + amount);
            return crestContext.proceed();
        }
    }

    public static class ErrInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, @Err final PrintStream err) {
            err.println("warning issued");
            return crestContext.proceed();
        }
    }

    public static class InInterceptor {

        @CrestInterceptor
        public Object intercept(@In final InputStream in, final CrestContext crestContext) throws IOException {
            final byte[] buffer = new byte[64];
            final int length = in.read(buffer);
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", stdin=" + new String(buffer, 0, length, UTF_8));
            return crestContext.proceed();
        }
    }

    public static class EnvironmentInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, final Environment environment) {
            assertNotNull(environment);
            assertSame(Environment.ENVIRONMENT_THREAD_LOCAL.get(), environment);
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", sawEnvironment=true");
            return crestContext.proceed();
        }
    }

    /**
     * A parameter only classifies as a service when the type is registered
     * in the Environment as the commands deploy, so the service-injected
     * command lives apart from Foo
     */
    public static class Services {

        @Command(interceptedBy = ServiceInterceptor.class)
        public static String greeted(final String arg) {
            return arg;
        }
    }

    public static class Greeting {

        private final String message;

        public Greeting(final String message) {
            this.message = message;
        }
    }

    public static class ServiceInterceptor {

        @CrestInterceptor
        public Object intercept(final Greeting greeting, final CrestContext crestContext) {
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", greeting=" + greeting.message);
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

    public static class MixedInterceptor {

        @CrestInterceptor
        public Object intercept(@Err final PrintStream err,
                                @Option("amount") @Default("2") final Integer amount,
                                final CrestContext crestContext,
                                @Out final PrintStream out,
                                final Window window,
                                @In final InputStream in) throws IOException {
            final byte[] buffer = new byte[64];
            final int length = in.read(buffer);
            out.println("mixed amount=" + amount + " stdin=" + new String(buffer, 0, length, UTF_8));
            err.println("mixed err");
            final List<Object> parameters = crestContext.getParameters();
            parameters.set(0, parameters.get(0) + ", window=" + window.offset + "-" + window.limit);
            return crestContext.proceed();
        }
    }

    @Options
    public static class Report {

        private final String label;
        private final PrintStream out;

        public Report(@Option("label") @Default("none") final String label, @Out final PrintStream out) {
            this.label = label;
            this.out = out;
        }
    }

    public static class ReportingInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, final Report report) {
            report.out.println("label=" + report.label);
            return crestContext.proceed();
        }
    }

    public static class Positional {

        @Command(interceptedBy = PositionalInterceptor.class)
        public static String positional(final String arg) {
            return arg;
        }
    }

    public static class PositionalInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, final String notAllowed) {
            return crestContext.proceed();
        }
    }
}
