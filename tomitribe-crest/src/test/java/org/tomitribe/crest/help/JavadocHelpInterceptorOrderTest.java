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
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.Priority;

import static org.junit.Assert.assertEquals;

/**
 * Two interceptors whose @Priority run order is the reverse of both the
 * order the command declares them and their alphabetical order: the
 * command lists Authorize then Verify, the alphabet agrees, yet Verify
 * (priority 3) runs before Authorize (priority 8) — so its narrative
 * inlines first.  The document proves inlining follows run order.
 */
public class JavadocHelpInterceptorOrderTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Payments.class).main(env, new String[]{"help", "charge"});

        assertEquals("NAME\n" +
                        "       charge\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       charge\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The charge command bills the card once.\n" +
                        "\n" +
                        "       The  verify  interceptor  confirms  the  card  is  real  before  anything  is charged.\n" +
                        "\n" +
                        "       The authorize interceptor places the hold after verification passes.\n",
                env.getOut().toString());
    }

    public static class Payments {

        /**
         * The charge command bills the card once.
         */
        @Command(interceptedBy = {AuthorizeInterceptor.class, VerifyInterceptor.class})
        public void charge() {
        }
    }

    @Priority(8)
    public static class AuthorizeInterceptor {

        /**
         * The authorize interceptor places the hold after verification
         * passes.
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }

    @Priority(3)
    public static class VerifyInterceptor {

        /**
         * The verify interceptor confirms the card is real before anything
         * is charged.
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }
}
