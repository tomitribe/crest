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
 * Where several sites document the same option, the nearest and most
 * explicit wins.  Three rungs, each carrying a distinct marker:
 *
 * --format is documented at three sites: the @Option(description) on the
 * command parameter beats the command's own @param, which beats the
 * interceptor's @param.
 *
 * --width is documented by the command's @param and the interceptor's
 * @param: the command wins.
 *
 * --depth belongs to an @Options bean and is documented by the bean
 * constructor's @param — the declaring site closest to the option.
 *
 * No participant has a javadoc body, so the man page has no DESCRIPTION:
 * option documentation alone does not require narrative.
 */
public class JavadocHelpPrecedenceTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Report.class).main(env, new String[]{"help", "render"});

        assertEquals("NAME\n" +
                        "       render\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       render [options]\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --format=<String>\n" +
                        "              the-annotation-description documents format\n" +
                        "       \n" +
                        "              default: table\n" +
                        "\n" +
                        "       --width=<Integer>\n" +
                        "              the-command-javadoc documents width\n" +
                        "       \n" +
                        "              default: 80\n" +
                        "\n" +
                        "       --depth=<Integer>\n" +
                        "              the-bean-constructor documents depth\n" +
                        "       \n" +
                        "              default: 2\n",
                env.getOut().toString());
    }

    public static class Report {

        /**
         * @param format the-command-javadoc loses to the annotation for format
         * @param width the-command-javadoc documents width
         */
        @Command(interceptedBy = RenderInterceptor.class)
        public void render(
                @Option(value = "format", description = "the-annotation-description documents format") @Default("table") final String format,
                @Option("width") @Default("80") final Integer width,
                final Scan scan) {
        }
    }

    @Options
    public static class Scan {

        /**
         * @param depth the-bean-constructor documents depth
         */
        public Scan(@Option("depth") @Default("2") final Integer depth) {
        }
    }

    public static class RenderInterceptor {

        /**
         * @param format the-interceptor-javadoc loses for format
         * @param width the-interceptor-javadoc loses for width
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("format") @Default("table") final String format,
                                @Option("width") @Default("80") final Integer width) {
            return crestContext.proceed();
        }
    }
}
