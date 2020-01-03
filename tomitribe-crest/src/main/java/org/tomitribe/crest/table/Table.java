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

import org.tomitribe.crest.help.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table implements Element {


    private final boolean headings;
    private final String[][] data;
    private final int columns;
    private final boolean[] numerics;

    public Table(final boolean headings, final String[][] data) {
        this.headings = headings;
        this.data = data;
        this.columns = Stream.of(data)
                .map(strings -> strings.length)
                .reduce(Math::max)
                .orElse(0);
        this.numerics = new boolean[columns];

        final Predicate<String> isNumeric = getIsNumeric();
        for (int i = 0; i < columns; i++) {
            this.numerics[i] = matches(isNumeric, i);
        }
    }

    private boolean matches(final Predicate<String> predicate, final int column) {
        final List<String> rows = getColumn(column).stream().collect(Collectors.toList());

        for (int i = headings ? 1 : 0; i < rows.size(); i++) {
            final String cell = rows.get(i);
            if (cell == null) continue;
            if (!predicate.test(cell)) {
                return false;
            }
        }

        return true;
    }

    public static Predicate<String> getIsNumeric() {
        return Pattern.compile("^-?[$£€¥₴]?[0-9]+[0-9,.]*%?$|^$").asPredicate();
    }

    public int getColumnCount() {
        return columns;
    }

    public List<Width> getColumnWidths() {
        final List<Width> widths = new ArrayList<Width>();
        for (int i = 0; i < columns; i++) {
            final Width width = getColumn(i).stream()
                    .map(Width::ofString)
                    .reduce(Width::adjust)
                    .orElse(new Width(0, 0));

            widths.add(width);
        }
        return widths;
    }

    public int getRowCount() {
        return data.length;
    }

    public String[] getHeadings() {
        return hasHeadings() ? data[0] : null;
    }

    public boolean hasHeadings() {
        return headings && data.length > 0;
    }

    public String[][] getData() {
        return data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public class Column {
        private final int i;

        public Column(final int i) {
            this.i = i;
        }

        public boolean isNumeric() {
            return numerics[i];
        }

        public Stream<String> stream() {
            final AtomicInteger row = new AtomicInteger();
            final Supplier<String> rows = () -> {
                final String[] r = data[row.getAndIncrement()];
                return r.length > i ? r[i] : "";
            };
            return Stream.generate(rows)
                    .limit(data.length);
        }
    }

    public Column getColumn(int i) {
        return new Column(i);
    }

    public class Row {

        private final String[] columns;

        public Row(final int i) {
            columns = data[i];
        }

        public Stream<String> stream() {
            final AtomicInteger column = new AtomicInteger();
            final Supplier<String> columns = () -> {
                final int i = column.getAndIncrement();
                return this.columns.length > i ? this.columns[i] : "";
            };
            return Stream.generate(columns)
                    .limit(getColumnCount());
        }
    }

    public Row getRow(int i) {
        return new Row(i);
    }

    @Override
    public java.lang.String getContent() {
        return null;
    }

    public Width getWidth() {
        return getColumnWidths().stream()
                .reduce(Width::add)
                .orElse(new Width(0, 0));
    }

    public static class Builder {
        private final List<String[]> rows = new ArrayList<String[]>();
        private boolean headings;

        public Builder row(final String... columns) {
            rows.add(columns);
            return this;
        }

        public Builder headings(final boolean headings) {
            this.headings = headings;
            return this;
        }

        public Table build() {
            final String[][] data = rows.toArray(new String[0][]);
            return new Table(headings, data);
        }
    }
}
