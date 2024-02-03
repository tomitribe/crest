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

import org.tomitribe.util.Join;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Border {
    /**
     * The very first line of the table before any header text
     */
    private final Line first;

    /**
     * The very last line of the table after all content
     */
    private final Line last;


    /**
     * The very first line of the table after any header text
     */
    private final Line header;

    /**
     * The inside lines between any rows
     */
    private final Line inner;

    /**
     * The row itself.  Only the left, right and inner values
     * of this Line instance will be used in rendering tables
     */
    private final Line row;

    private final Function<String, String> escape;

    public Border(final Line first, final Line last, final Line header, final Line inner, final Line row, final Function<String, String> escape) {
        this.first = first;
        this.last = last;
        this.header = header;
        this.inner = inner;
        this.row = row;
        this.escape = escape;
    }

    public String escape(final String value) {
        return escape.apply(value);
    }

    public String getRowFormat(final List<Data.Column> columns) {
        final List<String> formats = columns.stream().map(column -> {
            final int width = column.getWidth().getMax();
            return column.isNumeric() ? "%" + width + "s" : "%-" + width + "s";
        }).collect(Collectors.toList());

        final Line row = this.getRow();

        return row.getLeft() +
                Join.join(row.getInner(), formats) +
                row.getRight();
    }

    public Line getFirst() {
        return first;
    }

    public Line getLast() {
        return last;
    }

    public Line getHeader() {
        return header;
    }

    public Line getInner() {
        return inner;
    }

    public Line getRow() {
        return row;
    }

    public Width getWidth(final int columns) {
        final int width = row.getLeft().length() + row.getRight().length() + (row.getInner().length() * (Math.max(0, columns - 1)));
        return new Width(width, width);
    }

    /**
     * <pre>
     *                Col1                  Col2              Col3            Numeric Column
     *
     *  Value 1                            Value 2   123                                10.0
     *
     *  Separate                           cols      with a tab or 4 spaces         -2,027.1
     *
     *  This is a row with only one cell
     * </pre>
     */
    public static Border.Builder whitespaceSeparated() {
        return builder()
                .first(null)
                .header("", " ", "   ", "") // blank line after header
                .row("", "   ", "")
                .inner("", " ", "   ", "") // blank line between rows
                .last(null)
                ;
    }

    /**
     * <pre>
     *                Col1                  Col2              Col3            Numeric Column
     *
     *  Value 1                            Value 2   123                                10.0
     *  Separate                           cols      with a tab or 4 spaces         -2,027.1
     *  This is a row with only one cell
     * </pre>
     */
    public static Border.Builder whitespaceCompact() {
        return builder()
                .first(null) // no top border
                .inner(null) // no lines between rows
                .last(null) // no bottom border
                .header("", " ", "   ", "") // blank line after header
                .row("", "   ", "")
                ;
    }

    /**
     * <pre>
     * +----------------------------------+---------+------------------------+----------------+
     * |               Col1               |  Col2   |          Col3          | Numeric Column |
     * +----------------------------------+---------+------------------------+----------------+
     * | Value 1                          | Value 2 | 123                    |           10.0 |
     * | Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |
     * | This is a row with only one cell |         |                        |                |
     * +----------------------------------+---------+------------------------+----------------+
     * </pre>
     */
    public static Border.Builder mysqlStyle() {
        return builder()
                .first("+-", "-", "-+-", "-+")
                .header("+-", "-", "-+-", "-+")
                .row("| ", " | ", " |")
                .last("+-", "-", "-+-", "-+")
                .inner(null) // no lines between rows
                ;
    }

    /**
     * <pre>
     * +==================================+=========+========================+================+
     * |               Col1               |  Col2   |          Col3          | Numeric Column |
     * +==================================+=========+========================+================+
     * | Value 1                          | Value 2 | 123                    |           10.0 |
     * +----------------------------------+---------+------------------------+----------------+
     * | Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |
     * +----------------------------------+---------+------------------------+----------------+
     * | This is a row with only one cell |         |                        |                |
     * +----------------------------------+---------+------------------------+----------------+
     * </pre>
     */
    public static Border.Builder asciiSeparated() {
        return builder()
                .first("+=", "=", "=+=", "=+")
                .header("+=", "=", "=+=", "=+")
                .row("| ", " | ", " |")
                .inner("+-", "-", "-+-", "-+")
                .last("+-", "-", "-+-", "-+")
                ;
    }

    /**
     * <pre>
     *                 Col1                  Col2              Col3            Numeric Column
     *  ---------------------------------- --------- ------------------------ ----------------
     *   Value 1                            Value 2   123                                10.0
     *   Separate                           cols      with a tab or 4 spaces         -2,027.1
     *   This is a row with only one cell
     * </pre>
     */
    public static Border.Builder asciiCompact() {
        return builder()
                .first(null) // no top border
                .inner(null) // no lines between rows
                .last(null) // no bottom border
                .header("-", "-", "- -", "-")
                .row(" ", "   ", " ")
                ;
    }

    /**
     * <pre>
     * |               Col1               |  Col2   |          Col3          | Numeric Column |
     * |----------------------------------|---------|------------------------|----------------|
     * | Value 1                          | Value 2 | 123                    |           10.0 |
     * | Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |
     * | This is a row with only one cell |         |                        |                |
     * </pre>
     */
    public static Border.Builder githubMarkdown() {
        return builder()
                .first(null) // no top border
                .inner(null) // no lines between rows
                .last(null) // no bottom border
                .header("|-", "-", "-|-", "-|")
                .row("| ", " | ", " |")
                ;
    }

    /**
     * <pre>
     *                 Col1               |  Col2   |          Col3          | Numeric Column
     *  ----------------------------------|---------|------------------------|----------------
     *   Value 1                          | Value 2 | 123                    |           10.0
     *   Separate                         | cols    | with a tab or 4 spaces |       -2,027.1
     *   This is a row with only one cell |         |                        |
     * </pre>
     */
    public static Border.Builder redditMarkdown() {
        return builder()
                .first(null) // no top border
                .inner(null) // no lines between rows
                .last(null) // no bottom border
                .header("-", "-", "-|-", "-")
                .row(" ", " | ", " ")
                ;
    }

    /**
     * <pre>
     * +----------------------------------+---------+------------------------+----------------+
     * |               Col1               |  Col2   |          Col3          | Numeric Column |
     * +==================================+=========+========================+================+
     * | Value 1                          | Value 2 | 123                    |           10.0 |
     * | Separate                         | cols    | with a tab or 4 spaces |       -2,027.1 |
     * | This is a row with only one cell |         |                        |                |
     * +----------------------------------+---------+------------------------+----------------+
     * </pre>
     */
    public static Border.Builder reStructuredTextGrid() {
        return builder()
                .first("+-", "-", "-+-", "-+")
                .header("+=", "=", "=+=", "=+")
                .row("| ", " | ", " |")
                .last("+-", "-", "-+-", "-+")
                .inner(null) // no lines between rows
                ;
    }

    /**
     * <pre>
     *  ================================== ========= ======================== ================
     *                 Col1                  Col2              Col3            Numeric Column
     *  ================================== ========= ======================== ================
     *   Value 1                            Value 2   123                                10.0
     *   Separate                           cols      with a tab or 4 spaces         -2,027.1
     *   This is a row with only one cell
     *  ================================== ========= ======================== ================
     * </pre>
     */
    public static Border.Builder reStructuredTextSimple() {
        return builder()
                .first("=", "=", "= =", "=")
                .header("=", "=", "= =", "=")
                .row(" ", "   ", " ")
                .last("=", "=", "= =", "=")
                .inner(null) // no lines between rows
                ;
    }

    /**
     * <pre>
     * ........................................................................................
     * :               Col1               :  Col2   :          Col3          : Numeric Column :
     * :..................................:.........:........................:................:
     * : Value 1                          : Value 2 : 123                    :           10.0 :
     * : Separate                         : cols    : with a tab or 4 spaces :       -2,027.1 :
     * : This is a row with only one cell :         :                        :                :
     * :..................................:.........:........................:................:
     * </pre>
     */
    public static Border.Builder asciiDots() {
        return builder()
                .first("..", ".", "...", "..")
                .header(":.", ".", ".:.", ".:")
                .row(": ", " : ", " :")
                .last(":.", ".", ".:.", ".:")
                .inner(null) // no lines between rows
                ;
    }

    /**
     * <pre>
     * ╔══════════════════════════════════╦═════════╦════════════════════════╦════════════════╗
     * ║               Col1               ║  Col2   ║          Col3          ║ Numeric Column ║
     * ╠══════════════════════════════════╬═════════╬════════════════════════╬════════════════╣
     * ║ Value 1                          ║ Value 2 ║ 123                    ║           10.0 ║
     * ║ Separate                         ║ cols    ║ with a tab or 4 spaces ║       -2,027.1 ║
     * ║ This is a row with only one cell ║         ║                        ║                ║
     * ╚══════════════════════════════════╩═════════╩════════════════════════╩════════════════╝
     * </pre>
     */
    public static Border.Builder unicodeDouble() {
        return builder()
                .first("╔═", "═", "═╦═", "═╗")
                .header("╠═", "═", "═╬═", "═╣")
                .row("║ ", " ║ ", " ║")
                .last("╚═", "═", "═╩═", "═╝")
                .inner(null) // no lines between rows
                ;
    }

    /**
     * <pre>
     * ┌──────────────────────────────────┬─────────┬────────────────────────┬────────────────┐
     * │               Col1               │  Col2   │          Col3          │ Numeric Column │
     * ├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤
     * │ Value 1                          │ Value 2 │ 123                    │           10.0 │
     * │ Separate                         │ cols    │ with a tab or 4 spaces │       -2,027.1 │
     * │ This is a row with only one cell │         │                        │                │
     * └──────────────────────────────────┴─────────┴────────────────────────┴────────────────┘
     * </pre>
     */
    public static Border.Builder unicodeSingle() {
        return builder()
                .first("┌─", "─", "─┬─", "─┐")
                .header("├─", "─", "─┼─", "─┤")
                .row("│ ", " │ ", " │")
                .last("└─", "─", "─┴─", "─┘")
                .inner(null) // no lines between rows
                ;
    }

    /**
     * <pre>
     * ┌──────────────────────────────────┬─────────┬────────────────────────┬────────────────┐
     * │               Col1               │  Col2   │          Col3          │ Numeric Column │
     * ├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤
     * │ Value 1                          │ Value 2 │ 123                    │           10.0 │
     * ├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤
     * │ Separate                         │ cols    │ with a tab or 4 spaces │       -2,027.1 │
     * ├──────────────────────────────────┼─────────┼────────────────────────┼────────────────┤
     * │ This is a row with only one cell │         │                        │                │
     * └──────────────────────────────────┴─────────┴────────────────────────┴────────────────┘
     * </pre>
     */
    public static Border.Builder unicodeSingleSeparated() {
        return builder()
                .first("┌─", "─", "─┬─", "─┐")
                .header("├═", "═", "═┼═", "═┤")
                .inner("├─", "─", "─┼─", "─┤")
                .row("│ ", " │ ", " │")
                .last("└─", "─", "─┴─", "─┘")
                ;
    }

    public static Border.Builder tsv() {
        return builder()
                .first(null)
                .header(null)
                .inner(null)
                .row(Line.builder().left("").inner("\t").right("").padded(false))
                .last(null)
                .escape(s -> s.replace("\t", "    "));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Line.Builder first = Line.builder();
        private Line.Builder last = Line.builder();
        private Line.Builder header = Line.builder();
        private Line.Builder inner = Line.builder();
        private Line.Builder middle = Line.builder();

        private Function<String, String> escape = Function.identity();

        private Builder() {
        }

        public List<Line.Builder> all() {
            return Arrays.asList(
                    first,
                    last,
                    header,
                    inner,
                    middle
            );
        }


        public Line.Builder first() {
            return this.first.all("").inner("   ");
        }

        public Line.Builder last() {
            return this.last;
        }

        public Line.Builder header() {
            return this.header;
        }

        public Line.Builder inner() {
            return this.inner;
        }

        public Line.Builder row() {
            return this.middle;
        }

        public Builder first(final Line.Builder first) {
            this.first = first;
            return this;
        }

        public Builder last(final Line.Builder last) {
            this.last = last;
            return this;
        }

        public Builder header(final Line.Builder header) {
            this.header = header;
            return this;
        }

        public Builder inner(final Line.Builder inner) {
            this.inner = inner;
            return this;
        }

        public Builder row(final Line.Builder middle) {
            this.middle = middle;
            return this;
        }

        public Builder first(final String left, final String middle, final String inner, final String right) {
            this.first.left(left).middle(middle).inner(inner).right(right);
            return this;
        }

        public Builder header(final String left, final String middle, final String inner, final String right) {
            this.header.left(left).middle(middle).inner(inner).right(right);
            return this;
        }

        public Builder row(final String left, final String inner, final String right) {
            this.middle.left(left).inner(inner).right(right);
            return this;
        }

        public Builder inner(final String left, final String middle, final String inner, final String right) {
            this.inner.left(left).middle(middle).inner(inner).right(right);
            return this;
        }

        public Builder last(final String left, final String middle, final String inner, final String right) {
            this.last.left(left).middle(middle).inner(inner).right(right);
            return this;
        }

        public Builder escape(final Function<String, String> escape) {
            this.escape = escape;
            return this;
        }

        public Border build() {
            return new Border(
                    first != null ? first.build() : null,
                    last != null ? last.build() : null,
                    header != null ? header.build() : null,
                    inner != null ? inner.build() : null,
                    middle != null ? middle.build() : null,
                    escape);
        }
    }
}
