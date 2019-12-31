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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.environments.SystemEnvironment;
import org.tomitribe.crest.val.Directory;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GenericsInMethodSignatureTest {

    @Test
    public void test() throws Exception {
        final PrintString out = new PrintString();
        new Main(Commands.class).main(new SystemEnvironment() {
            @Override
            public PrintStream getOutput() {
                return out;
            }
        }, new String[]{"help", "commit"});
        assertEquals("NAME\n" +
                        "       commit\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       commit [options] File\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       Commit the changes from the directory into the repository specified.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --all\n" +
                        "\n" +
                        "       --message=<String>\n" +
                        "              Note also that host and module references don't require a trailing slash\n" +
                        "              to copy the contents of the default directory.   For  example,  both  of\n" +
                        "              these copy the remote directory's contents into \"/dest\":\n" +
                        "       \n" +
                        "                  rsync -av host: /dest\n" +
                        "                  rsync -av host::module /dest\n" +
                        "       \n" +
                        "              You  can  also  use  rsync in local-only mode, where both the source and\n" +
                        "              destination don't have a ':' in the name.  In this case it behaves  like\n" +
                        "              an improved copy command.\n" +
                        "\n" +
                        "       --strings=<String[]>\n" +
                        "\n",
                out.toString());
    }

    public static class Commands {
        /**
         * Commit the changes from the directory into the repository specified.
         *
         * @param text Note also that host and module references don't require a trailing slash
         *             to copy the contents of the default directory. For example, both of these
         *             copy the remote directory's contents into "/dest":
         *
         *                 rsync -av host: /dest
         *                 rsync -av host::module /dest
         *
         *             You can also use rsync in local-only mode, where both the source and
         *             destination don't have a ':' in the name. In this case it behaves
         *             like an improved copy command.
         *
         * @param repo the git repository cloned to the local system where changes should be committed
         */
        @Command("commit")
        public void commit(@Option("all") final boolean everything,
                           @Option("strings") final List<String> strings,
                           @Required @Option("message") final String text,
                           @Directory final File repo) {

        }
    }

}
