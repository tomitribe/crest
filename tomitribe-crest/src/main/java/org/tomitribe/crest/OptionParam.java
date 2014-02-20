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

import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Join;
import org.tomitribe.util.reflect.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OptionParam extends Param {

    public static final String LIST_SEPARATOR = "\u0000";
    public static final String LIST_TYPE = "\uFFFF￿\uFFFF￿";
    private final String name;
    private final String defaultValue;

    public OptionParam(final Parameter parameter) {
        super(parameter);

        this.name = getAnnotation(Option.class).value();
        this.defaultValue = initDefault();
    }

    public static List<String> getSeparatedValues(final String value) {
        if (value == null) return Collections.EMPTY_LIST;
        final List<String> split = new ArrayList<String>(Arrays.asList(value.split(LIST_TYPE + "|" + LIST_SEPARATOR)));
        if (split.size() > 0) split.remove(0);
        return split;
    }

    private String initDefault() {
        final Default def = getAnnotation(Default.class);

        if (def != null) {

            if (isListable()) {

                return LIST_TYPE + normalize(def);

            } else {

                return def.value();

            }

        } else if (isListable()) {

            return LIST_TYPE;

        } else if (getType().isPrimitive()) {

            final Class<?> type = getType();
            if (boolean.class.equals(type)) return "false";
            else if (byte.class.equals(type)) return "0";
            else if (char.class.equals(type)) return "\u0000";
            else if (short.class.equals(type)) return "0";
            else if (int.class.equals(type)) return "0";
            else if (long.class.equals(type)) return "0";
            else if (float.class.equals(type)) return "0";
            else if (double.class.equals(type)) return "0";
            else return null;

        } else {

            return null;
        }
    }

    public String normalize(final Default def) {
        final String value = def.value();

        if (value.contains(LIST_SEPARATOR)) {
            return value;
        }

        if (value.contains("\t")) {
            final String[] split = value.split("\t");
            return Join.join(LIST_SEPARATOR, split);
        }

        if (value.contains(",")) {
            final String[] split = value.split(",");
            return Join.join(LIST_SEPARATOR, split);
        }

        return value;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getDefaultValues() {
        return getSeparatedValues(defaultValue);
    }

    @Override
    public String toString() {
        return "Option{" +
                "name='" + name + '\'' +
                ", default='" + defaultValue + '\'' +
                '}';
    }
}
