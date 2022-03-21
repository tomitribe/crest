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

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Table;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TableOptionsTest {

    @Test
    public void from() throws Exception {
        final Table table = Data.class.getMethod("annotationOnly").getAnnotation(Table.class);
        final TableInterceptor.Options options = TableInterceptor.Options.from(table);

        assertEquals(Table.Orientation.vertical, options.orientation());
        assertEquals(Table.Border.asciiDots, options.border());
        assertEquals("red green blue", options.fields());
        assertEquals(Table.Format.csv, options.format());
        assertEquals("green", options.sort());
    }

    @Test
    public void fromParameters() throws Exception {

        final Main main = new Main(Data.class, CrestContextInterceptor.class);
        main.exec("optionsOnly",
                "--table-border=mysqlStyle",
                "--table-orientation=vertical",
                "--table-sort=red",
                "--table-fields=blue red",
                "--table-format=tsv",
                "--unrelated=opt"
        );

        final CrestContext crestContext = CrestContextInterceptor.context;

        final TableInterceptor.Options options = TableInterceptor.Options.from(crestContext.getParameterMetadata(), crestContext.getParameters());

        assertEquals(Table.Orientation.vertical, options.orientation());
        assertEquals(Table.Border.mysqlStyle, options.border());
        assertEquals("blue red", options.fields());
        assertEquals(Table.Format.tsv, options.format());
        assertEquals("red", options.sort());
    }

    @Test
    public void fromParametersPartial() throws Exception {

        final Main main = new Main(Data.class, CrestContextInterceptor.class);
        main.exec("optionsOnly",
                "--table-sort=red",
                "--table-fields=blue red",
                "--unrelated=opt"
        );

        final CrestContext crestContext = CrestContextInterceptor.context;

        final TableInterceptor.Options options = TableInterceptor.Options.from(crestContext.getParameterMetadata(), crestContext.getParameters());

        assertNull(options.orientation());
        assertNull(options.border());
        assertEquals("blue red", options.fields());
        assertNull(options.format());
        assertEquals("red", options.sort());
    }

    @Test
    public void fromCrestContext() throws Exception {

        final Main main = new Main(Data.class, CrestContextInterceptor.class);
        main.exec("both",
                "--table-sort=red",
                "--table-fields=blue red",
                "--unrelated=opt"
        );

        final CrestContext crestContext = CrestContextInterceptor.context;

        final TableInterceptor.Options options = TableInterceptor.Options.from(crestContext);

        assertEquals(Table.Orientation.vertical, options.orientation());
        assertEquals(Table.Border.asciiDots, options.border());
        assertEquals("blue red", options.fields());
        assertEquals(Table.Format.csv, options.format());
        assertEquals("red", options.sort());
    }

    @Test
    public void override() throws Exception {
        final Table table = Data.class.getMethod("annotationOnly").getAnnotation(Table.class);
        final TableInterceptor.Options options = TableInterceptor.Options.from(table);

        final TableInterceptor.Options overrides = new TableInterceptor.Options();
        overrides.setOrientation(Table.Orientation.horizontal);
        overrides.setFields("red green");

        final TableInterceptor.Options actual = options.override(overrides);
        assertEquals(Table.Orientation.horizontal, actual.orientation());
        assertEquals(Table.Border.asciiDots, actual.border());
        assertEquals("red green", actual.fields());
        assertEquals(Table.Format.csv, actual.format());
        assertEquals("green", actual.sort());
    }

    public static class Data {
        @Table(fields = "red green blue",
                format = Table.Format.csv,
                border = Table.Border.asciiDots,
                orientation = Table.Orientation.vertical,
                sort = "green"
        )
        @Command
        public void annotationOnly() {
        }


        @Command(interceptedBy = CrestContextInterceptor.class)
        public void optionsOnly(
                @Option("table-border") final Table.Border border,
                @Option("table-orientation") final Table.Orientation orientation,
                @Option("table-sort") final String sort,
                @Option("table-fields") final String fields,
                @Option("table-format") final Table.Format format,
                @Option("unrelated") final String unrelated
        ) {
        }

        @Table(fields = "red green blue",
                format = Table.Format.csv,
                border = Table.Border.asciiDots,
                orientation = Table.Orientation.vertical,
                sort = "green"
        )
        @Command(interceptedBy = CrestContextInterceptor.class)
        public void both(
                @Option("table-border") final Table.Border border,
                @Option("table-orientation") final Table.Orientation orientation,
                @Option("table-sort") final String sort,
                @Option("table-fields") final String fields,
                @Option("table-format") final Table.Format format,
                @Option("unrelated") final String unrelated
        ) {
        }

    }

    public static class CrestContextInterceptor {

        public static CrestContext context;

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            context = crestContext;
            return crestContext.proceed();
        }
    }
}