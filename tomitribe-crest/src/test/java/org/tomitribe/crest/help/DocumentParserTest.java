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
import org.tomitribe.util.Join;

import static org.junit.Assert.assertEquals;

public class DocumentParserTest {

    @Test
    public void paragraphs() {

        final String content = "This option causes rsync to set the group of the destination file to be the\n" +
                "              same as the source file. If the receiving program is not running as the super-user\n" +
                "              (or if --no-super was specified), only groups that the invoking user on the receiving\n" +
                "              side is a member of will be preserved. Without this option, the group is set to the\n" +
                "              default group of the invok- ing user on the receiving side.\n" +
                "\n" +
                "              The preservation of group information will associate matching names by default, but\n" +
                "              may fall back to using the ID number in some circumstances (see also the --numeric-ids\n" +
                "              option for a full discussion).";
        final Document document = DocumentParser.parser(content);

        final String actual = Join.join("\n", document.elements);
        assertEquals("" +
                "Paragraph{content='This option causes rsync to set the group of the destination" +
                " file to be the same as the source file. If the receiving program is not running " +
                "as the super-user (or if --no-super was specified), only groups that the invoking " +
                "user on the receiving side is a member of will be preserved. Without this option, " +
                "the group is set to the default group of the invok- ing user on the receiving side.'}\n" +
                "Paragraph{content='The preservation of group information will associate matching " +
                "names by default, but may fall back to using the ID number in some circumstances " +
                "(see also the --numeric-ids option for a full discussion).'}", actual);
    }

    @Test
    public void headings() {

        final String content = "=  this is a heading\n" +
                "              This is the source file. If the receiving program is not running as the super-user\n" +
                "              (or if --no-super was specified), only groups that the invoking user on the receiving\n" +
                "\n" +
                "              SECTION 2\n" +
                "              May fall back to using the ID number in some circumstances (see also the --numeric-ids\n" +
                "              option for a full discussion).\n" +
                "              #   section 3\n" +
                "              The preservation of group information will associate matching\n";
        final Document document = DocumentParser.parser(content);

        final String actual = Join.join("\n", document.elements);
        assertEquals("" +
                "Heading{content='this is a heading'}\n" +
                "Paragraph{content='This is the source file. If the receiving" +
                " program is not running as the super-user (or if --no-super " +
                "was specified), only groups that the invoking user on the receiving'}\n" +
                "Heading{content='SECTION 2'}\n" +
                "Paragraph{content='May fall back to using the ID number in " +
                "some circumstances (see also the --numeric-ids option for a full discussion).'}\n" +
                "Heading{content='section 3'}\n" +
                "Paragraph{content='The preservation of group information will associate matching'}", actual);
    }

    @Test
    public void headings2() {
        assertHeading("# this is awesome", "this is awesome");
        assertHeading("## this is awesome", "this is awesome");
        assertHeading("##### this is awesome", "this is awesome");
        assertHeading("= this is awesome", "this is awesome");
        assertHeading("=this is awesome", "this is awesome");
        assertHeading("===this is awesome", "this is awesome");
        assertHeading("===    this is awesome", "this is awesome");
        assertHeading("===    this is awesome   ", "this is awesome");
        assertHeading("= this is awesome!", "this is awesome!");
        assertHeading("= this is awesome!?(YES:NO)", "this is awesome!?(YES:NO)");
        assertHeading("= this is awesome!?(YES:NO) - Maybe; not", "this is awesome!?(YES:NO) - Maybe; not");
        assertHeading("THIS IS AWESOME!?(YES:NO) - MAYBE; NOT", "THIS IS AWESOME!?(YES:NO) - MAYBE; NOT");
        assertHeading("THIS, IS AWESOME", "THIS, IS AWESOME");
        assertHeading("DUNGEONS & DRAGONS", "DUNGEONS & DRAGONS");
        assertHeading("DUNGEONS + DRAGONS", "DUNGEONS + DRAGONS");
        assertHeading("I LOVE `CODE`", "I LOVE `CODE`");
        assertHeading("AROUND 300~ SPARTANS", "AROUND 300~ SPARTANS");
    }


    @Test
    public void bullets() {
        final String content = "" +
                "This option causes rsync to set the group of the destination file to be the\n" +
                "same as the source file. If the receiving program is not running as the super-user\n" +
                " - only groups that the invoking user on the receiving\n" +
                " - side is a member of will be preserved.\n" +
                " - Without this option, the group is set to the default group of the invok- ing user on the receiving side.\n" +
                "\n" +
                "The preservation of group information will associate matching names by default, but\n" +
                "may fall back to using the ID number in some circumstances (see also the --numeric-ids\n" +
                "option for a full discussion).";
        final Document document = DocumentParser.parser(content);
        final String actual = Join.join("\n", document.elements);
        assertEquals("" +
                "Paragraph{content='This option causes rsync to set the group of the " +
                "destination file to be the same as the source file. If the receiving " +
                "program is not running as the super-user'}\n" +
                "" +
                "Bullet{content='only groups that the invoking user on the receiving'}\n" +
                "Bullet{content='side is a member of will be preserved.'}\n" +
                "Bullet{content='Without this option, the group is set to the default group" +
                " of the invok- ing user on the receiving side.'}\n" +
                "" +
                "Paragraph{content='The preservation of group information will associate " +
                "matching names by default, but may fall back to using the ID number in some " +
                "circumstances (see also the --numeric-ids option for a full discussion).'}", actual);
    }

    @Test
    public void multilineBullets() {
        final String content = "" +
                "This option causes rsync to set the group of the destination file to be the\n" +
                "same as the source file. " +
                " - If the receiving program is not running as the super-user\n" +
                "   only groups that the invoking user on the receiving\n" +
                "   side is a member of will be preserved.\n" +
                " - Without this option, the group is set to the\n" +
                "   default group of the invok- ing user on the receiving side.\n" +
                "\n" +
                "The preservation of group information will associate matching names by default, but\n" +
                "may fall back to using the ID number in some circumstances (see also the --numeric-ids\n" +
                "option for a full discussion).";
        final Document document = DocumentParser.parser(content);
        final String actual = Join.join("\n", document.elements);
        assertEquals("" +
                "Paragraph{content='This option causes rsync to set the group " +
                "of the destination file to be the same as the source file. - If " +
                "the receiving program is not running as the super-user only groups" +
                " that the invoking user on the receiving side is a member of will " +
                "be preserved.'}\n" +
                "Bullet{content='Without this option, the group is set to the default " +
                "group of the invok- ing user on the receiving side.'}\n" +
                "Paragraph{content='The preservation of group information will associate " +
                "matching names by default, but may fall back to using the ID number in " +
                "some circumstances (see also the --numeric-ids option for a full " +
                "discussion).'}", actual);
    }

    @Test
    public void preformatted() {
        final String content = "" +
                "This option causes rsync to set the group of the destination file to be the\n" +
                "same as the source file. If the receiving program is not running as the super-user\n" +
                "\n" +
                "    for (int i = 0; i < 5; i++) {\n" +
                "      System.out.println(i);\n" +
                "    }" +
                "\n" +
                "The preservation of group information will associate matching names by default, but\n" +
                "may fall back to using the ID number in some circumstances (see also the --numeric-ids\n" +
                "option for a full discussion).";
        final Document document = DocumentParser.parser(content);
        final String actual = Join.join("\n", document.elements);
        assertEquals("" +
                "Paragraph{content='This option causes rsync to set the group of the destination " +
                "file to be the same as the source file. If the receiving program is not running " +
                "as the super-user'}\n" +
                "Preformatted{content='for (int i = 0; i < 5; i++) {\n" +
                "  System.out.println(i);\n" +
                "}'}\n" +
                "Paragraph{content='The preservation of group information will associate matching " +
                "names by default, but may fall back to using the ID number in some circumstances " +
                "(see also the --numeric-ids option for a full discussion).'}", actual);
    }

    private void assertHeading(final String input, final String expected) {
        final Document document = DocumentParser.parser(input);
        assertEquals("Expected a single header", 1, document.getElements().size());
        assertEquals(expected, document.getElements().get(0).getContent());
    }

}