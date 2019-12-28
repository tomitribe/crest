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

import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavadocParser {

    private final Javadoc.Builder javadoc = Javadoc.builder();
    private final String content;

    public JavadocParser(final String content) {
        this.content = content;
    }

    private Javadoc parse() {
        final ArrayList<String> parts = splitParts();

        if (parts.size() == 0) return javadoc.build();

        if (!parts.get(0).startsWith("@")) {
            javadoc.content(parts.remove(0));
        }

        final List<Javadoc.Tag> tags = parts.stream()
                .filter(s -> s.startsWith("@"))
                .map(JavadocParser::toTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (final Javadoc.Tag tag : tags) {
            convert(tag);
        }
        return javadoc.build();
    }

    private void convert(final Javadoc.Tag tag) {
        if ("param".equals(tag.getName())) {
            param(tag);
        } else if ("throws".equals(tag.getName())) {
            throwing(tag);
        } else if ("author".equals(tag.getName())) {
            author(tag);
        } else if ("see".equals(tag.getName())) {
            see(tag);
        } else if ("return".equals(tag.getName())) {
            returning(tag);
        } else if ("since".equals(tag.getName())) {
            since(tag);
        } else if ("version".equals(tag.getName())) {
            version(tag);
        } else if ("deprecated".equals(tag.getName())) {
            deprecated(tag);
        } else {
            javadoc.unknown(tag);
        }
    }

    private void deprecated(final Javadoc.Tag tag) {
        javadoc.deprecated(new Javadoc.Deprecated(tag.getContent()));
    }

    private void version(final Javadoc.Tag tag) {
        javadoc.version(new Javadoc.Version(tag.getContent()));
    }

    private void since(final Javadoc.Tag tag) {
        javadoc.since(new Javadoc.Since(tag.getContent()));
    }

    private void returning(final Javadoc.Tag tag) {
        javadoc.aReturn(new Javadoc.Return(tag.getContent()));
    }

    private void see(final Javadoc.Tag tag) {
        javadoc.see(new Javadoc.See(tag.getContent()));
    }

    private void author(final Javadoc.Tag tag) {
        javadoc.author(new Javadoc.Author(tag.getContent()));
    }

    private void throwing(final Javadoc.Tag tag) {
        final Map.Entry<String, String> entry = parseKeyValue(tag.getContent());
        if (entry != null) javadoc.throwing(new Javadoc.Throws(entry.getKey(), entry.getValue()));
    }

    private void param(final Javadoc.Tag tag) {
        final Map.Entry<String, String> entry = parseKeyValue(tag.getContent());
        if (entry != null) javadoc.param(new Javadoc.Param(entry.getKey(), entry.getValue()));
    }

    static Javadoc.Tag toTag(String text) {
        // Strip off the leading '@'
        text = text.substring(1).trim();

        final Map.Entry<String, String> pair = parseKeyValue(text);

        return new Javadoc.Tag(pair.getKey(), pair.getValue());
    }

    private static Map.Entry<String, String> parseKeyValue(final String text) {
        final String delimiter = "\000\000\000";
        final String[] parts = text
                .replaceFirst("[\n\t ]", delimiter)
                .split(delimiter);

        if (parts.length == 0) {

            return null;

        } else if (parts.length == 1) {

            return new Pair(parts[0], null);

        } else if (parts.length == 2) {

            final String name = parts[0].trim();
            final String content = parts[1].trim();

            return new Pair(name, content);

        } else {
            // Wow, they have javadoc with \000\000\000 in it
            // What the heck are they doing?
            // Anyway, let's handle it
            final ArrayList<String> strings = new ArrayList<>(Arrays.asList(parts));
            final String name = strings.remove(0).trim();
            final String content = Join.join(delimiter, strings).trim();
            return new Pair(name, content);
        }
    }

    public static class Pair implements Map.Entry<String, String> {
        private final String key;
        private final String value;

        Pair(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(final String value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Three things make parsing Javadoc interesting
     *
     * - tags can have any amount of leading spaces before '@'
     * - tags can also be multi-line and contain line breaks like newline
     * - the '@' character can be used inside descriptions
     *
     * It would be tempting to parse line-by-line, however it would result
     * in some pretty complicated buffering lines for multi-line content.
     *
     * Instead we simply split our content using '\n *@' as our regex and
     * we are done with the bulk of parsing and need only refine the parts.
     *
     * The result of the parsing will be a list where the first entry
     * will contain the javadoc text, if any, and the subsequent parts
     * contain the individual tags.
     */
    private ArrayList<String> splitParts() {
        final String[] parts = content
                .replaceAll("\n *@", "\n\000@")
                .split("\n\000");

        return new ArrayList<>(Arrays.asList(parts));
    }

    public Javadoc build() {
        return javadoc.build();
    }

    public static Javadoc parse(final String content) {
        return new JavadocParser(content).parse();
    }
}
