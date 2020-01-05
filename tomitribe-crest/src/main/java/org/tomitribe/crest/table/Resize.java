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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Resize {

    public static Data resize(final Data data, final int width) {
        if (data.getWidth().getMax() < width) return data;

        /*
         * We take the minimum size of the table and slowly and evenly expand it back out
         * until we reach the maximum.  If the desired table size is 200 and the minimum
         * size this table can be squished to is 92 we will have a remainder of 108.  The
         * 108 will be given back to each column till there is no more to allocate.
         *
         * Each column will stop taking space once it reaches its maximum or there is no
         * more space left to allocate.
         *
         * We do this to find the optimal sizes for each column, it does not actually
         * resize the cells.  That happens later.
         */
        final AtomicInteger remaining = new AtomicInteger(width - data.getWidth().getMin());

        final List<Column> columns = data.getColumns().stream()
                .map(column -> new Column(column, remaining))
                .collect(Collectors.toList());

        while (expand(columns)) ;

        /*
         * Now actually resize the cells to fit in their new column size
         * and add them to the new array of cell[][]
         */
        final Data.Cell[][] resized = new Data.Cell[data.getRows().size()][data.getColumns().size()];

        columns.stream()
                .flatMap(Column::resize)
                .forEach(cell -> resized[cell.getRow()][cell.getColumn()] = cell);

        /*
         * Create a new Data instance with the new cells
         */
        return new Data(resized, data);
    }

    private static boolean expand(final List<Column> columns) {
        return columns.stream()
                .map(Column::expand)
                .reduce((columnAExpanded, columnBExpanded) -> columnAExpanded || columnBExpanded)
                .orElse(false);
    }

    public static class Column {
        private final Data.Column column;
        private final AtomicInteger remaining;
        private int size;

        public Column(final Data.Column column, final AtomicInteger remaining) {
            this.column = column;
            this.size = column.getWidth().getMin();
            this.remaining = remaining;
        }

        public boolean expand() {
            if (size == column.getWidth().getMax()) return false;
            if (remaining.getAndDecrement() <= 0) return false;
            size++;
            return true;
        }

        public Stream<Data.Cell> resize() {
            return column.stream().map(cell -> cell.resizeTo(size));
        }
    }
}
