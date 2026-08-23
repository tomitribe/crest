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

import static org.junit.Assert.assertEquals;

/**
 * A dash at preformatted indentation is literal content, not a bullet:
 * the indented example {@code --chunk-size=1048576} renders exactly as
 * written, hyphens intact, while the low-indent "- item" lines above it
 * keep parsing as bullets.
 */
public class JavadocHelpPreformattedTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Mirror.class).main(env, new String[]{"help", "mirror"});

        assertEquals("NAME\n" +
                        "       mirror\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       mirror [options]\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The mirror command copies a remote repository into a local one.\n" +
                        "\n" +
                        "       The transfer options control how bytes move across the wire:\n" +
                        "\n" +
                        "       o      small chunks give frequent progress and cheap retries\n" +
                        "\n" +
                        "       o      large chunks cut per-request overhead on fast links\n" +
                        "\n" +
                        "       A gigabit link between data centers usually wants:\n" +
                        "\n" +
                        "           --chunk-size=1048576\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --chunk-size=<Integer>\n" +
                        "              bytes sent per request\n" +
                        "       \n" +
                        "              default: 65536\n",
                env.getOut().toString());
    }

    public static class Mirror {

        /**
         * The mirror command copies a remote repository into a local one.
         */
        @Command
        public void mirror(final Transfer transfer) {
        }
    }

    /**
     * The transfer options control how bytes move across the wire:
     *
     * - small chunks give frequent progress and cheap retries
     * - large chunks cut per-request overhead on fast links
     *
     * A gigabit link between data centers usually wants:
     *
     *     --chunk-size=1048576
     */
    @Options
    public static class Transfer {

        /**
         * @param chunkSize bytes sent per request
         */
        public Transfer(@Option("chunk-size") @Default("65536") final Integer chunkSize) {
        }
    }
}
