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

import org.tomitribe.util.Join;

import java.util.List;
import java.util.regex.Pattern;

public class Lines {

    private static final Pattern separator = Pattern.compile(" *" + System.lineSeparator());

    public static String[] split(final String string) {
        return separator.split(string);
    }

    public static String join(final List<Object> list) {
        return Join.join(System.lineSeparator(), list);
    }

    public static String join(final Object... list) {
        return Join.join(System.lineSeparator(), list);
    }
}
