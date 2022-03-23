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
package org.tomitribe.crest.api.table;

import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;

/**
 * <p>
 * Adding TableOptions to the argument list of your @Command method annotated with @Table
 * allows users to override the @Table settings via passing the following command-line arguments
 *</p>
 * <pre>
 *     --table-border=asciiCompact
 *     --no-table-header
 *     --table-sort="lastName address.zipCode"
 *     --table-fields="firstName lastName address.zipCode"
 * </pre>
 */
@Options
public class TableOptions {
    private final Boolean header;
    private final String fields;
    private final String sort;
    private final Border border;

    public TableOptions(@Option("table-border") final Border border,
                        @Option("table-header") final Boolean header,
                        @Option("table-sort") final String sort,
                        @Option("table-fields") final String fields
    ) {
        this.header = header;
        this.fields = fields;
        this.sort = sort;
        this.border = border;
    }

    public Boolean isHeader() {
        return header;
    }

    public String getFields() {
        return fields;
    }

    public String getSort() {
        return sort;
    }

    public Border getBorder() {
        return border;
    }
}
