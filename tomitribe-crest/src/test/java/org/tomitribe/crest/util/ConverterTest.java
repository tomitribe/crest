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

import org.junit.Assert;
import org.junit.Test;

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
        assertEquals(new URI("foo"), Converter.convert("foo", URI.class, null));
        assertEquals(new Green("foo"), Converter.convert("foo", Green.class, null));

        final Yellow expected = new Yellow();
        expected.value = "foo";

        assertEquals(expected, Converter.convert("foo", Yellow.class, null));
    }

    public static class Green {

        private String value;

        public Green(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Green)) return false;

            Green green = (Green) o;

            if (value != null ? !value.equals(green.value) : green.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class Yellow {

        private String value;

        public static Yellow makeOne(String value) {
            final Yellow yellow = new Yellow();
            yellow.value = value;
            return yellow;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Yellow yellow = (Yellow) o;

            if (value != null ? !value.equals(yellow.value) : yellow.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}
