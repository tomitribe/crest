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
package org.tomitribe.crest.util;

import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.util.collect.ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generic include/exclude regex filter for list commands.
 *
 * Uses ObjectMap (same as Crest's @Table rendering) to read field values,
 * so filtering matches exactly what users see in the table output.
 *
 * Each pattern is tested against each field value individually (OR across fields).
 * Multiple --include patterns OR together.  Multiple --exclude patterns OR together.
 * Final result: include AND NOT exclude.
 *
 * Case sensitive by default.  Use --case-insensitive to apply Pattern.CASE_INSENSITIVE
 * to all patterns, or use (?i) inline in individual patterns for per-pattern control.
 */
@Options
public class Filter<T> implements Predicate<T> {

    private final Predicate<T> predicate;
    private final List<String> visibleFields;
    private final Includes includes;
    private final Excludes excludes;
    private final boolean caseInsensitive;

    public Filter(final Includes includes,
                  final Excludes excludes,
                  @Option("case-insensitive") @Default("false") final Boolean caseInsensitive) {
        this(includes, excludes, caseInsensitive, Collections.EMPTY_LIST);
    }

    protected Filter(final Includes includes,
                     final Excludes excludes,
                     final Boolean caseInsensitive,
                     final List<String> visibleFields) {

        this.includes = includes;
        this.excludes = excludes;
        this.caseInsensitive = caseInsensitive != null && caseInsensitive;
        this.visibleFields = new ArrayList<>();
        for (final String visibleField : visibleFields) {
            this.visibleFields.add(visibleField.toLowerCase());
        }

        if (!includes.hasPatterns() && !excludes.hasPatterns()) {
            this.predicate = this::accept;
        } else {
            this.predicate = this::filter;
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public Filter<T> fields(final String fieldList) {
        final String[] split = fieldList.toLowerCase().split(" +");
        final List<String> visibleFields = Arrays.asList(split);
        return new Filter<>(includes, excludes, caseInsensitive, visibleFields);
    }

    @Override
    public boolean test(final T record) {
        return predicate.test(record);
    }

    private boolean accept(final T record) {
        return true;
    }

    private boolean filter(final T record) {

        if (includes.hasPatterns()) {
            final boolean included = fieldValues(record).stream()
                    .anyMatch(value -> includes.matchesAny(value, caseInsensitive));
            if (!included) return false;
        }

        if (excludes.hasPatterns()) {
            final boolean excluded = fieldValues(record).stream()
                    .anyMatch(value -> excludes.matchesAny(value, caseInsensitive));
            if (excluded) return false;
        }

        return true;
    }

    private List<String> fieldValues(final T record) {

        if (visibleFields.isEmpty()) {
            return allFields(record);
        } else {
            return selectedFields(record, visibleFields);
        }
    }

    private static <T> List<String> allFields(final T record) {
        return new ObjectMap(record).entrySet().stream()
                .map(entry -> {
                    try {
                        final Object value = entry.getValue();
                        if (value == null) return null;
                        return value.toString();
                    } catch (final Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private <T> List<String> selectedFields(final T object, final List<String> visibleFields) {
        final List<String> values = new ArrayList<>();
        final Map<String, Object> map = lowercaseKeys(new ObjectMap(object));

        final Map<String, List<String>> children = new HashMap<>();

        for (final String string : visibleFields) {
            if (!string.contains(".")) {

                try {
                    final Object value = map.get(string);
                    if (value != null) {
                        values.add(value.toString());
                    }
                } catch (final Exception ignored) {
                }

            } else {

                final int index = string.indexOf('.');
                final String child = string.substring(0, index);
                final String field = string.substring(index + 1);

                children.computeIfAbsent(child, s -> new ArrayList<>()).add(field);
            }
        }

        for (final Map.Entry<String, List<String>> entry : children.entrySet()) {
            final String key = entry.getKey();
            final List<String> value = entry.getValue();
            final Object child = map.get(key);

            if (child == null) {
                continue;
            }

            final List<String> strings = selectedFields(child, value);
            values.addAll(strings);
        }

        return values;
    }

    @Options
    public static class Includes {
        private final List<Pattern> patterns;

        public Includes(@Option("include") final List<Pattern> patterns) {
            this.patterns = patterns;
        }

        public boolean hasPatterns() {
            return patterns != null && !patterns.isEmpty();
        }

        public boolean matchesAny(final String value, final boolean caseInsensitive) {
            if (patterns == null) return false;
            return patterns.stream().anyMatch(p -> matches(p, value, caseInsensitive));
        }
    }

    @Options
    public static class Excludes {
        private final List<Pattern> patterns;

        public Excludes(@Option("exclude") final List<Pattern> patterns) {
            this.patterns = patterns;
        }

        public boolean hasPatterns() {
            return patterns != null && !patterns.isEmpty();
        }

        public boolean matchesAny(final String value, final boolean caseInsensitive) {
            if (patterns == null) return false;
            return patterns.stream().anyMatch(p -> matches(p, value, caseInsensitive));
        }
    }

    private static boolean matches(final Pattern pattern, final String value, final boolean caseInsensitive) {
        if (caseInsensitive && (pattern.flags() & Pattern.CASE_INSENSITIVE) == 0) {
            return Pattern.compile(pattern.pattern(), Pattern.CASE_INSENSITIVE).matcher(value).find();
        }
        return pattern.matcher(value).find();
    }

    private static <V> Map<String, V> lowercaseKeys(final Map<String, V> map) {
        final LinkedHashMap<String, V> result = new LinkedHashMap<>();
        for (final Map.Entry<String, V> entry : map.entrySet()) {
            result.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        return result;
    }

    public static class Builder<T> {
        final List<Pattern> includes = new ArrayList<>();
        final List<Pattern> excludes = new ArrayList<>();
        final List<String> fields = new ArrayList<>();
        Boolean caseInsensitive;

        public Builder<T> field(final String field) {
            this.fields.add(field);
            return this;
        }

        public Builder<T> fields(final Iterable<String> fields) {
            Objects.requireNonNull(fields, "fields");
            fields.forEach(this::field);
            return this;
        }

        public Builder<T> include(final Pattern pattern) {
            includes.add(Objects.requireNonNull(pattern, "pattern"));
            return this;
        }

        public Builder<T> include(final String pattern) {
            return include(Pattern.compile(
                    Objects.requireNonNull(pattern, "pattern")
            ));
        }

        public Builder<T> includes(final Iterable<? extends Pattern> patterns) {
            Objects.requireNonNull(patterns, "patterns");
            patterns.forEach(this::include);
            return this;
        }

        public Builder<T> exclude(final Pattern pattern) {
            excludes.add(Objects.requireNonNull(pattern, "pattern"));
            return this;
        }

        public Builder<T> exclude(final String pattern) {
            return exclude(Pattern.compile(
                    Objects.requireNonNull(pattern, "pattern")
            ));
        }

        public Builder<T> excludes(final Iterable<? extends Pattern> patterns) {
            Objects.requireNonNull(patterns, "patterns");
            patterns.forEach(this::exclude);
            return this;
        }

        public Builder<T> caseInsensitive(final boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
            return this;
        }


        public Filter<T> build() {
            return new Filter<>(
                    new Includes(new ArrayList<>(includes)),
                    new Excludes(new ArrayList<>(excludes)),
                    caseInsensitive,
                    fields
            );
        }
    }
}
