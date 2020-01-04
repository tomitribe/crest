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

import static org.junit.Assert.assertEquals;

public class TableFormatterTest {

    @Test
    public void test() throws Exception {


        assertEquals("", "");
    }


    @Test
    public void getTableWidth() {
        final String[][] rows = new String[4][];
        rows[0] = new String[]{"aa bbb"};
        rows[1] = new String[]{"a b c", "aa", "aa", "", "abcd ef"};
        rows[2] = new String[]{"", "a b", "", "abc"};
        rows[3] = new String[]{"a", "b", "cdefhijklmn opqrs"};

        final Table table = new Table(false, rows);
        final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

        final Width width = formatter.getWidth();
    }

    @Test
    public void getDelimiterWidth() {
        {
            final Table table = Table.builder().build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getDelimiterWidth();
            assertEquals(4, width.getMin());
            assertEquals(4, width.getMax());
        }

        {
            final Table table = Table.builder()
                    .row("1234567")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getDelimiterWidth();
            assertEquals(4, width.getMin());
            assertEquals(4, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234567", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getDelimiterWidth();
            assertEquals(7, width.getMin());
            assertEquals(7, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234567", "", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getDelimiterWidth();
            assertEquals(10, width.getMin());
            assertEquals(10, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234567", "", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated()
                    .row("|", "-|-", " |")
                    .build());

            final Width width = formatter.getDelimiterWidth();
            assertEquals(9, width.getMin());
            assertEquals(9, width.getMax());
        }
    }

    @Test
    public void getWidth() {
        {
            final Table table = Table.builder().build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(4, width.getMin());
            assertEquals(4, width.getMax());
        }

        {
            final Table table = Table.builder()
                    .row("1234567")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(11, width.getMin());
            assertEquals(11, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234567", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(14, width.getMin());
            assertEquals(14, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234567", "", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(17, width.getMin());
            assertEquals(17, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234 67", "", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(14, width.getMin());
            assertEquals(17, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234 67", "a", "")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(15, width.getMin());
            assertEquals(18, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234 67", "a", "")
                    .row("12 67", "a", "as")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(17, width.getMin());
            assertEquals(20, width.getMax());
        }
        {
            final Table table = Table.builder()
                    .row("1234 67", "a", "")
                    .row("12 67", "a", "as df")
                    .build();
            final TableFormatter formatter = new TableFormatter(100, table, Border.unicodeSingleSeparated().build());

            final Width width = formatter.getWidth();
            assertEquals(17, width.getMin());
            assertEquals(23, width.getMax());
        }
    }

}