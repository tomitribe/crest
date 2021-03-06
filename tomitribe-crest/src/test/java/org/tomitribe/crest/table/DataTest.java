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

import org.junit.Test;
import org.tomitribe.util.Join;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataTest {

    private final Data table = Data.builder()
            .row("id", "first_name", "last_name", "email", "slogan", "sentence")

            .row("1", "Wendie", "Marquet", "wmarquet0@blogspot.com", "unleash mission-critical experiences",
                    "Suspendisse ornare consequat lectus. In est risus, auctor sed, tristique in, tempus sit amet, sem.")

            .row("2", "Derry", "Henkmann", "dhenkmann1@cdbaby.com", "innovate seamless e-services",
                    "Sed vel enim sit amet nunc viverra dapibus. Nulla suscipit ligula in lacus. Curabitur at ipsum ac tellus semper interdum.")

            .row("3", "Heidi", "Bointon", "hbointon2@bloglovin.com", "visualize real-time architectures",
                    "Vivamus vel nulla eget eros elementum pellentesque. Quisque porta volutpat erat. Quisque erat eros, " +
                            "viverra eget, congue eget, semper rutrum, nulla.")

            .row("4", "Elladine", "Twelve", "etwelve3@friendfeed.com", "scale global platforms", "Integer non velit. " +
                    "Donec diam neque, vestibulum eget, vulputate ut, ultrices vel, augue. Vestibulum ante ipsum primis " +
                    "in faucibus orci luctus et ultrices posuere cubilia Curae; Donec pharetra, magna vestibulum aliquet " +
                    "ultrices, erat tortor sollicitudin mi, sit amet lobortis sapien sapien non mi. Integer ac neque. " +
                    "Duis bibendum. Morbi non quam nec dui luctus rutrum. Nulla tellus. In sagittis dui vel nisl. Duis ac " +
                    "nibh. Fusce lacus purus, aliquet at, feugiat non, pretium quis, lectus.")

            .row("5", "Erl", "Mellmer", "emellmer4@about.com", "harness cross-media infomediaries",
                    "Suspendisse potenti. In eleifend quam a odio. In hac habitasse platea dictumst. Maecenas ut" +
                            " massa quis augue luctus tincidunt.")

            .build();

    @Test
    public void streamColumn() throws Exception {
        final List<String> column2 = table.getColumn(1).stream()
                .map(Data.Cell::getData)
                .collect(Collectors.toList());

        assertEquals("first_name\n" +
                "Wendie\n" +
                "Derry\n" +
                "Heidi\n" +
                "Elladine\n" +
                "Erl", Join.join("\n", column2));
    }

    @Test
    public void columns() throws Exception {
        final List<String> column2 = table.getColumn(1).stream()
                .map(Data.Cell::getData)
                .collect(Collectors.toList());

        assertEquals("first_name\n" +
                "Wendie\n" +
                "Derry\n" +
                "Heidi\n" +
                "Elladine\n" +
                "Erl", Join.join("\n", column2));
    }

    @Test
    public void columnCount() {
        final String[][] rows = new String[4][];
        rows[0] = new String[]{""};
        rows[1] = new String[]{"", "", "", "", ""};
        rows[2] = new String[]{"", "", "", ""};
        rows[3] = new String[]{"", "", ""};

        final Data table = new Data(rows, false);
        assertEquals(5, table.getColumns().size());
    }

    @Test
    public void width() {
        final String[][] rows = new String[4][];
        rows[0] = new String[]{"aa bbb"};
        rows[1] = new String[]{"a b c", "aa", "aa", "", "abcd ef"};
        rows[2] = new String[]{"", "a b", "", "abc"};
        rows[3] = new String[]{"a", "b", "cdefhijklmn opqrs"};

        final Data table = new Data(rows, false);
        final Width width = table.getWidth();
        assertEquals(23, width.getMin());
        assertEquals(36, width.getMax());
    }

    @Test
    public void isNumeric() {
        assertNumeric("1");
        assertNotNumeric("a");
        assertNumeric("1.0");
        assertNotNumeric("1.0a");
        assertNumeric("1037");
        assertNumeric("1,024");
        assertNumeric("-0,987,654.321");
        assertNumeric("-$987,654.321");
        assertNumeric("");
    }

    public static void assertNotNumeric(final String s) {
        final Data data = Data.builder().row(s).build();
        assertFalse(data.getColumn(0).isNumeric());
    }

    public static void assertNumeric(final String s) {
        final Data data = Data.builder().row(s).build();
        assertTrue(data.getColumn(0).isNumeric());
    }

    @Test
    public void isNumericWithHeader() {
        final Data table = Data.builder().headings(true)
                .row("Col1", "Col2", "Col3", "Numeric Column")
                .row("Value 1", "Value 2", "123", "10.0")
                .row("Separate", "cols", "with a tab or 4 spaces", "-2,027.1")
                .row("This is a row with only one cell")
                .build();

        assertFalse(table.getColumn(0).isNumeric());
        assertFalse(table.getColumn(1).isNumeric());
        assertFalse(table.getColumn(2).isNumeric());
        assertTrue(table.getColumn(3).isNumeric());
    }
}