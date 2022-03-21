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

import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.api.Table;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.crest.term.Screen;
import org.tomitribe.util.collect.ObjectMap;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.OPTION;

@Table
public class TableInterceptor {

    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        final Options options = Options.from(crestContext);

        final Object result = crestContext.proceed();

        if (result instanceof Iterable) {
            final Iterable<?> list = (Iterable<?>) result;
            return new TableOutput(list, options);
        }
        return result;
    }

    public static class TableOutput implements PrintOutput {
        private final String[][] data;
        private final Options options;

        public TableOutput(final Iterable<?> iterable, final Options options) {
            this.data = Formatting.asTable(iterable, options.getFields(), options.getSort());
            this.options = options;
        }

        @Override
        public void write(final PrintStream out) throws IOException {

            final int guess = Screen.guessWidth();
            final int width = guess > 0 ? guess : 150;

            final Data data = new Data(this.data, true);
            final Border.Builder builder = getBuilder();
            final org.tomitribe.crest.table.Table table = new org.tomitribe.crest.table.Table(data, builder.build(), width);

            table.format(out);
        }

        private Border.Builder getBuilder() {
            final String borderName = options.getBorder().name();

            final Method method;
            try {
                method = Border.class.getMethod(borderName);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Border not found: " + borderName);
            }

            final Border.Builder builder;
            try {
                builder = (Border.Builder) method.invoke(null);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to access method Border." + borderName);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Unable to call method Border." + borderName, e);
            }
            return builder;
        }
    }

    public static class Options implements Table {
        private String fields;
        private String sort;
        private Orientation orientation;
        private Format format;
        private Border border;

        public String getFields() {
            return fields;
        }

        public String getSort() {
            return sort;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public Format getFormat() {
            return format;
        }

        public Border getBorder() {
            return border;
        }

        public void setFields(final String fields) {
            this.fields = fields;
        }

        public void setSort(final String sort) {
            this.sort = sort;
        }

        public void setOrientation(final Orientation orientation) {
            this.orientation = orientation;
        }

        public void setFormat(final Format format) {
            this.format = format;
        }

        public void setBorder(final Border border) {
            this.border = border;
        }

        @Override
        public String fields() {
            return fields;
        }

        @Override
        public String sort() {
            return sort;
        }

        @Override
        public Orientation orientation() {
            return orientation;
        }

        @Override
        public Format format() {
            return format;
        }

        @Override
        public Border border() {
            return border;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return getClass();
        }

        public static Options from(final Table table) {
            final Options options = new Options();
            options.setBorder(table.border());
            options.setFields(table.fields());
            options.setFormat(table.format());
            options.setOrientation(table.orientation());
            options.setSort(table.sort());
            return options;
        }

        public Options override(final Options overrides) {
            final Options options = from(this);
            if (overrides.border() != null) options.setBorder(overrides.border());
            if (overrides.orientation() != null) options.setOrientation(overrides.orientation());
            if (overrides.sort() != null) options.setSort(overrides.sort());
            if (overrides.fields() != null) options.setFields(overrides.fields());
            if (overrides.format() != null) options.setFormat(overrides.format());
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
}
