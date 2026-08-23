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
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import static org.junit.Assert.assertEquals;

/**
 * An interceptor carrying its own @Options bean: the bean's narrative
 * inlines right after its interceptor's, and the option the bean
 * contributes is documented by the bean constructor's @param entry.
 */
public class JavadocHelpInterceptorWithBeanTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Web.class).main(env, new String[]{"help", "fetch"});

        assertEquals("NAME\n" +
                        "       fetch\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       fetch [options]\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The fetch command downloads one url and prints the body.\n" +
                        "\n" +
                        "       The  cache  interceptor  serves  a  stored  copy when one is fresh enough, sparing the\n" +
                        "       network entirely.\n" +
                        "\n" +
                        "       The  expiry  options  decide  how  old  a stored copy may be and still count as fresh.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --quiet\n" +
                        "              suppress the body and print only the status line\n" +
                        "\n" +
                        "       --ttl=<Integer>\n" +
                        "              seconds a stored copy stays fresh\n" +
                        "       \n" +
                        "              default: 60\n",
                env.getOut().toString());
    }

    public static class Web {

        /**
         * The fetch command downloads one url and prints the body.
         *
         * @param quiet suppress the body and print only the status line
         */
        @Command(interceptedBy = CacheInterceptor.class)
        public void fetch(@Option("quiet") final boolean quiet) {
        }
    }

    public static class CacheInterceptor {

        /**
         * The cache interceptor serves a stored copy when one is fresh
         * enough, sparing the network entirely.
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext, final Expiry expiry) {
            return crestContext.proceed();
        }
    }

    /**
     * The expiry options decide how old a stored copy may be and still
     * count as fresh.
     */
    @Options
    public static class Expiry {

        /**
         * @param ttl seconds a stored copy stays fresh
         */
        public Expiry(@Option("ttl") @Default("60") final Integer ttl) {
        }
    }
}
