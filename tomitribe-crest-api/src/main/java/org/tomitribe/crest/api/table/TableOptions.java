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
    private Boolean header;
    private String fields;
    private String sort;
    private Border border;

    public TableOptions(@Option("table-border") final Border border,
                        @Option("table-header") final Boolean header,
                        @Option("table-sort") final String sort,
                        @Option("table-fields") final String fields,
                        @Option("tsv") final Boolean tsv,
                        @Option("csv") final Boolean csv
    ) {
        this.header = header;
        this.fields = fields;
        this.sort = sort;
        this.border = border;

        if (tsv != null && tsv) {
            this.border = Border.tsv;
        }

        if (csv != null && csv) {
            this.border = Border.csv;
        }
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

    public void setHeader(final Boolean header) {
        this.header = header;
    }

    public void setFields(final String fields) {
        this.fields = fields;
    }

    public void setSort(final String sort) {
        this.sort = sort;
    }

    public void setBorder(final Border border) {
        this.border = border;
    }
}
