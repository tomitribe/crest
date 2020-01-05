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

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The goal of this class its to house certain calculations that are used
 * just once and would make the Table code large and complicated if it
 * lived there.
 *
 * To keep the Table code as simple as possible we need to heavily normalize
 * all the data at construction.  Taking that hit early would have no benefit
 * if the code still lived in the Table class leaving it too complicated to
 * digest.
 */
public class Tables {

    private Tables() {
    }

    static int getMaxWidth(final String[][] data) {
        int max = 0;
        for (final String[] row : data) {
            if (row == null) continue;
            max = Math.max(max, row.length);
        }
        return max;
    }

    static Data.Cell[][] createCells(final Data table, final String[][] data) {
        final int width = getMaxWidth(data);

        final Data.Cell[][] cells = new Data.Cell[data.length][width];
        for (int row = 0; row < data.length; row++) {
            final String[] strings = data[row];

            if (strings != null) for (int column = 0; column < strings.length; column++) {
                final String string = strings[column];
                cells[row][column] = table.new Cell(row, column, string);
            }

            for (int column = 0; column < cells[row].length; column++) {
                if (cells[row][column] == null) {
                    cells[row][column] = table.new Cell(row, column, "");
                }
            }
        }
        return cells;
    }

    static Data.Row[] createRows(final Data table, final Data.Cell[][] cells) {
        final Data.Row[] rows = new Data.Row[cells.length];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = createRow(table, cells, i);
        }
        return rows;
    }

    private static Data.Row createRow(final Data table, final Data.Cell[][] cells, final int row) {
        final int height = cellsInRow(cells, row)
                .mapToInt(Data.Cell::getHeight)
                .reduce(Math::max)
                .orElse(0);

        final Width width = cellsInRow(cells, row)
                .map(Data.Cell::getWidth)
                .reduce(Width::add)
                .orElse(Width.ZERO);

        return table.new Row(row, width, height);
    }

    static Data.Column[] createColumns(final Data table, final Data.Cell[][] cells) {
        final Data.Column[] columns = new Data.Column[cells[0].length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = createColumn(table, cells, i);
        }
        return columns;
    }

    private static Data.Column createColumn(final Data table, final Data.Cell[][] cells, final int column) {

        final Predicate<Data.Cell> isHeading = cell -> table.hasHeading() && cell.getRow() == 0;
        final Predicate<String> isNumeric = Pattern.compile("^-?[$£€¥₴]?[0-9]+[0-9,.]*%?$|^$").asPredicate();
        final Boolean numeric = cellsInColumn(cells, column)
                .filter(isHeading.negate())
                .map(Data.Cell::getData)
                .map(isNumeric::test)
                .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
                .orElse(false);

        final Width width = cellsInColumn(cells, column)
                .map(Data.Cell::getWidth)
                .reduce(Width::adjust)
                .orElse(new Width(0, 0));

        return table.new Column(column, width, numeric);
    }

    public static Stream<Data.Cell> cellsInRow(final Data.Cell[][] cells, final int row) {
        return Stream.of(cells[row]);
    }

    public static Stream<Data.Cell> cellsInColumn(final Data.Cell[][] cells, final int column) {
        return Stream.of(cells).map(row -> row[column]);
    }
}
