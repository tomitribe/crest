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

import org.tomitribe.crest.help.Wrap;

import java.util.stream.Stream;

public class Width {
    private final int min;
    private final int max;

    public Width(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    public static Width ofString(final String string) {
        final String wrapped = Wrap.wrap(string, 1);

        final int maximum = string.length();
        final int minimum = Stream.of(wrapped.split("\n"))
                .map(String::length)
                .reduce(Math::max)
                .orElse(0);

        return new Width(minimum, maximum);
    }

    public Width adjust(final Width that) {
        final int min = Math.max(this.min, that.min);
        final int max = Math.max(this.max, that.max);

        return new Width(min, max);
    }

    public Width add(final Width that) {
        final int min = this.min + that.min;
        final int max = this.max + that.max;

        return new Width(min, max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "Width{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
