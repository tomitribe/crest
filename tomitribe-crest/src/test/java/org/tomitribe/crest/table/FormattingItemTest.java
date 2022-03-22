/*
 * Copyright 2022 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.table;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormattingItemTest {

    @Test
    public void compareToContainingComparable() {
        final Long rawA = new Long(9);
        final Long rawB = new Long(70);
        final Long rawC = new Long(9);
        final Formatting.Item a = new Formatting.Item(rawA);
        final Formatting.Item b = new Formatting.Item(rawB);
        final Formatting.Item c = new Formatting.Item(rawC);
        final Formatting.Item n = new Formatting.Item(null);
        final Formatting.Item n2 = new Formatting.Item(null);

        { // lesser
            final int expected = rawA.compareTo(rawB);
            assertEquals(expected, a.compareTo(b));
            assertEquals(-1, a.compareTo(b));
        }

        { // equals
            final int expected = rawA.compareTo(rawC);
            assertEquals(expected, a.compareTo(c));
            assertEquals(0, a.compareTo(c));
        }

        { // greater
            final int expected = rawB.compareTo(rawA);
            assertEquals(expected, b.compareTo(a));
            assertEquals(1, b.compareTo(a));
        }

        { // left is null
            assertEquals(-1, n.compareTo(a));
        }

        { // right is null
            assertEquals(1, a.compareTo(n));
        }

        { // left and right are null
            assertEquals(0, n.compareTo(n2));
        }
    }

    /**
     * The values do not implement Comparable and therefore will
     * be compared via their string value.
     */
    @Test
    public void compareToContainingNonComparable() {

        final Formatting.Item a = new Formatting.Item(new Value(new Long(9)));
        final Formatting.Item b = new Formatting.Item(new Value(new Long(70)));
        final Formatting.Item c = new Formatting.Item(new Value(new Long(9)));
        final Formatting.Item n = new Formatting.Item(null);
        final Formatting.Item n2 = new Formatting.Item(null);

        { // greater
            assertEquals(2, a.compareTo(b));
        }

        { // equals
            assertEquals(0, a.compareTo(c));
        }

        { // lesser
            assertEquals(-2, b.compareTo(a));
        }

        { // left is null
            assertEquals(-1, n.compareTo(a));
        }

        { // right is null
            assertEquals(1, a.compareTo(n));
        }

        { // left and right are null
            assertEquals(0, n.compareTo(n2));
        }
    }

    /**
     * This type is intentionally not serializable
     */
    public static class Value {
        private final Long value;

        public Value(final Long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
