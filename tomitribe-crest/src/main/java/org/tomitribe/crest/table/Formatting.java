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

import org.tomitribe.util.Join;
import org.tomitribe.util.collect.ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Formatting {

    private Formatting() {
    }

    public static <T> String[][] asTable(final Iterable<T> items, final String fields, final String sort) {
        final String[] sortArray;
        final String[] fieldsArray;

        if (sort == null || sort.length() == 0) {
            sortArray = null;
        } else {
            sortArray = sort.split("[ ,]+");
        }

        if (fields == null || fields.length() == 0 || "all".equals(fields)) {
            fieldsArray = null;
        } else {
            fieldsArray = fields.split("[ ,]+");
        }

        return asTable(items, fieldsArray, sortArray);
    }

    private static <T> String[][] asTable(final Iterable<T> items, String[] fields, final String[] sort) {
        final List<List<Item>> rows = new ArrayList<>();

        for (final T item : items) {
            final ObjectMap map = new ObjectMap(item);

            if (fields == null) {
                fields = map.keySet().toArray(new String[0]);
                Arrays.sort(fields);
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

        final String[][] data = new String[rows.size() + 1][fields.length];
        int rowCount = 0;

        // Add the headers
        data[rowCount++] = fields;

        for (final List<Item> row : rows) {
            final String[] a = new String[fields.length];
            for (int i = 0; i < a.length; i++) {
                a[i] = row.get(i).getString();
            }
            data[rowCount++] = a;
        }

        return data;
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

//    public static <T> String[][] asTable(final Iterable<T> items) {
//        final List<List<String>> rows = new ArrayList<>();
//
//        int columns = 0;
//        List<String> keys = null;
//        for (final T item : items) {
//            final List<String> row = new ArrayList<>();
//
//            final ObjectMap map = new ObjectMap(item);
//
//            keys = new ArrayList<>(map.keySet());
//            Collections.sort(keys);
//
//            columns = Math.max(columns, keys.size());
//
//            for (final String field : keys) {
//                row.add(resolve(map, field));
//            }
//
//            rows.add(row);
//        }
//
//        // sort the rows
////        Collections.sort(rows,(a, b) -> {
////            a.
////        });
//
//        final String[][] data = new String[rows.size() + 1][columns];
//        int rowCount = 0;
//
//        // Add the headers
//        data[rowCount++] = keys.toArray(new String[columns]);
//
//        for (final List<String> row : rows) {
//            data[rowCount++] = row.toArray(new String[columns]);
//        }
//
//        return data;
//    }

    private static Item resolve(final ObjectMap map, final String field) {
        final List<String> parts = new ArrayList<>(Arrays.asList(field.split("\\.")));

        if (parts.size() > 1) {
            final String part = parts.remove(0);
            final Object object = map.get(part);
            return resolve(new ObjectMap(object), Join.join(".", parts));
        }

        final Object o = map.get(field);
        return new Item(o);
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
