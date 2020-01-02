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
package org.tomitribe.crest.cmds.processors;

import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class Item {

    private final String flag;
    private final List<String> note = new LinkedList<>();
    private final String description;
    private final OptionParam param;

    public Item(final String flag, final String description, final OptionParam param, final List<String> notes) {
        this.flag = flag;
        this.description = description;
        this.param = param;
        this.note.addAll(notes);
    }

    Item(final OptionParam p, final String description) {
        this.description = description;
        this.param = p;
        final String prefix = p.getName().length() > 1 ? "--" : "-";

        final List<String> alias = new ArrayList<>();

        final Option option = p.getAnnotation(Option.class);
        for (int i = 1; i < option.value().length; i++) {
            final String aliasName = option.value()[i];
            alias.add(aliasName);
        }

        final boolean hasAlias = !alias.isEmpty();
        final Class<?> type = p.getType();

        String defaultValue = p.getDefaultValue();

        final String name = p.getName();
        if (boolean.class.equals(type) || (Boolean.class.equals(type) && defaultValue != null)) {

            if ("true".equals(defaultValue)) {
                this.flag = hasAlias ? Join.join(", ", "--no-" + name, getAlias(alias, false, true)) : "--no-" + name;
            } else {
                final String optName = name.startsWith("-") ? name : prefix + name;
                this.flag = hasAlias ? Join.join(", ", optName, getAlias(alias, true, false)) : optName;
            }

            defaultValue = null;

        } else {
            final String optName = name.startsWith("-") ? name : prefix + name;
            this.flag = hasAlias ? String.format("%s, %s=<%s>", optName, getAlias(alias, true, false), p.getDisplayType())
                    : String.format("%s=<%s>", optName, p.getDisplayType());
        }

        if (defaultValue != null) {

            if (p.isListable()) {
                final List<String> defaultValues = p.getDefaultValues();

                if (!defaultValues.isEmpty()) {
                    this.getNote().add(String.format("default: %s", Join.join(", ", defaultValues)));
                }

            } else {

                this.getNote().add(String.format("default: %s", p.getDefaultValue()));

            }
        }

        if (Enum.class.isAssignableFrom(type)) {
            final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            final EnumSet<? extends Enum> enums = EnumSet.allOf(enumType);
            final String join = Join.join(", ", enums);
            this.getNote().add(String.format("enum: %s", join));
        }

    }

    public OptionParam getParam() {
        return param;
    }

    private String getAlias(List<String> aliasList, boolean withDemiliter, boolean isBooleanValue) {
        StringBuilder sb = new StringBuilder();
        for (String alias : aliasList) {
            if (isBooleanValue) {
                sb.append(", --no-" + alias);
            } else {
                if (alias.length() > 1) {
                    sb.append(withDemiliter ? ", --" + alias : alias);
                } else {
                    sb.append(withDemiliter ? ", -" + alias : alias);
                }
            }
        }
        return sb.length() > 0 ? sb.toString().replaceFirst(", ", "") : "";
    }

    public String getFlag() {
        return flag;
    }

    public List<String> getNote() {
        return note;
    }

    public String getDescription() {
        return description;
    }
}
