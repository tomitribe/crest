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
package org.tomitribe.crest;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @version $Revision$ $Date$
*/
public class Substitution {

    private static final Pattern PATTERN = Pattern.compile("(\\$\\{)([\\w.]+)(})");

    public static String format(Target target, Method method, String input, DefaultsContext df) {
        return format(target, method, input, df, new HashSet<String>());
    }

    private static String format(Target target, Method method,  String input, DefaultsContext df, Set<String> seen) {
        if (!seen.add(input)) throw new IllegalStateException("Circular reference in " + input);

        Matcher matcher = PATTERN.matcher(input);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(2);
            String value = df.find(target, method, key);
            if (value != null) {
                value = format(target, method, value, df, seen);
                matcher.appendReplacement(buf, value);
            }
        }
        matcher.appendTail(buf);
        return buf.toString();
    }
}
