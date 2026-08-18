/*
 * Copyright 2026 Tomitribe and community
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
package org.tomitribe.crest.interceptor;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.util.PrintString;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Options declared by interceptors render in the command's help and
 * man-page output exactly like options the command declares itself:
 * aliases, defaults and descriptions included, shared options once.
 */
public class InterceptorOptionsHelpTest {

    @Test
    public void help() throws Exception {

        final TestMain main = new TestMain(Foo.class);

        final PrintString out = new PrintString();
        main.cmd("list").help(out);

        assertEquals("\n" +
                "Usage: list [options]\n" +
                "\n" +
                "Options:\n" +
                "  --region=<String>          default: us-east\n" +
                "  --include, -i=<String>     Only rows matching the pattern\n" +
                "                             (default: .*)\n" +
                "  --exclude=<String>         \n", out.toString().replace("\r\n", "\n"));
    }

    /**
     * A command with no expanded javadoc renders its man page from the
     * same source as help, contributed options included
     */
    @Test
    public void manual() throws Exception {

        final TestMain main = new TestMain(Foo.class);

        final PrintString help = new PrintString();
        main.cmd("list").help(help);

        final PrintString manual = new PrintString();
        main.cmd("list").manual(manual);

        assertEquals(help.toString(), manual.toString());
        assertTrue(manual.toString().contains("--include"));
    }

    public static class Foo {

        @Command(interceptedBy = FilterInterceptor.class)
        public static String list(@Option("region") @Default("us-east") final String region) {
            return region;
        }
    }

    public static class FilterInterceptor {

        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option(value = {"include", "i"}, description = "Only rows matching the pattern")
                                @Default(".*") final String include,
                                final Excludes excludes) {
            return crestContext.proceed();
        }
    }

    @Options
    public static class Excludes {

        public Excludes(@Option("exclude") final String exclude) {
        }
    }

    public static class TestMain extends Main {

        public TestMain(final Class<?>... classes) {
            super(classes);
        }

        public Cmd cmd(final String name) {
            return commands.get(name);
        }
    }
}
