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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentFormatter {

    private final int indent = 7;
    private final int width;
    private int column;

    public DocumentFormatter(final int width) {
        this.width = width;
        this.column = width - indent - indent;
    }

    public String format(final Document document) throws IOException {
        final PrintString out = new PrintString();

        final Iterator<Element> iterator = document.getElements().iterator();
        while (iterator.hasNext()) {
            final Element element = iterator.next();

            if (element instanceof Heading) {

                final Heading heading = (Heading) element;
                out.println(Strings.uppercase(heading.getContent()));

            } else if (element instanceof Paragraph) {

                final Paragraph paragraph = (Paragraph) element;
                final String content = Justify.wrapAndJustify(paragraph.getContent(), column);
                Stream.of(content.split("\n"))
                        .forEach(s -> out.format("       %s%n", s));

                if (iterator.hasNext()) out.println();

            } else if (element instanceof Bullet) {
                final Bullet bullet = (Bullet) element;

                final String content = Wrap.wrap(bullet.getContent(), column - indent);
                final List<String> lines = Stream.of(content.split("\n"))
                        .map(s -> String.format("       %s", s))
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
                        .forEach(s -> out.format("       %s%n", s));

                if (iterator.hasNext()) out.println();
            }
        }
        return out.toString();
    }
}
