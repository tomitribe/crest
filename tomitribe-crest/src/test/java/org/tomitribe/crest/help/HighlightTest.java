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
import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HighlightTest {

    @Test
    public void flags() {
        final List<Option> options = new ArrayList<>();
        options.add(new Option("--assignee, -a=<String[]>", null));
        options.add(new Option("--body, -b=<String>", null));
        options.add(new Option("--body-formatter, -g=<String>", null));
        options.add(new Option("--assign", null));
        options.add(new Option("--file, -f=<File>", null));
        options.add(new Option("--a", null));

        final List<String> flags = Highlight.flags(options);
        assertEquals("--body-formatter\n" +
                "--assignee\n" +
                "--assign\n" +
                "--file\n" +
                "--body\n" +
                "--a\n" +
                "-g\n" +
                "-f\n" +
                "-b\n" +
                "-a", Join.join("\n", flags));
    }

    @Test
    public void highlightFlags() {
        final List<Option> options = new ArrayList<>();
        options.add(new Option("--assignee, -a=<String[]>", null));
        options.add(new Option("--body, -b=<String>", null));
        options.add(new Option("--body-formatter, -g=<String>", null));
        options.add(new Option("--assign", null));
        options.add(new Option("--file, -f=<File>", null));
        options.add(new Option("--a", null));

        final Highlight highlight = new Highlight(options);

        /*
         * Can we support no leading or trailing characters?
         */
        assertEquals("\u001B[0m\u001B[1m--assignee\u001B[0m", highlight.matches("--assignee"));

        /*
         * Are we smart enough to avoid partial matches
         */
        assertEquals("the flags --ab and --body-wash are not valid", highlight.matches("the flags --ab and --body-wash are not valid"));

        /*
         * We can highlight the short versions with precision
         */
        assertEquals("the flags " +
                        "\u001B[0m\u001B[1m-a\u001B[0m and " +
                        "\u001B[0m\u001B[1m-b\u001B[0m are " +
                        "valid but -file and --body-double are not",
                highlight.matches("the flags -a and -b are valid but -file and --body-double are not"));

        /*
         * More complete example
         */
        assertEquals("foo \u001B[0m\u001B[1m--assignee\u001B[0m" +
                        " bar baz \u001B[0m\u001B[1m--a\u001B[0m" +
                        " and \u001B[0m\u001B[1m--file\u001B[0m" +
                        " with a \u001B[0m\u001B[1m--body\u001B[0m" +
                        " and \u001B[0m\u001B[1m--body-formatter\u001B[0m" +
                        " and --body-nothing and \u001B[0m\u001B[1m--a\u001B[0m",
                highlight.matches("foo --assignee bar baz --a and " +
                        "--file with a --body and --body-formatter and --body-nothing and --a"));
    }

    @Test
    public void highlightCode() {
        final List<Option> options = new ArrayList<>();
        options.add(new Option("--assignee, -a=<String[]>", null));
        options.add(new Option("--body, -b=<String>", null));

        final Highlight highlight = new Highlight(options);

        /*
         * Can we support no leading or trailing characters?
         */
        assertEquals("send a " +
                        "\u001B[0m\u001B[1mGET\u001B[0m request to " +
                        "\u001B[0m\u001B[1m/some/place\u001B[0m",
                highlight.matches("send a `GET` request to `/some/place`"));

    }

    @Test
    public void highlightBoth() {
        final List<Option> options = new ArrayList<>();
        options.add(new Option("--assignee, -a=<String[]>", null));
        options.add(new Option("--body, -b=<String>", null));

        final Highlight highlight = new Highlight(options);

        /*
         * Can we support no leading or trailing characters?
         */
        assertEquals("send a " +
                        "\u001B[0m\u001B[1mGET\u001B[0m request to " +
                        "\u001B[0m\u001B[1m/some/place\u001B[0m with " +
                        "\u001B[0m\u001B[1m--body\u001B[0m pointing to a json file",
                highlight.matches("send a `GET` request to `/some/place` with --body pointing to a json file"));

    }

    @Test
    public void dontDoubleHighlight() {
        final List<Option> options = new ArrayList<>();
        options.add(new Option("--assignee, -a=<String[]>", null));
        options.add(new Option("--body, -b=<String>", null));

        final Highlight highlight = new Highlight(options);

        /*
         * If someone surrounds a flag with `` we shouldn't highlight it twice
         */
        assertEquals("use " +
                        "\u001B[0m\u001B[1m--body\u001B[0m" +
                        " to point to a json file",
                highlight.matches("use `--body` to point to a json file"));

        /*
         * Make sure people can highlight specific setting recommendations
         */
        assertEquals("use \u001B[0m\u001B[1m--body=false\u001B[0m when in doubt",
                highlight.matches("use `--body=false` when in doubt"));

    }
}