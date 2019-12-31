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

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Highlight {

    private final Pattern flags;
    private final Pattern code;

    public Highlight(final List<Option> options) {
        final List<String> flags = flags(options);
        final String format = String.format("([^A-Za-z0-9`-]|^)(%s)([^A-Za-z0-9-]|$)", Join.join("|", flags));
        this.flags = Pattern.compile(format);
        this.code = Pattern.compile("`()([^`]+)`()");
    }

    static List<String> flags(final List<Option> options) {
        return options.stream()
                .map(Option::getFlag)
                .map(s -> s.split(", "))
                .flatMap(Stream::of)
                .map(s -> s.replaceAll("=.*", ""))
                .map(String::trim)
                .filter(s -> s.startsWith("-"))
                .sorted(Comparator.reverseOrder())
                .sorted((o1, o2) -> Integer.compare(o2.length(), o1.length()))
                .collect(Collectors.toList());
    }


    public String highlight(String text) {
        return String.format("\033[0m\033[1m%s\033[0m", text);
    }

    public String matches(String text) {
        text = matches(text, flags);
        text = matches(text, code);
        return text;
    }

    private String matches(final String text, final Pattern code) {
        return code.matcher(text).replaceAll("$1\033[0m\033[1m$2\033[0m$3");
    }
}
