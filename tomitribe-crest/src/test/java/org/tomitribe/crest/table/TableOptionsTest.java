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
import org.tomitribe.crest.api.table.Border;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TableOptionsTest {

    @Test
    public void defaults() throws Exception {
        final Table table = Data.class.getMethod("defaults").getAnnotation(Table.class);
        final Options options = Options.from(table);

        assertEquals(Border.asciiCompact, options.border());
        assertEquals("", options.fields());
        assertEquals("", options.sort());
        assertTrue(options.header());
//        assertEquals(Table.Format.csv, options.format());
    }

    @Test
    public void from() throws Exception {
        final Table table = Data.class.getMethod("annotationOnly").getAnnotation(Table.class);
        final Options options = Options.from(table);

        assertEquals(Border.asciiDots, options.border());
        assertEquals("red green blue", options.fields());
        assertTrue(options.header());
//        assertEquals(Table.Format.csv, options.format());
        assertEquals("green", options.sort());
    }

    @Test
    public void fromParameters() throws Exception {

        final Main main = new Main(Data.class, CrestContextInterceptor.class);
        main.exec("optionsOnly",
                "--table-border=mysqlStyle",
                "--table-sort=red",
                "--table-header=true",
                "--table-fields=blue red",
//                "--table-format=tsv",
                "--unrelated=opt"
        );

        final CrestContext crestContext = CrestContextInterceptor.context;

        final Options options = Options.from(crestContext.getParameterMetadata(), crestContext.getParameters());

        assertEquals(Border.mysqlStyle, options.border());
        assertEquals("blue red", options.fields());
        assertEquals("red", options.sort());
        assertTrue(options.header());
//        assertEquals(Table.Format.tsv, options.format());
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

        final Options options = Options.from(crestContext.getParameterMetadata(), crestContext.getParameters());

        assertNull(options.border());
        assertEquals("blue red", options.fields());
        assertNull(options.isHeader());
//        assertNull(options.format());
        assertEquals("red", options.sort());
    }

    @Test
    public void fromCrestContext() throws Exception {

        final Main main = new Main(Data.class, CrestContextInterceptor.class);
        main.exec("both",
                "--table-sort=red",
                "--no-table-header",
                "--table-fields=blue red",
                "--unrelated=opt"
        );

        final CrestContext crestContext = CrestContextInterceptor.context;

        final Options options = Options.from(crestContext);

        assertEquals(Border.asciiDots, options.border());
        assertEquals("blue red", options.fields());
        assertFalse(options.header());
//        assertEquals(Table.Format.csv, options.format());
        assertEquals("red", options.sort());
    }

    @Test
    public void override() throws Exception {
        final Table table = Data.class.getMethod("annotationOnly").getAnnotation(Table.class);
        final Options options = Options.from(table);

        final Options overrides = new Options();
        overrides.setFields("red green");

        final Options actual = options.override(overrides);
        assertEquals(Border.asciiDots, actual.border());
        assertEquals("red green", actual.fields());
        assertTrue(actual.header());
//        assertEquals(Table.Format.csv, actual.format());
        assertEquals("green", actual.sort());
    }

    public static class Data {
        @Table
        @Command
        public void defaults() {
        }

        @Table(fields = "red green blue",
//                format = Table.Format.csv,
                border = Border.asciiDots,
                sort = "green"
        )
        @Command
        public void annotationOnly() {
        }


        @Command(interceptedBy = CrestContextInterceptor.class)
        public void optionsOnly(
                @Option("table-border") final Border border,
                @Option("table-header") final Boolean header,
                @Option("table-sort") final String sort,
                @Option("table-fields") final String fields,
//                @Option("table-format") final Table.Format format,
                @Option("unrelated") final String unrelated
        ) {
        }

        @Table(fields = "red green blue",
//                format = Table.Format.csv,
                border = Border.asciiDots,
                sort = "green"
        )
        @Command(interceptedBy = CrestContextInterceptor.class)
        public void both(
                @Option("table-border") final Border border,
                @Option("table-header") final Boolean header,
                @Option("table-sort") final String sort,
                @Option("table-fields") final String fields,
//                @Option("table-format") final Table.Format format,
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
