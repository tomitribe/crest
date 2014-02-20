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

/**
 * @version $Revision$ $Date$
 */
public class Bytes implements Comparable<Bytes> {
    private long b;
    private long kb;
    private long mb;
    private long gb;

    public void add(final long bytes) {
        compact();

        b += bytes;
    }

    public long get() {
        compact();
        return mb;
    }

    private void compact() {
        if (b > 1024) {
            kb += b / 1024;
            b = b % 1024;
        }
        if (kb > 1024) {
            mb += kb / 1024;
            kb = kb % 1024;
        }
        if (mb > 1024) {
            gb += mb / 1024;
            mb = mb % 1024;
        }
    }

    @Override
    public int compareTo(final Bytes o) {
        compact();
        o.compact();

        if (this.gb != o.gb) return (this.gb > o.gb) ? 1 : -1;
        if (this.mb != o.mb) return (this.mb > o.mb) ? 1 : -1;
        if (this.kb != o.kb) return (this.kb > o.kb) ? 1 : -1;
        if (this.b != o.b) return (this.b > o.b) ? 1 : -1;

        return 0;
    }

    @Override
    public String toString() {
        compact();
        if (gb > 0) {
            final double n = gb + (mb * 0.000976562);
            return String.format("%,.2fgb", n);
        }
        if (mb > 0) {
            final double n = mb + (kb * 0.000976562);
            return String.format("%,.2fmb", n);
        }
        if (kb > 0) {
            final double n = kb + (b * 0.000976562);
            return String.format("%,.2fkb", n);
        }
        return b + "";
    }
}
