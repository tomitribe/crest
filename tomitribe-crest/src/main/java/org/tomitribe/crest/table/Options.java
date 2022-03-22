/*
 * Copyright 2022 Tomitribe and community
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

import org.tomitribe.crest.api.Table;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.util.collect.ObjectMap;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.OPTION;

public class Options implements org.tomitribe.crest.api.Table {
    private Boolean header;
    private String fields;
    private String sort;
    //        private Orientation orientation;
//        private Format format;
    private Border border;

    public Options() {
    }

    public String getFields() {
        return fields;
    }

    public String getSort() {
        return sort;
    }

//        public Orientation getOrientation() {
//            return orientation;
//        }
//
//        public Format getFormat() {
//            return format;
//        }

    public Border getBorder() {
        return border;
    }

    public void setFields(final String fields) {
        this.fields = fields;
    }

    public void setSort(final String sort) {
        this.sort = sort;
    }

//        public void setOrientation(final Orientation orientation) {
//            this.orientation = orientation;
//        }
//
//        public void setFormat(final Format format) {
//            this.format = format;
//        }

    public void setBorder(final Border border) {
        this.border = border;
    }

    public void setHeader(final Boolean header) {
        this.header = header;
    }

    public Boolean isHeader() {
        return header;
    }

    @Override
    public String fields() {
        return fields;
    }

    @Override
    public boolean header() {
        return header;
    }

    @Override
    public String sort() {
        return sort;
    }

//        @Override
//        public Orientation orientation() {
//            return orientation;
//        }
//
//        @Override
//        public Format format() {
//            return format;
//        }

    @Override
    public Border border() {
        return border;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return getClass();
    }

    public static Options from(final org.tomitribe.crest.api.Table table) {
        final Options options = new Options();
        options.setBorder(table.border());
        options.setFields(table.fields());
        options.setHeader(table.header());
//            options.setFormat(table.format());
//            options.setOrientation(table.orientation());
        options.setSort(table.sort());
        return options;
    }

    public Options override(final Options overrides) {
        final Options options = from(this);
        if (overrides.border() != null) options.setBorder(overrides.border());
//            if (overrides.orientation() != null) options.setOrientation(overrides.orientation());
        if (overrides.sort() != null) options.setSort(overrides.sort());
        if (overrides.fields() != null) options.setFields(overrides.fields());
        if (overrides.isHeader() != null) options.setHeader(overrides.isHeader());
//            if (overrides.format() != null) options.setFormat(overrides.format());
        return options;
    }

    public static Options from(final CrestContext crestContext) {
        final Options defaults = from(crestContext.getMethod().getAnnotation(Table.class));
        final Options overrides = from(crestContext.getParameterMetadata(), crestContext.getParameters());
        return defaults.override(overrides);
    }

    public static Options from(final List<ParameterMetadata> parameterMetadata, final List<Object> parameters) {
        final Options options = new Options();
        final ObjectMap map = new ObjectMap(options);
        for (int i = 0; i < parameterMetadata.size(); i++) {
            final ParameterMetadata metadata = parameterMetadata.get(i);
            if (!metadata.getType().equals(OPTION)) continue;
            if (!metadata.getName().startsWith("table-")) continue;

            final String key = metadata.getName().replace("table-", "");
            final Object value = parameters.get(i);
            map.put(key, value);
        }
        return options;
    }
}
