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
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.crest.term.Screen;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Table
public class TableInterceptor {

    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        final Object result = crestContext.proceed();

        final Options options = Options.from(crestContext);

        if (result instanceof Iterable) {
            final Iterable<?> list = (Iterable<?>) result;
            return new TableOutput(list, options);
        }

        if (result instanceof Stream) {
            final Stream<?> stream = (Stream<?>) result;
            final List<?> list = stream.collect(Collectors.toList());
            return new TableOutput(list, options);
        }

        if (result != null && result.getClass().isArray()) {
            final List<Object> list = Arrays.asList((Object[]) result);
            return new TableOutput(list, options);
        }

        return result;
    }

    public static class TableOutput implements PrintOutput {
        private final Data data;
        private final Options options;

        public TableOutput(final Iterable<?> iterable, final Options options) {
            this.data = Formatting.asTable(iterable, options);
            this.options = options;
        }

        @Override
        public void write(final PrintStream out) throws IOException {

            final int guess = Screen.guessWidth();
            final int width = guess > 0 ? guess : 150;

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

}
