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
import org.tomitribe.crest.api.Option;

import static org.junit.Assert.assertEquals;

/**
 * Two classes define the same @Command subcommand name under the same
 * @Command group with different signatures.  They should merge into
 * an OverloadedCmdMethod, with the correct overload selected based
 * on the arguments provided.
 */
public class SplitClassOverloadedSubCommandsTest {

    @Test
    public void tokenCreate() throws Exception {
        final Main main = Main.builder()
                .command(TokenCreateCommand.class)
                .command(TokenRevokeCommand.class)
                .build();

        assertEquals("token:create:MyToken", main.exec("auth", "token", "--create", "MyToken"));
    }

    @Test
    public void tokenRevoke() throws Exception {
        final Main main = Main.builder()
                .command(TokenCreateCommand.class)
                .command(TokenRevokeCommand.class)
                .build();

        assertEquals("token:revoke:abc123", main.exec("auth", "token", "--revoke", "abc123"));
    }

    @Command("auth")
    public static class TokenCreateCommand {

        @Command("token")
        public String create(@Option("create") final boolean create, final String name) {
            return "token:create:" + name;
        }
    }

    @Command("auth")
    public static class TokenRevokeCommand {

        @Command("token")
        public String revoke(@Option("revoke") final boolean revoke, final String id) {
            return "token:revoke:" + id;
        }
    }
}
