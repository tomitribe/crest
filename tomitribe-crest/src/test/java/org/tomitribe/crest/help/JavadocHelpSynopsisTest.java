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
 * The SYNOPSIS reflects the full option universe of the invocation and
 * assembles from single spaces.
 *
 * The pull command declares no options of its own — its only option is
 * contributed by an interceptor — yet the synopsis reads
 * "pull [options] String" above the OPTIONS section that lists it.
 *
 * The charge command has no options anywhere, so the synopsis is the bare
 * "charge String" with a single space, no gap where [options] would sit.
 */
public class JavadocHelpSynopsisTest {

    @Test
    public void interceptorOnlyOptions() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Repos.class).main(env, new String[]{"help", "pull"});

        assertEquals("NAME\n" +
                        "       pull\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       pull [options] String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The pull command copies new commits from one remote.\n" +
                        "\n" +
                        "       The timeout interceptor abandons a pull that stalls.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --timeout=<Integer>\n" +
                        "              seconds a pull may stall before it is abandoned\n" +
                        "       \n" +
                        "              default: 30\n",
                env.getOut().toString());
    }

    @Test
    public void optionless() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Repos.class).main(env, new String[]{"help", "charge"});

        assertEquals("NAME\n" +
                        "       charge\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       charge String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The charge command bills the card once for the given amount.\n",
                env.getOut().toString());
    }

    public static class Repos {

        /**
         * The pull command copies new commits from one remote.
         */
        @Command(interceptedBy = TimeoutInterceptor.class)
        public void pull(final String remote) {
        }

        /**
         * The charge command bills the card once for the given amount.
         */
        @Command
        public void charge(final String amount) {
        }
    }

    public static class TimeoutInterceptor {

        /**
         * The timeout interceptor abandons a pull that stalls.
         *
         * @param timeout seconds a pull may stall before it is abandoned
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("timeout") @Default("30") final Integer timeout) {
            return crestContext.proceed();
        }
    }
}
