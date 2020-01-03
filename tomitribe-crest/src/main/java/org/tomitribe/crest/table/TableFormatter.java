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
import org.tomitribe.util.PrintString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableFormatter {

    private final int width;
    private final Table table;
    private final Border border;
    private PrintString out;
    private List<Integer> widths;

    public TableFormatter(final int width, final Table table, final Border border) {
        this.width = width;
        this.table = table;
        this.border = border;

    }

    public String format() {
        out = new PrintString();
        this.widths = table.getColumnWidths()
                .stream()
                .map(Width::getMax)
                .collect(Collectors.toList());


        line(border.getFirst());
        int i = 0;

        if (table.hasHeadings()) {
            heading(table.getRow(i++).stream(), border.getRow());
            line(border.getHeader());
        }


        final List<Function<String, String>> rowPadding = getRowPaddingFunctions();
        final String separator = (border.getInner() != null) ? rowSeparator(border.getInner()) : null;
        for (; i < table.getRowCount(); i++) {
            final Line line = border.getRow();

            final Iterator<Function<String, String>> padding = rowPadding.iterator();

            final List<String> data = table.getRow(i).stream()
                    .map(s -> padding.next().apply(s))
                    .collect(Collectors.toList());

            out.println(line.getLeft() + Join.join(line.getInner(), data) + line.getRight());

            if (separator != null && i + 1 != table.getColumnCount()) {
                out.println(separator);
            }
        }

        line(border.getLast());

        return out.toString();
    }

    public List<Function<String, String>> getRowPaddingFunctions() {
        final List<Function<String, String>> rowPadding = new ArrayList<>();
        {
            final String padding = " ";
            for (int i = 0; i < table.getColumnCount(); i++) {

                final Table.Column column = table.getColumn(i);
                final Integer width = widths.get(i);

                if (column.isNumeric()) {
                    rowPadding.add(padRight(width, padding));
                } else {
                    rowPadding.add(padLeft(width, padding));
                }
            }
        }
        return rowPadding;
    }

    public void line(final Line line) {
        if (line == null) return;
        out.println(rowSeparator(line));
    }

    public String rowSeparator(final Line line) {
        final Iterator<Function<String, String>> padding = widths.stream()
                .map(integer -> padLeft(integer, line.getMiddle()))
                .collect(Collectors.toList())
                .iterator();

        final List<String> data = separatorRow()
                .map(s -> padding.next().apply(s))
                .collect(Collectors.toList());

        return line.getLeft() + Join.join(line.getInner(), data) + line.getRight();
    }

    public void row(final Stream<String> columns, final Line line) {
        final Iterator<Function<String, String>> padding = widths.stream()
                .map(integer -> padLeft(integer, " "))
                .collect(Collectors.toList())
                .iterator();

        final List<String> data = columns
                .map(s -> padding.next().apply(s))
                .collect(Collectors.toList());

        out.println(line.getLeft() + Join.join(line.getInner(), data) + line.getRight());
    }

    public void heading(final Stream<String> columns, final Line line) {
        final Iterator<Function<String, String>> padding = widths.stream()
                .map(integer -> center(integer, " "))
                .collect(Collectors.toList())
                .iterator();

        final List<String> data = columns
                .map(s -> padding.next().apply(s))
                .collect(Collectors.toList());

        out.println(line.getLeft() + Join.join(line.getInner(), data) + line.getRight());
    }

    public Stream<String> separatorRow() {
        return Stream.generate(() -> "")
                .limit(table.getColumnCount());
    }

    private Function<String, String> padLeft(final int width, final String sequence) {
        return s -> {
            final StringBuilder builder = new StringBuilder(width);
            builder.append(s);
            while (builder.length() < width) {
                builder.append(sequence);
            }
            return builder.toString();
        };
    }

    private Function<String, String> padRight(final int width, final String sequence) {
        return s -> {
            final StringBuilder builder = new StringBuilder(width);
            builder.append(s);
            while (builder.length() < width) {
                builder.insert(0, sequence);
            }
            return builder.toString();
        };
    }

    private Function<String, String> center(final int width, final String sequence) {
        return s -> {
            final StringBuilder builder = new StringBuilder(width);
            builder.append(s);
            while (builder.length() < width) {
                builder.append(sequence);

                if (builder.length() < width) {
                    builder.insert(0, sequence);
                }
            }
            return builder.toString();
        };
    }

    public Width getWidth() {
        return table.getWidth().add(getDelimiterWidth());
    }

    public Width getDelimiterWidth() {
        return border.getWidth(table.getColumnCount());
    }

}
