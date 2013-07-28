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
package org.tomitribe.crest.util;

import junit.framework.TestCase;

import static org.tomitribe.crest.util.SizeUnit.BYTES;
import static org.tomitribe.crest.util.SizeUnit.GIGABYTES;
import static org.tomitribe.crest.util.SizeUnit.KILOBYTES;
import static org.tomitribe.crest.util.SizeUnit.MEGABYTES;
import static org.tomitribe.crest.util.SizeUnit.TERABYTES;

/**
 * @version $Revision$ $Date$
 */
public class SizeTest extends TestCase {

    public void test() throws Exception {

        assertEquals(new Size(3, BYTES), new Size("3 bytes"));
        assertEquals(new Size(3, BYTES), new Size("3bytes"));
        assertEquals(new Size(3, BYTES), new Size("3byte"));
        assertEquals(new Size(3, BYTES), new Size("3b"));

        assertEquals(new Size(4, KILOBYTES), new Size("4 kilobytes"));
        assertEquals(new Size(4, KILOBYTES), new Size("4kilobytes"));
        assertEquals(new Size(4, KILOBYTES), new Size("4kilobyte"));
        assertEquals(new Size(4, KILOBYTES), new Size("4kb"));
        assertEquals(new Size(4, KILOBYTES), new Size("4k"));

        assertEquals(new Size(5, MEGABYTES), new Size("5 megabytes"));
        assertEquals(new Size(5, MEGABYTES), new Size("5megabyte"));
        assertEquals(new Size(5, MEGABYTES), new Size("5mb"));
        assertEquals(new Size(5, MEGABYTES), new Size("5m"));

        assertEquals(new Size(6, GIGABYTES), new Size("6 gigabytes"));
        assertEquals(new Size(6, GIGABYTES), new Size("6gigabyte"));
        assertEquals(new Size(6, GIGABYTES), new Size("6gb"));
        assertEquals(new Size(6, GIGABYTES), new Size("6g"));

        assertEquals(new Size(7, TERABYTES), new Size("7 terabytes"));
        assertEquals(new Size(7, TERABYTES), new Size("7 terabyte"));
        assertEquals(new Size(7, TERABYTES), new Size("7 tb"));
        assertEquals(new Size(7, TERABYTES), new Size("7 t"));


        assertEquals(new Size(1, null), new Size("1"));
        assertEquals(new Size(234, null), new Size("234"));
        assertEquals(new Size(123, null), new Size("123"));
        assertEquals(new Size(-1, null), new Size("-1"));
    }


    public void testUnitConversion() throws Exception {
        assertEquals(3, new Size(3, BYTES).getSize(BYTES));
        assertEquals(3072, new Size(3, KILOBYTES).getSize(BYTES));
        assertEquals(3145728, new Size(3, MEGABYTES).getSize(BYTES));
        assertEquals(3221225472l, new Size(3, GIGABYTES).getSize(BYTES));
        assertEquals(3298534883328l, new Size(3, TERABYTES).getSize(BYTES));

        assertEquals(3, new Size(3072, BYTES).getSize(KILOBYTES));
        assertEquals(3, new Size(3, KILOBYTES).getSize(KILOBYTES));
        assertEquals(3072, new Size(3, MEGABYTES).getSize(KILOBYTES));
        assertEquals(3145728, new Size(3, GIGABYTES).getSize(KILOBYTES));
        assertEquals(3221225472l, new Size(3, TERABYTES).getSize(KILOBYTES));

        assertEquals(3, new Size(3145728, BYTES).getSize(MEGABYTES));
        assertEquals(3, new Size(3072, KILOBYTES).getSize(MEGABYTES));
        assertEquals(3, new Size(3, MEGABYTES).getSize(MEGABYTES));
        assertEquals(3072, new Size(3, GIGABYTES).getSize(MEGABYTES));
        assertEquals(3145728, new Size(3, TERABYTES).getSize(MEGABYTES));

        assertEquals(3, new Size(3221225472l, BYTES).getSize(GIGABYTES));
        assertEquals(3, new Size(3145728, KILOBYTES).getSize(GIGABYTES));
        assertEquals(3, new Size(3072, MEGABYTES).getSize(GIGABYTES));
        assertEquals(3, new Size(3, GIGABYTES).getSize(GIGABYTES));
        assertEquals(3072, new Size(3, TERABYTES).getSize(GIGABYTES));

        assertEquals(3, new Size(3298534883328l, BYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3221225472l, KILOBYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3145728, MEGABYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3072, GIGABYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3, TERABYTES).getSize(TERABYTES));
    }

    public void testMultiple() throws Exception {
        assertEquals(new Size(1101662261253l, BYTES), Size.parse("1tb and 2gb and 3mb and 4kb and 5 bytes"));
    }

    public void testDefaultUnit() throws Exception {
        assertEquals(new Size(15, MEGABYTES), new Size("15 megabytes", BYTES));

        assertEquals(new Size(15, null), new Size("1 and 2 and 3 and 4 and 5"));
        assertEquals(new Size(15, BYTES), new Size("1 and 2 and 3 and 4 and 5", BYTES));
        assertEquals(new Size(15, KILOBYTES), new Size("1 and 2 and 3 and 4 and 5", KILOBYTES));
        assertEquals(new Size(15, MEGABYTES), new Size("1 and 2 and 3 and 4 and 5", MEGABYTES));
        assertEquals(new Size(15, GIGABYTES), new Size("1 and 2 and 3 and 4 and 5", GIGABYTES));
        assertEquals(new Size(15, TERABYTES), new Size("1 and 2 and 3 and 4 and 5", TERABYTES));

        assertEquals(new Size(1102738096134l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", BYTES));
        assertEquals(new Size(1102738102272l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", KILOBYTES));
        assertEquals(new Size(1102744387584l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", MEGABYTES));
        assertEquals(new Size(1109180547073l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb and 1byte", GIGABYTES));
        assertEquals(new Size(7343109, MEGABYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", TERABYTES));
        assertEquals(new Size(7343109 * 1024l * 1024l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", TERABYTES));
    }
}
