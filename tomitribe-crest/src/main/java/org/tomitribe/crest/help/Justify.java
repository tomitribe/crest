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

import java.util.function.Function;

public class Justify {

    private final int width;
    private Function<String, String> padding;

    public Justify(final int width) {
        this.width = width;
        this.padding = this::fromLeft;
    }

    public static String wrapAndJustify(final String text, final int width) {
        final Justify justify = new Justify(width);
        return justify.wrapAndJustify(text);
    }

    private String wrapAndJustify(final String text) {
        final String wrapped = Wrap.wrap(text, width, null, true);

        final String[] lines = wrapped.split("\n");

        for (int i = 0; i < lines.length; i++) {
            final String padded = padding.apply(lines[i]);

            /*
             * If padding resulted in a perfect line length, use it
             * If not discard the attempt -- we're probably on the last
             * wrapped line.
             */
            if (padded.length() == width) {
                lines[i] = padded;
            }
        }
        return Join.join("\n", (Object[]) lines);

    }

    private String fromLeft(final String line) {
        final StringBuilder b = new StringBuilder(line);
        int needed = width - b.length();
        needed = padSentences(b, needed);
        for (int i = 0; needed > 0 && i < b.length(); i++) {
            final char c = b.charAt(i);
            if (c == ' ') {
                b.insert(i, ' ');
                i++;
                needed--;
            }
        }

        this.padding = this::fromRight;

        return b.toString();
    }

    private String fromRight(final String line) {
        final StringBuilder b = new StringBuilder(line);
        int needed = width - b.length();
        needed = padSentences(b, needed);
        for (int i = b.length() - 1; needed > 0 && i > 0; i--) {
            final char c = b.charAt(i);
            if (c == ' ') {
                b.insert(i, ' ');
                needed--;
            }
        }

        this.padding = this::fromLeft;

        return b.toString();
    }

    private int padSentences(final StringBuilder b, int needed) {
        if (needed == 0) return needed;
        for (int i = 0; needed > 0 && i < b.length(); i++) {
            final char c = b.charAt(i);
            if (c == '.') {
                if (b.length() > i + 1 && b.charAt(i + 1) == ' ') {
                    b.insert(i + 1, ' ');
                    i += 2;
                    needed--;
                }
            }
        }

        return needed;
    }
}
