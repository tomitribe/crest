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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Taable {

    private final Data data;
    private final Border border;
    private final int width;

    public Taable(final Data data, final Border border, final int width) {
        final int available = width - border.getWidth(data.getColumns().size()).getMax();
        this.data = Resize.resize(data, available);
        this.border = border;
        this.width = width;
    }

    public void format(final PrintStream out) {
        final String rowFormat = getFormat(border.getRow());
        final String rowSeparator = getLine(border.getInner());

        final Consumer<Object> printSeparator = rowSeparator != null ? o -> out.println(rowSeparator) : o -> {
        };
        final Consumer<String[]> printRow = strings -> out.println(String.format(rowFormat, (Object[]) strings));

        final List<Data.Row> rows = new ArrayList<>(data.getRows());

        /*
         * Stream all but the very last row which we will treat
         * separately as we don't want to print a row separator
         * after it.
         */

        /*
         * Print the top border
         */
        if (border.getFirst() != null) out.println(getLine(border.getFirst()));

        /*
         * Print the heading
         */

        if (data.hasHeading()) {
            Stream.of(rows.remove(0))
                    .map(Data.Row::toLines)
                    .flatMap(Stream::of)
                    .forEach(printRow);

            if (border.getHeader() != null) out.println(getLine(border.getHeader()));
        }

        /*
         * Print the first line after the heading
         */
        if (rows.size() > 0) Stream.of(rows.remove(0))
                .map(Data.Row::toLines)
                .flatMap(Stream::of)
                .forEach(printRow);


        /*
         * Print the remaining lines with a separator before
         * each one if we have a separator
         */
        rows.stream()
                .peek(printSeparator)
                .map(Data.Row::toLines)
                .flatMap(Stream::of)
                .forEach(printRow);

        /*
         * Print the bottom border
         */
        if (border.getLast() != null) out.println(getLine(border.getLast()));
    }

    public String getFormat(final Line line) {
        final List<Data.Column> columns = this.data.getColumns();
        final List<String> formats = columns.stream().map(column -> {
            final int width = column.getWidth().getMax();
            return column.isNumeric() ? "%" + width + "s" : "%-" + width + "s";
        }).collect(Collectors.toList());

        return line.getLeft() +
                Join.join(line.getInner(), formats) +
                line.getRight();
    }

    public String getLine(final Line line) {
        final String format = getFormat(line);
        return String.format(format, (Object[]) rowOf(line.getMiddle()));
    }

    private String[] rowOf(final String string) {
        final List<Data.Column> columns = this.data.getColumns();

        final String[] row = new String[columns.size()];

        for (int i = 0; i < row.length; i++) {
            row[i] = columnOf(columns.get(i), string);
        }
        return row;
    }

    private String columnOf(final Data.Column column, String string) {
        final int size = column.getWidth().getMax();

        while (string.length() < size) {
            string += string;
        }

        return string.substring(0, size);
    }

}
