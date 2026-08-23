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

import static org.junit.Assert.assertEquals;

/**
 * Sparse documentation in both directions.  A command with no javadoc of
 * its own still gets a man page when its interceptor supplies one — the
 * interceptor's narrative is the whole DESCRIPTION, with no blank
 * artifacts where the command's body would have been.  And an
 * undocumented interceptor bound to a documented command contributes
 * nothing — no seam artifacts, the command's own page is untouched.
 */
public class JavadocHelpSparseTest {

    @Test
    public void undocumentedCommandDocumentedInterceptor() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Network.class).main(env, new String[]{"help", "ping"});

        assertEquals("NAME\n" +
                        "       ping\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       ping\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The trace interceptor writes every request and reply to the trace log.\n",
                env.getOut().toString());
    }

    @Test
    public void documentedCommandUndocumentedInterceptor() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Network.class).main(env, new String[]{"help", "pong"});

        assertEquals("NAME\n" +
                        "       pong\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       pong\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The pong command answers a ping with the same payload.\n",
                env.getOut().toString());
    }

    public static class Network {

        @Command(interceptedBy = TraceInterceptor.class)
        public void ping() {
        }

        /**
         * The pong command answers a ping with the same payload.
         */
        @Command(interceptedBy = QuietInterceptor.class)
        public void pong() {
        }
    }

    public static class TraceInterceptor {

        /**
         * The trace interceptor writes every request and reply to the
         * trace log.
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }

    public static class QuietInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            return crestContext.proceed();
        }
    }
}
