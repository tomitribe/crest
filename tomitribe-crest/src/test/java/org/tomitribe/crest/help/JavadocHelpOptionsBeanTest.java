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
 *
 * Also the ARGUMENTS presence rule: one documented positional puts the
 * section on the page listing every positional in order — the undocumented
 * host renders as a bare type entry so arity and order stay readable.  And
 * a bean constructor may contribute a positional of its own, documented by
 * the constructor's @param — the declaring site closest to the argument.
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
                        "       backup [options] String String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The  backup command copies a database to the archive host and prunes old copies as new\n" +
                        "       ones arrive.\n" +
                        "\n" +
                        "       The  retention  options decide how many copies survive the prune and how long any copy\n" +
                        "       may live.\n" +
                        "\n" +
                        "ARGUMENTS\n" +
                        "       String the database to copy\n" +
                        "\n" +
                        "       String\n" +
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

    /**
     * A positional supplied by an @Options bean's constructor is
     * documented at the bean, not the command
     */
    @Test
    public void beanPositional() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Vault.class).main(env, new String[]{"help", "restore"});

        assertEquals("NAME\n" +
                        "       restore\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       restore [options] String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The restore command copies a database back out of the archive.\n" +
                        "\n" +
                        "       The target options pick which copy comes back and where it lands.\n" +
                        "\n" +
                        "ARGUMENTS\n" +
                        "       String the archive file the copy is read from\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --verify, --no-verify\n" +
                        "              check the copy's checksum before restoring it\n",
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
        public void backup(final Retention retention, final String database, final String host) {
        }

        /**
         * The restore command copies a database back out of the archive.
         */
        @Command
        public void restore(final Target target) {
        }
    }

    /**
     * The target options pick which copy comes back and where it lands.
     */
    @Options
    public static class Target {

        /**
         * @param verify check the copy's checksum before restoring it
         * @param archive the archive file the copy is read from
         */
        public Target(@Option("verify") @Default("true") final Boolean verify,
                      final String archive) {
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
