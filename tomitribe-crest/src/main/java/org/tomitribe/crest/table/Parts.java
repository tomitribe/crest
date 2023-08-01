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

import org.tomitribe.util.Join;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class Parts {

    private final List<String> parsed = new ArrayList<>();
    private final Chars chars;
    private Mode mode = new ReadingToken();

    public Parts(final String path) {
        this.chars = new Chars(path.toCharArray());

        try {
            while (chars.hasNext()) {
                mode.accept(chars.next());
            }
        } finally {
            mode.close();
        }
    }

    public static List<String> from(final String path) {
        return new Parts(path).parsed;
    }

    public static String unescape(final String path) {
        final List<String> parts = from(path);
        return Join.join(".", parts);
    }

    public static String escape(final String key) {
        return key.replace(".", "\\.");
    }

    class ReadingToken implements Mode {
        private final StringBuilder token = new StringBuilder();

        @Override
        public void accept(final char c) {
            switch (c) {
                case '\\':
                    mode = new Escape();
                    break;
                case '.':
                    parsed.add(token.toString());
                    mode = new ReadingToken();
                    break;
                default:
                    token.append(c);
            }
        }

        @Override
        public void close() {
            parsed.add(token.toString());
        }

        class Escape implements Mode {
            @Override
            public void accept(final char c) {
                switch (c) {
                    case '\\':
                    case '.':
                        token.append(c);
                        break;
                    default:
                        token.append('\\');
                        token.append(c);
                }
                mode = ReadingToken.this;
            }

            @Override
            public void close() {
                token.append('\\');
                ReadingToken.this.close();
            }
        }
    }

    interface Mode extends Closeable {
        void accept(final char c);

        default void close() {

        }
    }

    private static class Chars {
        private final char[] chars;
        private int index;

        public Chars(final char[] chars) {
            this.chars = chars;
            this.index = 0;
        }

        public char next() {
            return chars[index++];
        }

        public boolean hasNext() {
            return index < chars.length;
        }
    }
}
