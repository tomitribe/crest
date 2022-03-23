/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.table;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.api.table.Border;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.util.IO;
import org.tomitribe.util.PrintString;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Revision$ $Date$
 */
public class TableBorderNoHeaderTest {

    @Test
    public void whitespaceSeparated() {
        assertBorder(Border.whitespaceSeparated);
    }

    @Test
    public void whitespaceCompact() {
        assertBorder(Border.whitespaceCompact);
    }

    @Test
    public void mysqlStyle() {
        assertBorder(Border.mysqlStyle);
    }

    @Test
    public void asciiSeparated() {
        assertBorder(Border.asciiSeparated);
    }

    @Test
    public void asciiCompact() {
        assertBorder(Border.asciiCompact);
    }

    @Test
    public void githubMarkdown() {
        assertBorder(Border.githubMarkdown);
    }

    @Test
    public void redditMarkdown() {
        assertBorder(Border.redditMarkdown);
    }

    @Test
    public void reStructuredTextGrid() {
        assertBorder(Border.reStructuredTextGrid);
    }

    @Test
    public void reStructuredTextSimple() {
        assertBorder(Border.reStructuredTextSimple);
    }

    @Test
    public void asciiDots() {
        assertBorder(Border.asciiDots);
    }

    @Test
    public void unicodeDouble() {
        assertBorder(Border.unicodeDouble);
    }

    @Test
    public void unicodeSingle() {
        assertBorder(Border.unicodeSingle);
    }

    @Test
    public void unicodeSingleSeparated() {
        assertBorder(Border.unicodeSingleSeparated);
    }

    private void assertBorder(final Border border) {

        try {

            final Main main = new Main(Foo.class);
            final Object override = main.exec("override", "--table-border=" + border);

            final String actual = asString((PrintOutput) override);

            final URL resource = this.getClass().getClassLoader().getResource(this.getClass().getSimpleName() + "/" + border + ".txt");
            final String expected = IO.slurp(resource);
            assertEquals(expected, actual);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String asString(final PrintOutput object) throws IOException {
        final PrintString output = new PrintString();
        object.write(output);
        return output.toString();
    }

    public static class Foo {

        private final List<Branch> branches;

        public Foo() {
            branches = Arrays.asList(
                    new Branch(9, "Apache TomEE", "7.0.x", "2016-05-17"),
                    new Branch(523456789, "Tomcat", "9.0.x", "2018-01-17"),
                    new Branch(14, "Apache ActiveMQ Classic", "5.17.x", "2022-03-09")
            );
        }

        @Command
        @Table(header = false)
        public List<Branch> override(@Option("table-border") final Border border) {
            return branches;
        }
    }

    public static class Branch {
        private final int id;
        private final String project;
        private final String version;
        private final LocalDate releaseDate;

        public Branch(final int id, final String project, final String version, final String releaseDate) {
            this.id = id;
            this.project = project;
            this.version = version;
            this.releaseDate = LocalDate.parse(releaseDate);
        }

        public int getId() {
            return id;
        }

        public String getProject() {
            return project;
        }

        public String getVersion() {
            return version;
        }

        public LocalDate getReleaseDate() {
            return releaseDate;
        }
    }
}
