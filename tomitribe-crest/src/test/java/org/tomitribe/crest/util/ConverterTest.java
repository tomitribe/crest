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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.converters.Converter;

import java.net.URI;

/**
 * @version $Revision$ $Date$
 */
public class ConverterTest extends Assert {

    @Test
    public void testConvert() throws Exception {

        assertEquals(1, Converter.convert("1", Integer.class, null));
        assertEquals(1l, Converter.convert("1", Long.class, null));
        assertEquals(true, Converter.convert("true", Boolean.class, null));
        assertEquals((byte) 0, Converter.convert("0", Byte.class, null));
        assertEquals('c', Converter.convert('c', Character.class, null));
        assertEquals(new URI("foo"), Converter.convert("foo", URI.class, null));
        assertEquals(new Green("foo"), Converter.convert("foo", Green.class, null));

        //primitive converter
        assertEquals(1, Converter.convert("1", int.class, null));
        assertEquals(1l, Converter.convert("1", long.class, null));
        assertEquals(1.0d, Converter.convert("1.0", double.class, null));
        assertEquals(true, Converter.convert("true", boolean.class, null));
        assertEquals((byte) 0, Converter.convert("0", byte.class, null));
        assertEquals('c', Converter.convert('c', char.class, null));

        //test null value
        assertEquals(null, Converter.convert(null,Integer.class, null));
        assertEquals(null, Converter.convert(null, Boolean.class, null));
        assertEquals(false, Converter.convert(null, boolean.class, null));

        final Yellow expected = new Yellow();
        expected.value = "foo";

        assertEquals(expected, Converter.convert("foo", Yellow.class, null));
    }

    public static class Green {

        private String value;

        public Green(final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Green)) {
                return false;
            }

            final Green green = (Green) o;

            if (value != null ? !value.equals(green.value) : green.value != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class Yellow {

        private String value;

        public static Yellow makeOne(final String value) {
            final Yellow yellow = new Yellow();
            yellow.value = value;
            return yellow;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Yellow yellow = (Yellow) o;

            if (value != null ? !value.equals(yellow.value) : yellow.value != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}
