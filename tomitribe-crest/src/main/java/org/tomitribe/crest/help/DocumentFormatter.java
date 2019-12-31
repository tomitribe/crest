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

import org.tomitribe.util.PrintString;
import org.tomitribe.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentFormatter {

    private final int indent = 7;
    private final int width;
    private final int column;
    private final boolean color;

    public DocumentFormatter(final int width) {
        this(width, true);
    }

    public DocumentFormatter(final int width, final boolean color) {
        this.width = width;
        this.column = width - indent - indent;
        this.color = color;
    }

    public String format(final Document document) {
        final PrintString out = new PrintString();

        final Highlight highlighter = new Highlight(getFlagss(document));

        final Function<String, String> highlight = color ? highlighter::highlight : Function.identity();
        final Function<String, String> highlightKeywords = color ? highlighter::matches : Function.identity();

        final Iterator<Element> iterator = document.getElements().iterator();
        while (iterator.hasNext()) {
            final Element element = iterator.next();

            if (element instanceof Heading) {

                final Heading heading = (Heading) element;
                final String text = Strings.uppercase(heading.getContent());

                out.println(highlight.apply(text));

            } else if (element instanceof Paragraph) {
                final Paragraph paragraph = (Paragraph) element;
                final String content = Justify.wrapAndJustify(paragraph.getContent() + "", column);
                Stream.of(content.split("\n"))
                        .map(highlightKeywords)
                        .forEach(s -> out.format("       %s%n", s));

                if (iterator.hasNext()) out.println();

            } else if (element instanceof Option) {
                final Option option = (Option) element;

                final DocumentFormatter formatter = new DocumentFormatter(column, false);
                final String content = formatter.format(option.getDocument());

                final List<String> lines;
                if (content == null || content.length() == 0) {
                    lines = Collections.EMPTY_LIST;
                } else {
                    lines = new ArrayList<>(Arrays.asList(content.split("\n")));
                }


                if (option.getFlag().length() < 7 && lines.size() > 0) {
                    final String firstLine = lines.remove(0).trim();
                    if (color) {
                        out.format("       \033[0m\033[1m%-6s\033[0m %s%n",
                                option.getFlag(),
                                highlightKeywords.apply(firstLine));
                    } else {
                        out.format("       %-6s %s%n", option.getFlag(), firstLine);
                    }
                    lines.stream()
                            .map(highlightKeywords)
                            .forEach(s -> out.format("       %s%n", s));
                } else {
                    final String flag = highlight.apply(option.getFlag());
                    out.format("       %s%n", flag);
                    lines.stream()
                            .map(highlightKeywords)
                            .forEach(s -> out.format("       %s%n", s));
                }

                if (iterator.hasNext()) out.println();

            } else if (element instanceof Bullet) {
                final Bullet bullet = (Bullet) element;

                final String content = Wrap.wrap(bullet.getContent(), column - indent);
                final List<String> lines = Stream.of(content.split("\n"))
                        .map(s -> String.format("       %s", s))
                        .map(highlightKeywords)
                        .collect(Collectors.toList());


                { // Set the first line as the bullet
                    final String first = lines.get(0).substring(1);
                    lines.set(0, "o" + first);
                }

                lines.forEach(s -> out.format("       %s%n", s));

                if (iterator.hasNext()) out.println();

            } else if (element instanceof Preformatted) {
                final Preformatted preformatted = (Preformatted) element;
                Stream.of(preformatted.getContent().split("\n"))
                        .forEach(s -> out.format("           %s%n", s));

                if (iterator.hasNext()) out.println();
            }
        }
        return out.toString();
    }

    public static List<String> getFlags(final Document document) {
        final List<Option> flagss = getFlagss(document);
        return Highlight.flags(flagss);
    }

    public static List<Option> getFlagss(final Document document) {
        return document.getElements().stream()
                .filter(element -> element instanceof Option)
                .map(Option.class::cast)
                .collect(Collectors.toList());
    }
}
