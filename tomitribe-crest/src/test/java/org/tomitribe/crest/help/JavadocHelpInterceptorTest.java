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
package org.tomitribe.crest.help;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import static org.junit.Assert.assertEquals;

/**
 * A command bound to one interceptor: the interceptor method's javadoc
 * body inlines into DESCRIPTION after the command's own, and the option
 * the interceptor contributes is documented by its @param entry.
 */
public class JavadocHelpInterceptorTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Mail.class).main(env, new String[]{"help", "send"});

        assertEquals("NAME\n" +
                        "       send\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       send [options] String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The send command delivers one message to one recipient.\n" +
                        "\n" +
                        "       The retry interceptor repeats a failed send until it succeeds or the attempts run out.\n" +
                        "\n" +
                        "ARGUMENTS\n" +
                        "       String the text of the message\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --to=<String>\n" +
                        "              the address the message is delivered to\n" +
                        "\n" +
                        "       --attempts=<Integer>\n" +
                        "              deliveries attempted before the failure is final\n" +
                        "       \n" +
                        "              default: 3\n",
                env.getOut().toString());
    }

    public static class Mail {

        /**
         * The send command delivers one message to one recipient.
         *
         * @param to the address the message is delivered to
         * @param message the text of the message
         */
        @Command(interceptedBy = RetryInterceptor.class)
        public void send(@Option("to") final String to, final String message) {
        }
    }

    public static class RetryInterceptor {

        /**
         * The retry interceptor repeats a failed send until it succeeds or
         * the attempts run out.
         *
         * @param attempts deliveries attempted before the failure is final
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("attempts") @Default("3") final Integer attempts) {
            return crestContext.proceed();
        }
    }
}
