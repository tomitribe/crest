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
package org.tomitribe.crest.javadoc;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JavadocParserTest {

    @Test
    public void parseMethod() {
        final String javadocText = "" +
                " This is javadoc text as it\n" +
                " would be given to an annotation \n" +
                " processor\n" +
                " \n And this  @param  is ignored as it is\n" +
                " just text and not used as a tag\n" +
                " \n" +
                " @param shape Specify a circle, square or rectangle\n" +
                " @throws org.acme.FooException if things go south\n" +
                " @param color What is your favorite color?\n Perhaps it is:\nred\ngreen\nblue\n" +
                " @see Some text that is possibly incorrectly formatted\n" +
                " @version 1.1\n" +
                " @since 1.0\n" +
                " @madeup Something unknown\n" +
                " @throws com.example.BarThrowable when the specified thing is no good\n" +
                " @return Something awesome and useful\n" +
                " @author Joe Cool <jcool@example.com>\n" +
                " @see More information here\n" +
                " @author Woodstock\n" +
                " ";

        final Javadoc actual = JavadocParser.parse(javadocText);

        final Javadoc expected = Javadoc.builder()
                .content(" This is javadoc text as it\n" +
                        " would be given to an annotation \n" +
                        " processor\n" +
                        " \n" +
                        " And this  @param  is ignored as it is\n" +
                        " just text and not used as a tag\n" +
                        " ")
                .aReturn(new Javadoc.Return("Something awesome and useful"))
                .version(new Javadoc.Version("1.1"))
                .since(new Javadoc.Since("1.0"))
                .see(new Javadoc.See("Some text that is possibly incorrectly formatted"))
                .see(new Javadoc.See("More information here"))
                .author(new Javadoc.Author("Joe Cool <jcool@example.com>"))
                .author(new Javadoc.Author("Woodstock"))
                .throwing(new Javadoc.Throws("org.acme.FooException", "if things go south"))
                .throwing(new Javadoc.Throws("com.example.BarThrowable", "when the specified thing is no good"))
                .param(new Javadoc.Param("shape", "Specify a circle, square or rectangle"))
                .param(new Javadoc.Param("color", "What is your favorite color?\n Perhaps it is:\nred\ngreen\nblue"))
                .unknown(new Javadoc.Tag("madeup", "Something unknown"))
                .build();

        assertJavadoc(expected, actual);
    }

    @Test
    public void testToTag() throws Exception {
        assertToTag("@foo bar", "foo", "bar");
        assertToTag("@ foo bar", "foo", "bar");
        assertToTag("@ foo    bar", "foo", "bar");
        assertToTag("@ foo    bar     ", "foo", "bar");
        assertToTag("@   foo    bar     ", "foo", "bar");
    }

    @Test
    public void testToTagMultiLine() throws Exception {
        assertToTag("@foo red\n  green\nblue", "foo", "red\n  green\nblue");
        assertToTag("@foo\n red\n  green\nblue", "foo", "red\n  green\nblue");
        assertToTag("@foo\nred\n  green\nblue", "foo", "red\n  green\nblue");
        assertToTag("@foo\tred\n  green\nblue", "foo", "red\n  green\nblue");
        assertToTag("@foo red\n  green\nblue\n\n\n", "foo", "red\n  green\nblue");
    }

    @Test
    public void testToTagMultiLineWithDelimiter() throws Exception {
        assertToTag("@foo red\n  green\n\000\000\000blue", "foo", "red\n  green\n\000\000\000blue");
        assertToTag("@foo red\n  green\n\000blue", "foo", "red\n  green\n\000blue");
    }

    public void assertToTag(final String input, final String name, final String content) {
        final Javadoc.Tag tag = JavadocParser.toTag(input);
        assertJavadoc(new Javadoc.Tag(name, content), tag);
    }

    private void assertJavadoc(final Javadoc expected, final Javadoc actual) {
        assertEquals(expected.getContent(), actual.getContent());
        assertJavadoc(expected.getReturn(), expected.getReturn());
        assertJavadoc(expected.getDeprecated(), expected.getDeprecated());
        assertJavadoc(expected.getVersion(), expected.getVersion());
        assertJavadoc(expected.getSince(), expected.getSince());
        assertParams(expected.getParams(), actual.getParams());
        assertAuthor(expected.getAuthors(), actual.getAuthors());
        assertSee(expected.getSees(), actual.getSees());
        assertThrows(expected.getThrowing(), actual.getThrowing());
        assertTags(expected.getUnknown(), actual.getUnknown());
    }

    private void assertParams(final List<Javadoc.Param> e, final List<Javadoc.Param> a) {
        if (e == null) {
            assertNull(a);
            return;
        }

        final Iterator<Javadoc.Param> expected = e.iterator();
        final Iterator<Javadoc.Param> actual = a.iterator();
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            assertJavadoc(expected.next(), actual.next());
        }
        assertFalse(actual.hasNext());
    }

    private void assertTags(final List<Javadoc.Tag> e, final List<Javadoc.Tag> a) {
        if (e == null) {
            assertNull(a);
            return;
        }

        final Iterator<Javadoc.Tag> expected = e.iterator();
        final Iterator<Javadoc.Tag> actual = a.iterator();
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            assertJavadoc(expected.next(), actual.next());
        }
        assertFalse(actual.hasNext());
    }

    private void assertThrows(final List<Javadoc.Throws> e, final List<Javadoc.Throws> a) {
        if (e == null) {
            assertNull(a);
            return;
        }

        final Iterator<Javadoc.Throws> expected = e.iterator();
        final Iterator<Javadoc.Throws> actual = a.iterator();
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            assertJavadoc(expected.next(), actual.next());
        }
        assertFalse(actual.hasNext());
    }

    private void assertSee(final List<Javadoc.See> e, final List<Javadoc.See> a) {
        if (e == null) {
            assertNull(a);
            return;
        }

        final Iterator<Javadoc.See> expected = e.iterator();
        final Iterator<Javadoc.See> actual = a.iterator();
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            assertJavadoc(expected.next(), actual.next());
        }
        assertFalse(actual.hasNext());
    }

    private void assertAuthor(final List<Javadoc.Author> e, final List<Javadoc.Author> a) {
        if (e == null) {
            assertNull(a);
            return;
        }

        final Iterator<Javadoc.Author> expected = e.iterator();
        final Iterator<Javadoc.Author> actual = a.iterator();
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            assertJavadoc(expected.next(), actual.next());
        }
        assertFalse(actual.hasNext());
    }

    private void assertJavadoc(final Javadoc.Throws expected, final Javadoc.Throws actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.getClassname(), actual.getClassname());
            assertEquals(expected.getDescription(), actual.getDescription());
        }
    }

    private void assertJavadoc(final Javadoc.Param expected, final Javadoc.Param actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getDescription(), actual.getDescription());
        }
    }

    private void assertJavadoc(final Javadoc.Tag expected, final Javadoc.Tag actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getContent(), actual.getContent());
        }
    }

    private void assertJavadoc(final Javadoc.Since expected, final Javadoc.Since actual) {
        if (expected == null) {
            assertNull(actual);
        } else
            assertEquals(expected.getContent(), actual.getContent());
    }

    private void assertJavadoc(final Javadoc.See expected, final Javadoc.See actual) {
        if (expected == null) {
            assertNull(actual);
        } else
            assertEquals(expected.getContent(), actual.getContent());
    }

    private void assertJavadoc(final Javadoc.Version expected, final Javadoc.Version actual) {
        if (expected == null) {
            assertNull(actual);
        } else
            assertEquals(expected.getContent(), actual.getContent());
    }

    private void assertJavadoc(final Javadoc.Return expected, final Javadoc.Return actual) {
        if (expected == null) {
            assertNull(actual);
        } else
            assertEquals(expected.getContent(), actual.getContent());
    }

    private void assertJavadoc(final Javadoc.Author expected, final Javadoc.Author actual) {
        if (expected == null) {
            assertNull(actual);
        } else
            assertEquals(expected.getContent(), actual.getContent());
    }

    private void assertJavadoc(final Javadoc.Deprecated expected, final Javadoc.Deprecated actual) {
        if (expected == null) {
            assertNull(actual);
        } else
            assertEquals(expected.getContent(), actual.getContent());
    }
}