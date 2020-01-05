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

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

public class TableWrappingTest {


    private final Table table = Table.builder()
            .headings(true)
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
    public void unicodeSingleSeparated() {
        assertTable(Border::unicodeSingleSeparated, "");
    }

    public void assertTable(final Supplier<Border.Builder> border, final String expected) {
        final TableFormatter formatter = new TableFormatter(300, table, border.get().build());
        final String actual = formatter.format();
        Assert.assertEquals(expected, actual);
    }
}