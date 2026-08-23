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
import org.tomitribe.crest.api.GlobalOptions;
import org.tomitribe.crest.api.Option;

import static org.junit.Assert.assertEquals;

/**
 * A @GlobalOptions bean documented like any other: declared as a command
 * parameter its narrative and @param docs land in the command's man page;
 * registered with Main its options are documented in the root help
 * listing.
 */
public class JavadocHelpGlobalOptionsTest {

    @Test
    public void commandHelp() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Tools.class).main(env, new String[]{"help", "status"});

        assertEquals("NAME\n" +
                        "       status\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       status [options]\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The status command reports whether the service is up.\n" +
                        "\n" +
                        "       The verbosity options apply to every command in this tool.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --debug\n" +
                        "              print wire-level detail meant for maintainers\n",
                env.getOut().toString());
    }

    @Test
    public void rootHelp() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Tools.class, Verbosity.class).main(env, new String[]{"help"});

        assertEquals("Options:\n" +
                        "  --debug                  print wire-level detail meant for maintainers\n" +
                        "\n" +
                        "Commands: \n" +
                        "\n" +
                        "   help                                                             \n" +
                        "   status   The status command reports whether the service is up.   \n" +
                        "\n" +
                        "Help: \n" +
                        "\n" +
                        "   help --all       List all commands recursively\n" +
                        "   help <command>   Show detailed help for a command\n",
                env.getOut().toString());
    }

    public static class Tools {

        /**
         * The status command reports whether the service is up.
         */
        @Command
        public void status(final Verbosity verbosity) {
        }
    }

    /**
     * The verbosity options apply to every command in this tool.
     */
    @GlobalOptions
    public static class Verbosity {

        /**
         * @param debug print wire-level detail meant for maintainers
         */
        public Verbosity(@Option("debug") @Default("false") final Boolean debug) {
        }
    }
}
