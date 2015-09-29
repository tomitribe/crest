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
package org.tomitribe.crest.cli.api.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

public class Table {
    private static final String COL_SEP = "|";
    private static final String HEADER_CHAR = "=";
    private static final String LINE_CHAR = "-";
    private static final char EMPTY_CHAR = ' ';

    private final List<Row> rows = new LinkedList<Row>();
    private final String cr;
    private final Row header;

    public Table(final String... header) {
        this.cr = System.lineSeparator();
        this.header = new Row(asList(header));
    }

    public Table row(final Object... columns) {
        if (!rows.isEmpty() && rows.iterator().next().columns.length != columns.length) {
            throw new IllegalArgumentException("columns should have all the same size");
        }

        final Collection<String> str = new ArrayList<String>(columns.length);
        for (final Object o : columns) {
            str.add(String.valueOf(o));
        }
        rows.add(new Row(str));
        return this;
    }

    public Table sort() {
        Collections.sort(rows);
        return this;
    }

    public void print(final PrintStream printStream, final boolean horizontal) {
        if (horizontal) {
            printHorizontal(printStream);
        } else {
            printVertical(printStream);
        }
    }

    public void printHorizontal(final PrintStream printStream) {
        final List<Integer> width = new ArrayList<Integer>(rows.size());
        for (int i = 0; i < header.columns.length; i++) {
            if (width.size() <= i) {
                width.add(header.columns[i].length());
            }
        }
        for (final Row r : rows) {
            for (int i = 0; i < r.columns.length; i++) {
                width.set(i, Math.max(width.get(i), r.columns[i].length()));
            }
        }

        int columnWidth = 0;
        for (final Integer i : width) {
            columnWidth += i;
        }

        sepLineHorizontal(printStream, columnWidth, HEADER_CHAR);
        printStream.print(COL_SEP);
        printLineHorizontal(printStream, width, header.columns);
        printStream.print(cr);
        sepLineHorizontal(printStream, columnWidth, HEADER_CHAR);
        for (final Row line : rows) {
            printStream.print(COL_SEP);
            printLineHorizontal(printStream, width, line.columns);
            printStream.print(cr);
        }
        sepLineHorizontal(printStream, columnWidth, LINE_CHAR);
        printStream.print(cr);
    }

    private void printLineHorizontal(final PrintStream printStream, final List<Integer> width, final String[] items) {
        for (int i = 0; i < items.length; i++) {
            printStream.print(EMPTY_CHAR + center(items[i], width.get(i)) + EMPTY_CHAR + COL_SEP);
        }
    }

    private void sepLineHorizontal(final PrintStream printStream, final int width, final String sep) {
        for (int i = 0; i < width + 1 + header.columns.length * 3; i++) {
            printStream.print(sep);
        }
        printStream.print(cr);
    }

    public void printVertical(final PrintStream printStream) {
        final int headerWidth = lineWidth(header);
        for (final Row line : rows) {
            final int width = lineWidth(line);

            sepLineVertical(printStream, width, headerWidth);
            for (int idx = 0; idx < header.columns.length; idx++) {
                printStream.print(COL_SEP + COL_SEP + EMPTY_CHAR);
                printStream.print(center(header.columns[idx], headerWidth));
                printStream.print(EMPTY_CHAR + COL_SEP + COL_SEP);

                printStream.print(EMPTY_CHAR + center(line.columns[idx], width) + EMPTY_CHAR + COL_SEP);
                printStream.print(cr);
            }
            sepLineVertical(printStream, width, headerWidth);
            printStream.print(cr);
        }
    }

    private int lineWidth(final Row line) {
        int width = 0;
        for (final String h : line.columns) {
            if (h.length() > width) {
                width = h.length();
            }
        }
        return width;
    }

    private void printLineVertical(final PrintStream printStream, final List<Integer> width, final String[] items) {
        for (int i = 0; i < items.length; i++) {
            printStream.print(EMPTY_CHAR + center(items[i], width.get(i)) + EMPTY_CHAR + COL_SEP);
        }
    }

    private void sepLineVertical(final PrintStream printStream, final int width, final int headerWidth) {
        for (int i = 0; i < headerWidth + 5; i++) {
            printStream.print(HEADER_CHAR);
        }
        for (int i = 0; i < width + 4; i++) {
            printStream.print(LINE_CHAR);
        }
        printStream.print(cr);
    }

    private String center(final String text, final int len) {
        if (text.length() >= len) {
            return text;
        }

        final StringBuilder builder = new StringBuilder(len);
        final int empty = len - text.length();
        for (int i = 0; i < empty / 2; i++) {
            builder.append(EMPTY_CHAR);
        }
        builder.append(text);
        for (int i = 0; i < empty - empty / 2; i++) {
            builder.append(EMPTY_CHAR);
        }
        return builder.toString();
    }

    public boolean hasContent() {
        return !rows.isEmpty();
    }

    private static class Row implements Comparable<Row> {
        private final String[] columns;

        private Row(final Collection<String> columns) {
            this.columns = columns.toArray(new String[columns.size()]);
        }

        @Override
        public int compareTo(final Row o) {
            for (int i = 0; i < columns.length; i++) {
                int cmp = String.valueOf(columns[i]).compareTo(String.valueOf(o.columns[i]));
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        }
    }
}
