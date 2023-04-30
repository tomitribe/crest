/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableOutput implements PrintOutput {
    private final Data data;
    private final Options options;

    public TableOutput(final Iterable<?> iterable, final Options options) {
        Objects.requireNonNull(iterable);
        Objects.requireNonNull(options);
        this.data = Formatting.asTable(iterable, options);
        this.options = options;
    }

    @Override
    public void write(final PrintStream out) throws IOException {

        final int guess = Screen.guessWidth();
        final int width = guess > 0 ? guess : 150;

        final Border.Builder builder = getBuilder();
        final Table table = new Table(data, builder.build(), width);

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Options options = new Options();
        private Iterable<?> iterable;

        public Builder() {
            options.setBorder(org.tomitribe.crest.api.table.Border.asciiCompact);
            options.setHeader(true);
        }

        public Builder data(final Stream<?> data) {
            this.iterable = data.collect(Collectors.toList());
            return this;
        }

        public Builder data(final Iterable<?> data) {
            this.iterable = data;
            return this;
        }

        public Builder data(final Object[] data) {
            this.iterable = Arrays.asList(data);
            return this;
        }

        public Builder sort(final String sort) {
            options.setSort(sort);
            return this;
        }

        public Builder border(final org.tomitribe.crest.api.table.Border border) {
            options.setBorder(border);
            return this;
        }

        public Builder header(final Boolean header) {
            options.setHeader(header);
            return this;
        }

        public Builder fields(final String fields) {
            options.setFields(fields);
            return this;
        }

        public TableOutput build() {
            return new TableOutput(iterable, options);
        }
    }
}
