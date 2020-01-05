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
package org.tomitribe.crest.table;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Tests the formatting of our out-of-the-box table formats
 */
public class TableFormatsTest {

    private final Data data = Data.builder().headings(true)
            .row("Col1", "Col2", "Col3", "Numeric Column")
            .row("Value 1", "Value 2", "123", "10.0")
            .row("Separate", "cols", "with a tab or 4 spaces", "-2,027.1")
            .row("This is a row with only one cell")
            .build();

    @Test
    public void asciiCompact() {
        assertTable(Border::asciiCompact, "" +
                "               Col1                  Col2              Col3            Numeric Column \n" +
                "---------------------------------- --------- ------------------------ ----------------\n" +
                " Value 1                            Value 2   123                                10.0 \n" +
                " Separate                           cols      with a tab or 4 spaces         -2,027.1 \n" +
                " This is a row with only one cell                                                     \n");
    }

    @Test
    public void asciiDots() {
        assertTable(Border::asciiDots, "" +
                "........................................................................................\n" +
                ":               Col1               :  Col2   :          Col3          : Numeric Column :\n" +
                ":..................................:.........:........................:................:\n" +
                ": Value 1                          : Value 2 : 123                    :           10.0 :\n" +
                ": Separate                         : cols    : with a tab or 4 spaces :       -2,027.1 :\n" +
                ": This is a row with only one cell :         :                        :                :\n" +
                ":..................................:.........:........................:................:\n");
    }

    @Test
    public void asciiSeparated() {
        assertTable(Border::asciiSeparated, "" +
                "+==================================+=========+========================+================+\n" +
                "|               Col1               |  Col2   |          Col3          | Numeric Column |\n" +
                "+==================================+=========+========================+================+\n" +
                "| Value 1                          | Value 2 | 123                    |           10.0 |\n" +
                "+----------------------------------+---------+------------------------+----------------+\n" +
                "| Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |\n" +
                "+----------------------------------+---------+------------------------+----------------+\n" +
                "| This is a row with only one cell |         |                        |                |\n" +
                "+----------------------------------+---------+------------------------+----------------+\n");
    }

    @Test
    public void mysqlStyle() {
        assertTable(Border::mysqlStyle, "" +
                "+----------------------------------+---------+------------------------+----------------+\n" +
                "|               Col1               |  Col2   |          Col3          | Numeric Column |\n" +
                "+----------------------------------+---------+------------------------+----------------+\n" +
                "| Value 1                          | Value 2 | 123                    |           10.0 |\n" +
                "| Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |\n" +
                "| This is a row with only one cell |         |                        |                |\n" +
                "+----------------------------------+---------+------------------------+----------------+\n");
    }

    @Test
    public void githubMarkdown() {
        assertTable(Border::githubMarkdown, "" +
                "|               Col1               |  Col2   |          Col3          | Numeric Column |\n" +
                "|----------------------------------|---------|------------------------|----------------|\n" +
                "| Value 1                          | Value 2 | 123                    |           10.0 |\n" +
                "| Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |\n" +
                "| This is a row with only one cell |         |                        |                |\n");
    }

    @Test
    public void redditMarkdown() {
        assertTable(Border::redditMarkdown, "" +
                "               Col1               |  Col2   |          Col3          | Numeric Column \n" +
                "----------------------------------|---------|------------------------|----------------\n" +
                " Value 1                          | Value 2 | 123                    |           10.0 \n" +
                " Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 \n" +
                " This is a row with only one cell |         |                        |                \n");
    }

    @Test
    public void reStructuredTextGrid() {
        assertTable(Border::reStructuredTextGrid, "" +
                "+----------------------------------+---------+------------------------+----------------+\n" +
                "|               Col1               |  Col2   |          Col3          | Numeric Column |\n" +
                "+==================================+=========+========================+================+\n" +
                "| Value 1                          | Value 2 | 123                    |           10.0 |\n" +
                "| Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |\n" +
                "| This is a row with only one cell |         |                        |                |\n" +
                "+----------------------------------+---------+------------------------+----------------+\n");
    }

    @Test
    public void reStructuredTextSimple() {
        assertTable(Border::reStructuredTextSimple, "" +
                "================================== ========= ======================== ================\n" +
                "               Col1                  Col2              Col3            Numeric Column \n" +
                "================================== ========= ======================== ================\n" +
                " Value 1                            Value 2   123                                10.0 \n" +
                " Separate                           cols      with a tab or 4 spaces         -2,027.1 \n" +
                " This is a row with only one cell                                                     \n" +
                "================================== ========= ======================== ================\n");
    }

    @Test
    public void unicodeDouble() {
        assertTable(Border::unicodeDouble, "" +
                "╔══════════════════════════════════╦═════════╦════════════════════════╦════════════════╗\n" +
                "║               Col1               ║  Col2   ║          Col3          ║ Numeric Column ║\n" +
                "╠══════════════════════════════════╬═════════╬════════════════════════╬════════════════╣\n" +
                "║ Value 1                          ║ Value 2 ║ 123                    ║           10.0 ║\n" +
                "║ Separate                         ║ cols    ║ with a tab or 4 spaces ║       -2,027.1 ║\n" +
                "║ This is a row with only one cell ║         ║                        ║                ║\n" +
                "╚══════════════════════════════════╩═════════╩════════════════════════╩════════════════╝\n");
    }

    @Test
    public void unicodeSingle() {
        assertTable(Border::unicodeSingle, "" +
                "┌──────────────────────────────────┬─────────┬────────────────────────┬────────────────┐\n" +
                "│               Col1               │  Col2   │          Col3          │ Numeric Column │\n" +
                "├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤\n" +
                "│ Value 1                          │ Value 2 │ 123                    │           10.0 │\n" +
                "│ Separate                         │ cols    │ with a tab or 4 spaces │       -2,027.1 │\n" +
                "│ This is a row with only one cell │         │                        │                │\n" +
                "└──────────────────────────────────┴─────────┴────────────────────────┴────────────────┘\n");
    }

    @Test
    public void unicodeSingleSeparated() {
        assertTable(Border::unicodeSingleSeparated, "" +
                "┌──────────────────────────────────┬─────────┬────────────────────────┬────────────────┐\n" +
                "│               Col1               │  Col2   │          Col3          │ Numeric Column │\n" +
                "├══════════════════════════════════┼═════════┼════════════════════════┼════════════════┤\n" +
                "│ Value 1                          │ Value 2 │ 123                    │           10.0 │\n" +
                "├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤\n" +
                "│ Separate                         │ cols    │ with a tab or 4 spaces │       -2,027.1 │\n" +
                "├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤\n" +
                "│ This is a row with only one cell │         │                        │                │\n" +
                "└──────────────────────────────────┴─────────┴────────────────────────┴────────────────┘\n");
    }

    @Test
    public void whitespaceCompact() {
        assertTable(Border::whitespaceCompact, "" +
                "              Col1                  Col2              Col3            Numeric Column\n" +
                "                                                                                    \n" +
                "Value 1                            Value 2   123                                10.0\n" +
                "Separate                           cols      with a tab or 4 spaces         -2,027.1\n" +
                "This is a row with only one cell                                                    \n");
    }

    @Test
    public void whitespaceSeparated() {
        assertTable(Border::whitespaceSeparated, "" +
                "              Col1                  Col2              Col3            Numeric Column\n" +
                "                                                                                    \n" +
                "Value 1                            Value 2   123                                10.0\n" +
                "                                                                                    \n" +
                "Separate                           cols      with a tab or 4 spaces         -2,027.1\n" +
                "                                                                                    \n" +
                "This is a row with only one cell                                                    \n");
    }

    public void assertTable(final Supplier<Border.Builder> border, final String expected) {
        final Taable taable = new Taable(data, border.get().build(), 300);
        final String actual = taable.format();
        Assert.assertEquals(expected, actual);
    }
}
