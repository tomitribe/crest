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
package org.tomitribe.crest.help;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for Wrap class.
 */
public class WrapTest {

    //-----------------------------------------------------------------------
    @Test
    public void testWrap_StringInt() {
        assertNull(Wrap.wrap(null, 20));
        assertNull(Wrap.wrap(null, -1));

        assertEquals("", Wrap.wrap("", 20));
        assertEquals("", Wrap.wrap("", -1));

        // normal
        final String systemNewLine = System.lineSeparator();
        String input = "Here is one line of text that is going to be wrapped after 20 columns.";
        String expected = "Here is one line of" + systemNewLine + "text that is going"
                + systemNewLine + "to be wrapped after" + systemNewLine + "20 columns.";
        assertEquals(expected, Wrap.wrap(input, 20));

        // long word at end
        input = "Click here to jump to the commons website - https://commons.apache.org";
        expected = "Click here to jump" + systemNewLine + "to the commons" + systemNewLine
                + "website -" + systemNewLine + "https://commons.apache.org";
        assertEquals(expected, Wrap.wrap(input, 20));

        // long word in middle
        input = "Click here, https://commons.apache.org, to jump to the commons website";
        expected = "Click here," + systemNewLine + "https://commons.apache.org," + systemNewLine
                + "to jump to the" + systemNewLine + "commons website";
        assertEquals(expected, Wrap.wrap(input, 20));

        // leading spaces on a new line are stripped
        // trailing spaces are not stripped
        input = "word1             word2                        word3";
        expected = "word1  " + systemNewLine + "word2  " + systemNewLine + "word3";
        assertEquals(expected, Wrap.wrap(input, 7));
    }

    @Test
    public void testWrap_StringIntStringBoolean() {
        assertNull(Wrap.wrap(null, 20, "\n", false));
        assertNull(Wrap.wrap(null, 20, "\n", true));
        assertNull(Wrap.wrap(null, 20, null, true));
        assertNull(Wrap.wrap(null, 20, null, false));
        assertNull(Wrap.wrap(null, -1, null, true));
        assertNull(Wrap.wrap(null, -1, null, false));

        assertEquals("", Wrap.wrap("", 20, "\n", false));
        assertEquals("", Wrap.wrap("", 20, "\n", true));
        assertEquals("", Wrap.wrap("", 20, null, false));
        assertEquals("", Wrap.wrap("", 20, null, true));
        assertEquals("", Wrap.wrap("", -1, null, false));
        assertEquals("", Wrap.wrap("", -1, null, true));

        // normal
        String input = "Here is one line of text that is going to be wrapped after 20 columns.";
        String expected = "Here is one line of\ntext that is going\nto be wrapped after\n20 columns.";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", false));
        assertEquals(expected, Wrap.wrap(input, 20, "\n", true));

        // unusual newline char
        input = "Here is one line of text that is going to be wrapped after 20 columns.";
        expected = "Here is one line of<br />text that is going<br />to be wrapped after<br />20 columns.";
        assertEquals(expected, Wrap.wrap(input, 20, "<br />", false));
        assertEquals(expected, Wrap.wrap(input, 20, "<br />", true));

        // short line length
        input = "Here is one line";
        expected = "Here\nis one\nline";
        assertEquals(expected, Wrap.wrap(input, 6, "\n", false));
        expected = "Here\nis\none\nline";
        assertEquals(expected, Wrap.wrap(input, 2, "\n", false));
        assertEquals(expected, Wrap.wrap(input, -1, "\n", false));

        // system newline char
        final String systemNewLine = System.lineSeparator();
        input = "Here is one line of text that is going to be wrapped after 20 columns.";
        expected = "Here is one line of" + systemNewLine + "text that is going" + systemNewLine
                + "to be wrapped after" + systemNewLine + "20 columns.";
        assertEquals(expected, Wrap.wrap(input, 20, null, false));
        assertEquals(expected, Wrap.wrap(input, 20, null, true));

        // with extra spaces
        input = " Here:  is  one  line  of  text  that  is  going  to  be  wrapped  after  20  columns.";
        expected = "Here:  is  one  line\nof  text  that  is \ngoing  to  be \nwrapped  after  20 \ncolumns.";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", false));
        assertEquals(expected, Wrap.wrap(input, 20, "\n", true));

        // with tab
        input = "Here is\tone line of text that is going to be wrapped after 20 columns.";
        expected = "Here is\tone line of\ntext that is going\nto be wrapped after\n20 columns.";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", false));
        assertEquals(expected, Wrap.wrap(input, 20, "\n", true));

        // with tab at wrapColumn
        input = "Here is one line of\ttext that is going to be wrapped after 20 columns.";
        expected = "Here is one line\nof\ttext that is\ngoing to be wrapped\nafter 20 columns.";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", false));
        assertEquals(expected, Wrap.wrap(input, 20, "\n", true));

        // difference because of long word
        input = "Click here to jump to the commons website - https://commons.apache.org";
        expected = "Click here to jump\nto the commons\nwebsite -\nhttps://commons.apache.org";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", false));
        expected = "Click here to jump\nto the commons\nwebsite -\nhttps://commons.apac\nhe.org";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", true));

        // difference because of long word in middle
        input = "Click here, https://commons.apache.org, to jump to the commons website";
        expected = "Click here,\nhttps://commons.apache.org,\nto jump to the\ncommons website";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", false));
        expected = "Click here,\nhttps://commons.apac\nhe.org, to jump to\nthe commons website";
        assertEquals(expected, Wrap.wrap(input, 20, "\n", true));
    }

    @Test
    public void testWrap_StringIntStringBooleanString() {

        //no changes test
        String input = "flammable/inflammable";
        String expected = "flammable/inflammable";
        assertEquals(expected, Wrap.wrap(input, 30, "\n", false, "/"));

        // wrap on / and small width
        expected = "flammable\ninflammable";
        assertEquals(expected, Wrap.wrap(input, 2, "\n", false, "/"));

        // wrap long words on / 1
        expected = "flammable\ninflammab\nle";
        assertEquals(expected, Wrap.wrap(input, 9, "\n", true, "/"));

        // wrap long words on / 2
        expected = "flammable\ninflammable";
        assertEquals(expected, Wrap.wrap(input, 15, "\n", true, "/"));

        // wrap long words on / 3
        input = "flammableinflammable";
        expected = "flammableinflam\nmable";
        assertEquals(expected, Wrap.wrap(input, 15, "\n", true, "/"));
    }


    @Test
    public void testLANG1292() {
        // Prior to fix, this was throwing StringIndexOutOfBoundsException
        Wrap.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 70);
    }

    @Test
    public void testLANG1397() {
        // Prior to fix, this was throwing StringIndexOutOfBoundsException
        Wrap.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
    }
}