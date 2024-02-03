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
package org.tomitribe.crest.table;

public class Line {
    private final String left;
    private final String right;
    private final String inner;
    private final String middle;

    private final boolean padded;

    public Line(final String left, final String right, final String inner, final String middle, final boolean padded) {
        this.left = left;
        this.right = right;
        this.inner = inner;
        this.middle = middle;
        this.padded = padded;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public String getMiddle() {
        return middle;
    }

    public String getInner() {
        return inner;
    }

    public boolean isPadded() {
        return padded;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String left;
        private String right;
        private String inner;
        private String middle;
        private boolean padded = true;

        private Builder() {
        }


        public Builder all(String string) {
            this.left = string;
            this.right = string;
            this.middle = string;
            this.inner = string;
            return this;
        }

        public Builder left(String left) {
            this.left = left;
            return this;
        }

        public Builder right(String right) {
            this.right = right;
            return this;
        }

        public Builder inner(String inner) {
            this.inner = inner;
            return this;
        }

        public Builder middle(String middle) {
            this.middle = middle;
            return this;
        }

        public Builder padded(final boolean padded) {
            this.padded = padded;
            return this;
        }


        public Line build() {
            return new Line(left, right, inner, middle, padded);
        }
    }
}
