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

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.table.Table;

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

}
