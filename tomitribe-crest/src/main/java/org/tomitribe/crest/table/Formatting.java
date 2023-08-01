/*
 * Copyright 2021 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.table;

import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.term.Screen;
import org.tomitribe.util.collect.ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Formatting {

    private Formatting() {
    }

    static Data asTable(final Iterable<?> iterable, final Options options) {
        final String[] sort = getSortArray(options);
        String[] fields = getFieldsArray(options);

        final List<List<Item>> rows = new ArrayList<>();

        for (final Object item : iterable) {
            final CaseInsensitiveMap map = asMap(item);

            if (fields == null) {
                final Set<String> keys = new LinkedHashSet<>(map.keySet());

                if (map.isObject()) {
                    // Do not show class in any default contexts
                    // People can select it explicitly if they want it
                    keys.remove("class");
                    fields = keys.toArray(new String[0]);

                } else {
                    fields = keys.stream()
                            .map(Parts::escape)
                            .toArray(String[]::new);
                }
            }

            final List<Item> row = new ArrayList<>();

            for (final String field : fields) {
                row.add(resolve(map, field));
            }
            rows.add(row);
        }

        if (sort != null && sort.length > 0) {
            rows.sort(compareFields(fields, sort));
        }

        final Data.Builder data = Data.builder();

        if (options.header()) {
            // Add the headers
            data.headings(true);
            data.row(unescape(fields));
        }

        for (final List<Item> row : rows) {
            final String[] a = new String[fields.length];
            for (int i = 0; i < a.length; i++) {
                a[i] = row.get(i).getString();
            }
            data.row(a);
        }

        return data.build();
    }

    private static String[] unescape(final String[] fields) {
        final String[] headings = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            headings[i] = Parts.unescape(fields[i]);
        }
        return headings;
    }

    private static CaseInsensitiveMap asMap(final Object item) {
        if (item instanceof CaseInsensitiveMap) {
            return (CaseInsensitiveMap) item;
        }

        if (item instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) item;
            return new CaseInsensitiveMap(map, false);
        }

        /*
         * Convert the object to a map
         */
        final LinkedHashMap<Object, Object> sorted = new LinkedHashMap<>();
        new ObjectMap(item).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return new CaseInsensitiveMap(sorted, true);
    }

    private static Map<String, Object> toStringKeys(final Map<?, ?> map) {
        final Map<String, Object> dest = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            dest.put(entry.getKey().toString(), entry.getValue());
        }
        return dest;
    }

    static class CaseInsensitiveMap implements Map<String, Object> {
        final Map<String, Object> map;
        private final Map<String, String> caseInsensitive;
        private final boolean object;

        public CaseInsensitiveMap(final Map<?, ?> map, final boolean object) {
            this.map = toStringKeys(map);
            this.caseInsensitive = caseInsensitiveMapping(this.map);
            this.object = object;
        }

        public boolean isObject() {
            return object;
        }

        public Object get(final String name) {
            final Object value = map.get(name);
            if (value != null) return value;

            final String alternateCaseName = caseInsensitive.get(name.toLowerCase());
            if (alternateCaseName == null) return null;
            return map.get(alternateCaseName);
        }

        public Set<String> keySet() {
            return map.keySet();
        }

        private static Map<String, String> caseInsensitiveMapping(final Map<String, Object> map) {
            /*
             * To enable case-insensitive field names, create a
             * map of the field names in lower case form as the
             * key and the case-sensitive field name as the value
             */
            final Map<String, String> fieldMappings = new LinkedHashMap<>();
            for (final String field : map.keySet()) {
                fieldMappings.put(field.toLowerCase(), field);
            }
            return fieldMappings;
        }


        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(final Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(final Object key) {
            return get(key.toString());
        }

        @Override
        public Object put(final String key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(final Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(final Map<? extends String, ?> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Object> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException();
        }

    }

    private static String[] getSortArray(final Options options) {
        final String sort = options.getSort();
        final String[] sortArray;

        if (sort == null || sort.length() == 0) {
            sortArray = null;
        } else {
            sortArray = sort.split("[ ,]+");
        }
        return sortArray;
    }

    private static String[] getFieldsArray(final Options options) {
        final String[] fieldsArray;
        final String fields = options.getFields();
        if (fields == null || fields.length() == 0 || "all".equals(fields)) {
            fieldsArray = null;
        } else {
            fieldsArray = fields.split("[ ,]+");
        }
        return fieldsArray;
    }

    public static Comparator<List<Item>> compareFields(final String[] fields, final String[] sort) {
        return compareFields(Arrays.asList(fields), Arrays.asList(sort));
    }

    public static Comparator<List<Item>> compareFields(final List<String> fields, final List<String> sort) {

        Comparator<List<Item>> comparator = (o1, o2) -> 0;

        for (final String field : sort) {
            final int i = fields.indexOf(field);
            if (i < 0) continue;
            comparator = comparator.thenComparing(strings -> strings.get(i));
        }

        return comparator;
    }

    private static Item resolve(final Map<?, ?> map, final String field) {
        return resolve(map, parts(field));
    }

    private static Item resolve(final Map<?, ?> map, final List<String> parts) {

        if (parts.size() == 0) {
            return new Item("");
        }

        final String part = parts.remove(0);
        final Object object = map.get(part);

        if (object == null) {
            return new Item("");
        }

        if (parts.size() == 0) {
            return new Item(object);
        }

        return resolve(asMap(object), parts);
    }

    static List<String> parts(final String field) {
        return Parts.from(field);
    }

    public static PrintOutput asPrintStream(final String[][] strings) {
        final int guess = Screen.guessWidth();
        final int width = guess > 0 ? guess : 150;

        final Data data = new Data(strings, true);
        final Table table = new Table(data, Border.asciiCompact().build(), width);

        return table::format;
    }

    public static class Item implements Comparable<Item> {
        private final Comparable object;
        private final String string;

        public Item(final Object value) {
            this.object = value instanceof Comparable ? (Comparable) value : null;
            this.string = value != null ? value.toString() : "";
        }

        public String getString() {
            return string;
        }

        @Override
        public int compareTo(final Item that) {
            if (this.object != null || that.object != null) {
                if (that.object == null) return 1;
                if (this.object == null) return -1;
                return this.object.compareTo(that.object);
            } else {
                return this.string.compareTo(that.string);
            }
        }
    }
}
