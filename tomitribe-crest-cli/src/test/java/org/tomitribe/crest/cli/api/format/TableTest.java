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

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.lang.System.lineSeparator;
import static org.junit.Assert.assertEquals;

public class TableTest {
    @Test
    public void horizontal() {
        final Table table = new Table("aaa", "b", "c");
        table.row("1", "22", "3");
        table.row("4", "5", "66");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        table.printHorizontal(new PrintStream(out));

        assertEquals("" +
            "=================" + lineSeparator() +
            "| aaa | b  | c  |" + lineSeparator() +
            "=================" + lineSeparator() +
            "|  1  | 22 | 3  |" + lineSeparator() +
            "|  4  | 5  | 66 |" + lineSeparator() +
            "-----------------" + lineSeparator() + lineSeparator(), new String(out.toByteArray()));
    }

    @Test
    public void vertical() {
        final Table table = new Table("aaa", "b", "c");
        table.row("11", "2", "3");
        table.row("4", "5", "666");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        table.printVertical(new PrintStream(out));

        assertEquals("" +
            "========------\n" +
            "|| aaa || 11 |\n" +
            "||  b  || 2  |\n" +
            "||  c  || 3  |\n" +
            "========------\n" +
            "\n" +
            "========-------\n" +
            "|| aaa ||  4  |\n" +
            "||  b  ||  5  |\n" +
            "||  c  || 666 |\n" +
            "========-------\n" +
            "\n", new String(out.toByteArray()).replace(lineSeparator(), "\n"));
    }
}
