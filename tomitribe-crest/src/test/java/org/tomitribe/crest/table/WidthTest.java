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
import org.tomitribe.crest.table.Width;

import static org.junit.Assert.assertEquals;

public class WidthTest {

    @Test
    public void ofString() {
        final Width a = Width.ofString("this string has a longish-style word in it");
        assertEquals(13, a.getMin());
        assertEquals(42, a.getMax());
        assertEquals("Width{min=13, max=42}", a.toString());

        final Width b = Width.ofString("this string has a word that is not quite as long");
        assertEquals(6, b.getMin());
        assertEquals(48, b.getMax());
        assertEquals("Width{min=6, max=48}", b.toString());
    }

    @Test
    public void testToString() {
        final Width a = new Width(13, 42);
        assertEquals("Width{min=13, max=42}", a.toString());
    }

    @Test
    public void add() {
        final Width a = new Width(3, 10);
        final Width b = new Width(7, 11);
        final Width c = a.add(b);
        assertEquals(10, c.getMin());
        assertEquals(21, c.getMax());
    }

    @Test
    public void adjust() {
        final Width a = new Width(3, 10);
        final Width b = new Width(7, 8);
        final Width c = a.adjust(b);
        assertEquals(7, c.getMin());
        assertEquals(10, c.getMax());
    }
}