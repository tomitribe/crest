/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest.help;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.val.Directory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class DefaultsShownTest {

    @Test
    public void full() throws Exception {
        final TestEnvironment env = new TestEnvironment().env("NOCOLOR", "");
        new Main(Commands.class).main(env, new String[]{"help", "commit"});
        assertEquals("NAME\n" +
                        "       commit\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       commit [options] File\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       Stores the current contents of the index in a new commit along with a log message from\n" +
                        "       the user describing the changes.  The --dry-run option can be used to obtain a summary\n" +
                        "       of  what is included by any of the above for the next commit by giving the same set of\n" +
                        "       parameters (options and paths).\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --elapsed=<TimeUnit>\n" +
                        "              default:  MINUTES.    enum:  NANOSECONDS,  MICROSECONDS, MILLISECONDS, SECONDS,\n" +
                        "              MINUTES, HOURS, DAYS\n" +
                        "\n" +
                        "       --message=<String>\n" +
                        "              a message detailing the commit\n" +
                        "       \n" +
                        "              default: Updates and other fixes\n" +
                        "\n" +
                        "       --time=<TimeUnit>\n" +
                        "              indicates all changes should be committed, including deleted files\n" +
                        "       \n" +
                        "              enum:  NANOSECONDS,  MICROSECONDS,  MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS\n" +
                        "",
                env.getOut().toString());
    }

    public static class Commands {

        /**
         * Stores the current contents of the index in a new commit along with a log
         * message from the user describing the changes. The --dry-run option can be
         * used to obtain a summary of what is included by any of the above for the
         * next commit by giving the same set of parameters (options and paths).
         *
         * @param times indicates all changes should be committed, including deleted files
         * @param text a message detailing the commit
         * @param repo the git repository cloned to the local system where changes should be committed
         */
        @Command("commit")
        public void commit(@Option("time") final TimeUnit times,
                           @Option("elapsed") @Default("MINUTES") final TimeUnit elapsed,
                           @Required @Option("message") @Default("Updates and other fixes") final String text,
                           @Directory final File repo) {

        }
    }

}
