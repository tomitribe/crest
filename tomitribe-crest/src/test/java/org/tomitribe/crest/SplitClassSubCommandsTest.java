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
package org.tomitribe.crest;

import org.junit.Test;
import org.tomitribe.crest.api.Command;

import static org.junit.Assert.assertEquals;

/**
 * Test that subcommands under the same @Command group name can be
 * split across multiple classes and still be accessible.
 */
public class SplitClassSubCommandsTest {

    @Test
    public void authLogin() throws Exception {
        final Main main = Main.builder()
                .command(LoginCommand.class)
                .command(RefreshCommand.class)
                .build();

        assertEquals("login:Foo", main.exec("auth", "login", "Foo"));
    }

    @Test
    public void authRefresh() throws Exception {
        final Main main = Main.builder()
                .command(LoginCommand.class)
                .command(RefreshCommand.class)
                .build();

        assertEquals("refresh:Foo", main.exec("auth", "refresh", "Foo"));
    }

    @Command("auth")
    public static class LoginCommand {

        @Command("login")
        public String login(final String name) {
            return "login:" + name;
        }
    }

    @Command("auth")
    public static class RefreshCommand {

        @Command("refresh")
        public String refresh(final String name) {
            return "refresh:" + name;
        }
    }
}
