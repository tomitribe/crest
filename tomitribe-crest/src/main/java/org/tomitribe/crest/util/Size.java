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
package org.tomitribe.crest.util;

import org.tomitribe.crest.util.editor.Editors;

import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class Size {

    private long size;
    private SizeUnit unit;

    public Size() {
    }

    public Size(final long size, final SizeUnit unit) {
        this.size = size;
        this.unit = unit;
    }

    public Size(final String string) {
        this(string, null);
    }

    public Size(final String string, final SizeUnit defaultUnit) {
        final String[] strings = string.split(",| and ");

        Size total = new Size();

        for (String s : strings) {
            final Size part = new Size();
            s = s.trim();

            final StringBuilder t = new StringBuilder();
            final StringBuilder u = new StringBuilder();

            int i = 0;

            // get the number
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isDigit(c) || i == 0 && c == '-' || i > 0 && c == '.') {
                    t.append(c);
                } else {
                    break;
                }
            }

            if (t.length() == 0) {
                invalidFormat(s);
            }

            // skip whitespace
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                } else {
                    break;
                }
            }

            // get time unit text part
            for (; i < s.length(); i++) {
                final char c = s.charAt(i);
                if (Character.isLetter(c)) {
                    u.append(c);
                } else {
                    invalidFormat(s);
                }
            }


            part.unit = parseUnit(u.toString());

            if (part.unit == null) {
                part.unit = defaultUnit;
            }

            final String size = t.toString();
            if (size.contains(".")) {
                if (part.unit == null)
                    throw new IllegalArgumentException("unit must be specified with floating point numbers");
                final double d = Double.parseDouble(size);
                final long bytes = part.unit.toBytes(1);
                part.size = (long) (bytes * d);
                part.unit = SizeUnit.BYTES;
            } else {
                part.size = Integer.parseInt(size);
            }

            total = total.add(part);
        }

        this.size = total.size;
        this.unit = total.unit;
    }

    public long getSize() {
        return size;
    }

    public long getSize(final SizeUnit unit) {
        return unit.convert(this.size, this.unit);
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public SizeUnit getUnit() {
        return unit;
    }

    public void setUnit(final SizeUnit unit) {
        this.unit = unit;
    }

    private static class Normalize {
        private long a;
        private long b;
        private SizeUnit base;

        private Normalize(final Size a, final Size b) {
            this.base = lowest(a, b);
            this.a = a.unit == null ? a.size : base.convert(a.size, a.unit);
            this.b = b.unit == null ? b.size : base.convert(b.size, b.unit);
        }

        private static SizeUnit lowest(final Size a, final Size b) {
            if (a.unit == null) return b.unit;
            if (b.unit == null) return a.unit;
            if (a.size == 0) return b.unit;
            if (b.size == 0) return a.unit;
            return SizeUnit.values()[Math.min(a.unit.ordinal(), b.unit.ordinal())];
        }
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Size that = (Size) o;

        final Normalize n = new Normalize(this, that);
        return n.a == n.b;
    }

    public Size add(final Size that) {
        final Normalize n = new Normalize(this, that);
        return new Size(n.a + n.b, n.base);
    }

    public Size subtract(final Size that) {
        final Normalize n = new Normalize(this, that);
        return new Size(n.a - n.b, n.base);
    }

    public static Size parse(final String text) {
        return new Size(text);
    }

    private static void invalidFormat(final String text) {
        throw new IllegalArgumentException("Illegal size format: '" + text + "'.  Valid examples are '10kb' or '10 kilobytes'.");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(size);
        if (unit != null) {
            sb.append(" ");
            sb.append(unit);
        }
        return sb.toString();
    }

    private static SizeUnit parseUnit(final String u) {
        if (u.length() == 0) return null;

        if (u.equalsIgnoreCase("BYTES")) return SizeUnit.BYTES;
        if (u.equalsIgnoreCase("BYTE")) return SizeUnit.BYTES;
        if (u.equalsIgnoreCase("B")) return SizeUnit.BYTES;

        if (u.equalsIgnoreCase("KILOBYTES")) return SizeUnit.KILOBYTES;
        if (u.equalsIgnoreCase("KILOBYTE")) return SizeUnit.KILOBYTES;
        if (u.equalsIgnoreCase("KB")) return SizeUnit.KILOBYTES;
        if (u.equalsIgnoreCase("K")) return SizeUnit.KILOBYTES;

        if (u.equalsIgnoreCase("MEGABYTES")) return SizeUnit.MEGABYTES;
        if (u.equalsIgnoreCase("MEGABYTE")) return SizeUnit.MEGABYTES;
        if (u.equalsIgnoreCase("MB")) return SizeUnit.MEGABYTES;
        if (u.equalsIgnoreCase("M")) return SizeUnit.MEGABYTES;

        if (u.equalsIgnoreCase("GIGABYTES")) return SizeUnit.GIGABYTES;
        if (u.equalsIgnoreCase("GIGABYTE")) return SizeUnit.GIGABYTES;
        if (u.equalsIgnoreCase("GB")) return SizeUnit.GIGABYTES;
        if (u.equalsIgnoreCase("G")) return SizeUnit.GIGABYTES;

        if (u.equalsIgnoreCase("TERABYTES")) return SizeUnit.TERABYTES;
        if (u.equalsIgnoreCase("TERABYTE")) return SizeUnit.TERABYTES;
        if (u.equalsIgnoreCase("TB")) return SizeUnit.TERABYTES;
        if (u.equalsIgnoreCase("T")) return SizeUnit.TERABYTES;


        throw new IllegalArgumentException("Unknown size unit '" + u + "'.  Supported units " + Join.join(", ", lowercase(SizeUnit.values())));
    }

    private static List<String> lowercase(final Enum... units) {
        final List<String> list = new ArrayList<String>();
        for (final Enum unit : units) {
            list.add(unit.name().toLowerCase());
        }
        return list;
    }

    public static class SizeEditor extends java.beans.PropertyEditorSupport {
        public void setAsText(final String text) {
            final Size d = Size.parse(text);
            setValue(d);
        }
    }

    static {
        PropertyEditorManager.registerEditor(Size.class, SizeEditor.class);
        Editors.get(Size.class);
    }
}
