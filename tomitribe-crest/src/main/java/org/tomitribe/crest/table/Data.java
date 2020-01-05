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

import org.tomitribe.crest.help.Justify;
import org.tomitribe.crest.help.Wrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.tomitribe.crest.table.Tables.createCells;
import static org.tomitribe.crest.table.Tables.createColumns;
import static org.tomitribe.crest.table.Tables.createRows;

public class Data {

    /**
     * To prevent modification there is intentionally no
     * getter that exposes the array instance
     */
    private final Row[] rows;
    /**
     * To prevent modification there is intentionally no
     * getter that exposes the array instance
     */
    private final Column[] columns;
    /**
     * To prevent modification there is intentionally no
     * getter that exposes the array instance
     */
    private final Cell[][] cells;

    private final Width width;
    private final int height;

    public Data(final String[][] data) {
        this.cells = createCells(this, data);
        this.rows = createRows(this, this.cells);
        this.columns = createColumns(this, this.cells);
        this.width = Stream.of(columns)
                .map(Column::getWidth)
                .reduce(Width::add)
                .orElse(Width.ZERO);
        this.height = Stream.of(rows)
                .mapToInt(Row::getHeight)
                .reduce(Integer::sum)
                .orElse(0);
    }

    Data(final Cell[][] cells) {
        this.cells = cells;
        this.rows = createRows(this, this.cells);
        this.columns = createColumns(this, this.cells);
        this.width = Stream.of(columns)
                .map(Column::getWidth)
                .reduce(Width::add)
                .orElse(Width.ZERO);
        this.height = Stream.of(rows)
                .mapToInt(Row::getHeight)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public Width getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Column> getColumns() {
        return Arrays.asList(columns);
    }

    public List<Row> getRows() {
        return Arrays.asList(rows);
    }

    public class Row {
        private final int row;
        private final Width width;
        private final int height;

        public Row(final int row, final Width width, final int height) {
            this.row = row;
            this.width = width;
            this.height = height;
        }

        public Stream<Cell> stream() {
            return Stream.of(cells[row]);
        }

        public int getRow() {
            return row;
        }

        public Width getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int length() {
            return cells[row].length;
        }

        public String[][] toLines() {
            final String[][] lines = new String[height][columns.length];
            for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
                final String[] columns = lines[lineNumber];
                for (int columnNumber = 0; columnNumber < columns.length; columnNumber++) {
                    final Cell cell = cells[row][columnNumber];
                    final String content = lineNumber < cell.lines.size() ? cell.lines.get(lineNumber) : "";
                    columns[columnNumber] = content;
                }
            }
            return lines;
        }
    }

    public class Column {
        private final int index;
        private final Width width;
        private final boolean numeric;

        public Column(final int index, final Width width, final boolean numeric) {
            this.index = index;
            this.width = width;
            this.numeric = numeric;
        }

        public int getIndex() {
            return index;
        }

        public Width getWidth() {
            return width;
        }

        public boolean isNumeric() {
            return numeric;
        }

        public Stream<Cell> stream() {
            return Tables.cellsInColumn(cells, index);
        }

    }

    public class Cell {

        /**
         * The raw data, unsplit or word-wrapped
         */
        private final String data;
        private final int row;
        private final int column;
        private final Width width;
        private final List<String> lines;


        public Cell(final int row, final int column, final String data) {
            this(row, column, data, Lines.split(data));
        }

        private Cell(final int row, final int column, final String data, final String[] lines) {
            this.data = data;
            this.row = row;
            this.column = column;
            this.lines = Collections.unmodifiableList(Arrays.asList(lines));
            this.width = this.lines.stream()
                    .map(Width::ofString)
                    .reduce(Width::adjust)
                    .orElse(Width.ZERO);
        }

        public List<String> getLines() {
            return lines;
        }

        public int getHeight() {
            return lines.size();
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public Width getWidth() {
            return width;
        }

        public String getData() {
            return data;
        }

        public Cell resizeTo(final int width) {
            final String wrapped = Justify.wrapAndJustify(data, width);
            return new Cell(row, column, data, Lines.split(wrapped));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String[]> rows = new ArrayList<String[]>();

        public Data.Builder row(final String... columns) {
            rows.add(columns);
            return this;
        }

        public Data build() {
            final String[][] data = rows.toArray(new String[0][]);
            return new Data(data);
        }
    }
}
