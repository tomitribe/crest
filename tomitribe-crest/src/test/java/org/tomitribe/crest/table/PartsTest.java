/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.table;

import org.junit.Test;
import org.tomitribe.util.Join;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PartsTest {

    @Test
    public void onePart() throws Exception {
        assertParts("one", "one");
    }

    @Test
    public void twoParts() throws Exception {
        assertParts("one.two", "one", "two");
    }

    @Test
    public void escaped() throws Exception {
        assertParts("one\\.two", "one.two");
    }

    @Test
    public void escapedSlash() throws Exception {
        assertParts("one\\\\.two", "one\\", "two");
        assertParts("\\\\one.two", "\\one", "two");
        assertParts("o\\\\ne.two", "o\\ne", "two");
        assertParts("one.\\\\two", "one", "\\two");
        assertParts("one.t\\\\wo", "one", "t\\wo");
        assertParts("one.two\\", "one", "two\\");
    }

    @Test
    public void escapedDot() throws Exception {
        assertParts("one\\.two", "one.two");
        assertParts("\\.one.two", ".one", "two");
        assertParts("one.two\\.", "one", "two.");
    }

    @Test
    public void escapedSlashAndDot() throws Exception {
        assertParts("one\\\\\\.two", "one\\.two");
    }

    @Test
    public void escapedOther() throws Exception {
        // Simple example
        assertParts("one\\two", "one\\two");

        for (int a = 0; a < 256; a++) {
            final char c = (char) a;

            if (c == '.' || c == '\\') continue;

            final String input = "\\" + c;
            assertParts(input, input);
        }

    }

    @Test
    public void unescape() throws Exception {
        assertUnescape("one", "one");
        assertUnescape("one.two", "one.two");
        assertUnescape("one\\.two", "one.two");
        assertUnescape("one\\\\.two", "one\\.two");
        assertUnescape("\\\\one.two", "\\one.two");
        assertUnescape("o\\\\ne.two", "o\\ne.two");
        assertUnescape("one.\\\\two", "one.\\two");
        assertUnescape("one.t\\\\wo", "one.t\\wo");
        assertUnescape("one.two\\", "one.two\\");
        assertUnescape("one\\.two", "one.two");
        assertUnescape("\\.one.two", ".one.two");
        assertUnescape("one.two\\.", "one.two.");
        assertUnescape("one\\\\\\.two", "one\\.two");
        assertUnescape("one\\two", "one\\two");

    }

    private static void assertUnescape(final String input, final String expected) {
        assertEquals(expected, Parts.unescape(input));
    }


    private void assertParts(final String input, final String... expectedParts) {
        System.out.println(input);
        final List<String> actualParts = Parts.from(input);
        final String actual = Join.join("\n", actualParts);
        final String expected = Join.join("\n", (Object[]) expectedParts);
        assertEquals(expected, actual);
    }


}