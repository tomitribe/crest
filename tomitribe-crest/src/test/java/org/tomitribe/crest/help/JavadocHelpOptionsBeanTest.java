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
 * A command declaring one @Options bean: the bean class javadoc supplies
 * its narrative, inlined into DESCRIPTION right after the command's own,
 * and the bean constructor's @param entries document the options the bean
 * contributes.
 */
public class JavadocHelpOptionsBeanTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Vault.class).main(env, new String[]{"help", "backup"});

        assertEquals("NAME\n" +
                        "       backup\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       backup [options] String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The  backup command copies a database to the archive host and prunes old copies as new\n" +
                        "       ones arrive.\n" +
                        "\n" +
                        "       The  retention  options decide how many copies survive the prune and how long any copy\n" +
                        "       may live.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --keep=<Integer>\n" +
                        "              copies preserved regardless of age\n" +
                        "       \n" +
                        "              default: 5\n" +
                        "\n" +
                        "       --days=<Integer>\n" +
                        "              age in days beyond which a copy is pruned\n" +
                        "       \n" +
                        "              default: 30\n",
                env.getOut().toString());
    }

    public static class Vault {

        /**
         * The backup command copies a database to the archive host and
         * prunes old copies as new ones arrive.
         *
         * @param database the database to copy
         */
        @Command
        public void backup(final Retention retention, final String database) {
        }
    }

    /**
     * The retention options decide how many copies survive the prune and
     * how long any copy may live.
     */
    @Options
    public static class Retention {

        /**
         * @param keep copies preserved regardless of age
         * @param days age in days beyond which a copy is pruned
         */
        public Retention(@Option("keep") @Default("5") final Integer keep,
                         @Option("days") @Default("30") final Integer days) {
        }
    }
}
