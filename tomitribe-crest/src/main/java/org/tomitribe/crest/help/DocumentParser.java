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

import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentParser {

    private final String content;
    private final Pattern leadingSpaces = Pattern.compile("^( *)");
    private final Pattern bullet = Pattern.compile("^( *-) *(.+)");
    private final Pattern heading = Pattern.compile("^[=#]+ *(.+)|^([A-Z]+[^a-z]+)");
    private final Pattern preformatted = Pattern.compile("^    (.+)");
    private final Document.Builder doc = Document.builder();

    public DocumentParser(final String content) {
        this.content = content;
    }

    public static Document parser(String content) {
        if (HtmlDocumentParser.isHtml(content)) {
            return HtmlDocumentParser.parse(content);
        } else {
            return new DocumentParser(content).parse();
        }
    }

    private Document parse() {

        final List<String> lines = normalizeAndSplitContent();

        for (final String line : lines) {
            if (processTerminator(line)) continue;
            if (processHeader(line)) continue;
            if (processBullet(line)) continue;
            if (processPreformatted(line)) continue;
            processParagraph(line);
        }

        terminate();

        return doc.build();
    }

    private List<String> normalizeAndSplitContent() {
        List<String> lines = new ArrayList<>(Arrays.asList(content.trim()
                .replace("\t", "    ")
                .replaceAll("\n *\n", "\n\n")
                .split("\n")));

        lines = stripIndent(lines);
        return lines;
    }

    private boolean processPreformatted(final String line) {
        final Matcher matcher = preformatted.matcher(line);
        if (!matcher.find()) return false;

        if (state != null && !(state instanceof ReadingPreformatted)) {
            state.terminate();
            state = null;
        }

        if (state == null) {
            state = new ReadingPreformatted();
        }

        state.process(matcher.group(1));

        return true;
    }

    private boolean processTerminator(final String line) {
        if (line.length() != 0) return false;

        terminate();

        return true;
    }

    private void processParagraph(final String line) {
        if (state != null && !(state instanceof ReadingParagraph)) {
            state.terminate();
            state = null;
        }

        if (state == null) {
            state = new ReadingParagraph();
        }

        state.process(line);
    }

    private boolean processHeader(final String line) {
        final Matcher matcher = heading.matcher(line);
        if (!matcher.find()) return false;


        if (matcher.group(1) != null) {
            terminate();
            final String text = matcher.group(1);
            doc.heading(text);
            return true;
        }
        if (matcher.group(2) != null) {
            terminate();
            final String text = matcher.group(2);
            doc.heading(text);
            return true;
        }

        return false;
    }

    private boolean processBullet(final String line) {
        { // Is this line the start of a bullet?
            final Matcher matcher = bullet.matcher(line);
            if (matcher.find()) {
                terminate();

                final String prefix = matcher.group(1);
                final String text = matcher.group(2);

                this.state = new ReadingBullet(prefix);
                this.state.process(text);

                return true;
            }
        }

        // Is this line a continuation of a bullet?
        if (state instanceof ReadingBullet){
            final ReadingBullet readingBullet = (ReadingBullet) this.state;
            final Matcher matcher = readingBullet.continued.matcher(line);
            if (!matcher.find()) return false;

            final String text = matcher.group(1);
            readingBullet.process(text);
            return true;
        }

        return false;
    }


    private void terminate() {
        if (state == null) return;
        state.terminate();
        state = null;
    }


    private State state;

    public interface State {
        void process(final String line);

        void terminate();
    }

    private class ReadingParagraph implements State {
        private final List<String> lines = new ArrayList<>();

        @Override
        public void process(final String line) {
            lines.add(line.trim());
        }

        @Override
        public void terminate() {
            final String content = Join.join(" ", lines)
                    .replaceAll("  +", " ");
            doc.paragraph(content);
        }
    }

    private class ReadingPreformatted implements State {
        private final List<String> lines = new ArrayList<>();

        @Override
        public void process(final String line) {
            lines.add(line);
        }

        @Override
        public void terminate() {
            doc.preformatted(Join.join("\n", lines));
        }
    }

    private class ReadingBullet implements State {
        private final List<String> lines = new ArrayList<>();
        private final Pattern continued;

        public ReadingBullet(final String bullet) {
            final String spaces = bullet.replaceAll(".", " ");
            this.continued = Pattern.compile(String.format("^%s(.+)", spaces));
        }

        @Override
        public void process(final String line) {
            lines.add(line);
        }

        @Override
        public void terminate() {
            final String content = Join.join(" ", lines)
                    .replaceAll("  +", " ");

            doc.bullet(content);
        }
    }

    private List<String> stripIndent(final List<String> lines) {
        final String first = lines.remove(0);

        int indent = getIndent(lines);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() < indent) continue;
            line = line.substring(indent);
            lines.set(i, line);
        }
        lines.add(0, first);
        return lines;
    }

    private int getIndent(final List<String> lines) {
        int indent = Integer.MAX_VALUE;
        for (final String line : lines) {
            if (line.length() == 0) continue;
            final Matcher matcher = leadingSpaces.matcher(line);
            if (matcher.find()) {
                final String spaces = matcher.group(1);
                indent = Math.min(indent, spaces.length());
            }
        }
        return indent;
    }

}
