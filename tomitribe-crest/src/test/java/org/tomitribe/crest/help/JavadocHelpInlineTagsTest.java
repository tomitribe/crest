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
 * Inline javadoc tags translate to plain text when the javadoc is
 * captured: code and literal keep their content, link and linkplain
 * become the label or the shortened member reference, and value becomes
 * the constant it names when the compiler can resolve it.
 *
 * The safety rule is the headline: a tag the translator has never heard
 * of drops its braces and keeps its content, and an unresolvable value
 * falls back to its reference text — a raw '{@' can never reach the
 * rendered document.
 */
public class JavadocHelpInlineTagsTest {

    @Test
    public void tags() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Store.class).main(env, new String[]{"help", "list"});

        assertEquals("NAME\n" +
                        "       list\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       list [options]\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The  list  command prints name=value pairs for every widget in the catalog.  Rows come\n" +
                        "       back freshly cached and expand globs the way Widgets.glob does.\n" +
                        "\n" +
                        "       The match options decide how a <glob> compares to a widget name.\n" +
                        "\n" +
                        "       The limit interceptor caps the list at 25 rows unless raised.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --pattern=<String>\n" +
                        "              only names matching pattern are printed\n" +
                        "       \n" +
                        "              default: *\n" +
                        "\n" +
                        "       --ignore-case\n" +
                        "              treat A and a as the same letter\n" +
                        "\n" +
                        "       --limit=<Integer>\n" +
                        "              rows shown before the rest are elided\n" +
                        "       \n" +
                        "              default: 25\n",
                env.getOut().toString());
    }

    @Test
    public void safety() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Store.class).main(env, new String[]{"help", "unknowable"});

        assertEquals("NAME\n" +
                        "       unknowable\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       unknowable\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The  unknowable  command  keeps  keep this and falls back to Nope.MISSING when nothing\n" +
                        "       resolves.\n",
                env.getOut().toString());
    }

    public static class Store {

        public static final int DEFAULT_LIMIT = 25;

        /**
         * The list command prints {@code name=value} pairs for every
         * widget in the catalog.  Rows come back
         * {@linkplain Store#refresh() freshly cached} and expand globs
         * the way {@link Widgets#glob(String)} does.
         *
         * @param pattern only names matching {@code pattern} are printed
         */
        @Command(interceptedBy = LimitInterceptor.class)
        public void list(@Option("pattern") @Default("*") final String pattern, final Match match) {
        }

        /**
         * The unknowable command keeps {@bogus keep this} and falls back
         * to {@value Nope#MISSING} when nothing resolves.
         */
        @Command
        public void unknowable() {
        }
    }

    public static class Widgets {
    }

    public static class LimitInterceptor {

        /**
         * The limit interceptor caps the list at
         * {@value Store#DEFAULT_LIMIT} rows unless raised.
         *
         * @param limit rows shown before the rest are elided
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("limit") @Default("25") final Integer limit) {
            return crestContext.proceed();
        }
    }

    /**
     * The match options decide how a {@literal <glob>} compares to a
     * widget name.
     */
    @Options
    public static class Match {

        /**
         * @param ignoreCase treat {@code A} and {@code a} as the same letter
         */
        public Match(@Option("ignore-case") @Default("false") final Boolean ignoreCase) {
        }
    }
}
