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

import java.util.ArrayList;
import java.util.List;

public class Document {

    final List<Element> elements = new ArrayList<>();

    private Document(final List<Element> elements) {
        this.elements.addAll(elements);
    }

    public List<Element> getElements() {
        return elements;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        final List<Element> elements = new ArrayList<>();

        public Builder paragraph(final String content) {
            elements.add(new Paragraph(content));
            return this;
        }

        public Builder bullet(final String content) {
            elements.add(new Bullet(content));
            return this;
        }

        public Builder preformatted(final String content) {
            elements.add(new Preformatted(content));
            return this;
        }

        public Builder heading(final String content) {
            elements.add(new Heading(content));
            return this;
        }

        public Document build() {
            return new Document(elements);
        }
    }
}
