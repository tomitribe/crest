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
package org.tomitribe.crest.util;

import junit.framework.TestCase;

import static org.tomitribe.util.SizeUnit.*;

public class SizeUnitTest extends TestCase {

    public void testToBytes() throws Exception {
        assertEquals(2, BYTES.toBytes(2));
        assertEquals(2048, KILOBYTES.toBytes(2));
        assertEquals(2097152, MEGABYTES.toBytes(2));
        assertEquals(2147483648l, GIGABYTES.toBytes(2));
        assertEquals(2199023255552l, TERABYTES.toBytes(2));

        assertEquals(2, BYTES.convert(2, BYTES));
        assertEquals(2048, BYTES.convert(2, KILOBYTES));
        assertEquals(2097152, BYTES.convert(2, MEGABYTES));
        assertEquals(2147483648l, BYTES.convert(2, GIGABYTES));
        assertEquals(2199023255552l, BYTES.convert(2, TERABYTES));
    }

    public void testToKilobytes() throws Exception {
        assertEquals(2, BYTES.toKilobytes(2048));
        assertEquals(2, KILOBYTES.toKilobytes(2));
        assertEquals(2048, MEGABYTES.toKilobytes(2));
        assertEquals(2097152, GIGABYTES.toKilobytes(2));
        assertEquals(2147483648l, TERABYTES.toKilobytes(2));

        assertEquals(2, KILOBYTES.convert(2048, BYTES));
        assertEquals(2, KILOBYTES.convert(2, KILOBYTES));
        assertEquals(2048, KILOBYTES.convert(2, MEGABYTES));
        assertEquals(2097152, KILOBYTES.convert(2, GIGABYTES));
        assertEquals(2147483648l, KILOBYTES.convert(2, TERABYTES));
    }

    public void testToMegabytes() throws Exception {
        assertEquals(2, BYTES.toMegabytes(2097152));
        assertEquals(2, KILOBYTES.toMegabytes(2048));
        assertEquals(2, MEGABYTES.toMegabytes(2));
        assertEquals(2048, GIGABYTES.toMegabytes(2));
        assertEquals(2097152, TERABYTES.toMegabytes(2));

        assertEquals(2, MEGABYTES.convert(2097152, BYTES));
        assertEquals(2, MEGABYTES.convert(2048, KILOBYTES));
        assertEquals(2, MEGABYTES.convert(2, MEGABYTES));
        assertEquals(2048, MEGABYTES.convert(2, GIGABYTES));
        assertEquals(2097152, MEGABYTES.convert(2, TERABYTES));
    }

    public void testToGigabytes() throws Exception {
        assertEquals(2, BYTES.toGigabytes(2147483648l));
        assertEquals(2, KILOBYTES.toGigabytes(2097152));
        assertEquals(2, MEGABYTES.toGigabytes(2048));
        assertEquals(2, GIGABYTES.toGigabytes(2));
        assertEquals(2048, TERABYTES.toGigabytes(2));

        assertEquals(2, GIGABYTES.convert(2147483648l, BYTES));
        assertEquals(2, GIGABYTES.convert(2097152, KILOBYTES));
        assertEquals(2, GIGABYTES.convert(2048, MEGABYTES));
        assertEquals(2, GIGABYTES.convert(2, GIGABYTES));
        assertEquals(2048, GIGABYTES.convert(2, TERABYTES));
    }

    public void testToTerabytes() throws Exception {
        assertEquals(2, BYTES.toTerabytes(2199023255552l));
        assertEquals(2, KILOBYTES.toTerabytes(2147483648l));
        assertEquals(2, MEGABYTES.toTerabytes(2097152));
        assertEquals(2, GIGABYTES.toTerabytes(2048));
        assertEquals(2, TERABYTES.toTerabytes(2));

        assertEquals(2, TERABYTES.convert(2199023255552l, BYTES));
        assertEquals(2, TERABYTES.convert(2147483648l, KILOBYTES));
        assertEquals(2, TERABYTES.convert(2097152, MEGABYTES));
        assertEquals(2, TERABYTES.convert(2048, GIGABYTES));
        assertEquals(2, TERABYTES.convert(2, TERABYTES));
    }
}
