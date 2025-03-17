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
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.util.PrintString;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class OverloadedCmdHelpTest {

    @Test
    public void test() {
        final PrintString out = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(AccountCommands.class)
                .name("ops")
                .version("23.5.6")
                .out(out)
                .exit(exit::set)
                .build();

        main.run("help", "account", "zendesk");

        assertEquals(
                String.format("\n" +
                        "Usage: account zendesk [options1]\n" +
                        "       account zendesk [options2] Name\n" +
                        "       account zendesk [options3] Name\n" +
                        "       account zendesk [options4] Name\n" +
                        "\n" +
                        "Options1: \n" +
                        "  --env=<Env>              default: prod\n" +
                        "                           (enum: prod, dev)\n" +
                        "  --list, -l               \n" +
                        "\n" +
                        "Options2: \n" +
                        "  --create, -c             \n" +
                        "  --env=<Env>              default: prod\n" +
                        "                           (enum: prod, dev)\n" +
                        "  --host=<URI>             \n" +
                        "  --token=<String>         \n" +
                        "  --username=<String>      \n" +
                        "\n" +
                        "Options3: \n" +
                        "  --env=<Env>              default: prod\n" +
                        "                           (enum: prod, dev)\n" +
                        "  --host=<URI>             \n" +
                        "  --token=<String>         \n" +
                        "  --update, -u             \n" +
                        "  --username=<String>      \n" +
                        "\n" +
                        "Options4: \n" +
                        "  --delete, -d             \n" +
                        "  --env=<Env>              default: prod\n" +
                        "                           (enum: prod, dev)\n" +
                        "\n" +
                        "ops 23.5.6\n"),
                out.toString());
    }

    @Test
    public void testDashHonSingleCommand() {
        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(SingleAccountCommands.class)
                .name("ops")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        main.run("account", "zendesk", "--help");
        assertEquals(
                String.format("Unknown options: --help%n" +
                        "%n" +
                        "Usage: account zendesk [options]%n" +
                        "%n" +
                        "Options: %n" +
                        "  --env=<Env>              default: prod%n" +
                        "                           (enum: prod, dev)%n" +
                        "  --list, -l               %n" +
                        "%n" +
                        "ops 23.5.6%n"),
                err.toString());

        assertEquals("", out.toString());
    }

    @Test
    public void testDashHonOverloadedCommand() {
        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(AccountCommands.class)
                .name("ops")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        main.run("account", "zendesk", "--help");
        assertEquals(
                String.format("Unknown options: --help%n" +
                        "%n" +
                        "Usage: account zendesk [options1]%n" +
                        "       account zendesk [options2] Name%n" +
                        "       account zendesk [options3] Name%n" +
                        "       account zendesk [options4] Name%n" +
                        "%n" +
                        "Options1: %n" +
                        "  --env=<Env>              default: prod%n" +
                        "                           (enum: prod, dev)%n" +
                        "  --list, -l               %n" +
                        "%n" +
                        "Options2: %n" +
                        "  --create, -c             %n" +
                        "  --env=<Env>              default: prod%n" +
                        "                           (enum: prod, dev)%n" +
                        "  --host=<URI>             %n" +
                        "  --token=<String>         %n" +
                        "  --username=<String>      %n" +
                        "%n" +
                        "Options3: %n" +
                        "  --env=<Env>              default: prod%n" +
                        "                           (enum: prod, dev)%n" +
                        "  --host=<URI>             %n" +
                        "  --token=<String>         %n" +
                        "  --update, -u             %n" +
                        "  --username=<String>      %n" +
                        "%n" +
                        "Options4: %n" +
                        "  --delete, -d             %n" +
                        "  --env=<Env>              default: prod%n" +
                        "                           (enum: prod, dev)%n" +
                        "%n" +
                        "ops 23.5.6%n"),
                err.toString());

        assertEquals("", out.toString());
    }

    @Command("account")
    public static class SingleAccountCommands {
        @Command("zendesk")
        @Table(fields = "username token host")
        public Stream<AccountCommands.Zendesk> zendeskList(@Option({"list", "l"}) boolean list, @Option("env") @Default("prod") AccountCommands.Env env) {
            if (list) return Stream.of(
                    new AccountCommands.Zendesk("foo@bar.com", "12341sdfqs", URI.create("http://zendesk.com/foo/")),
                    new AccountCommands.Zendesk("orange@red.com", "asdfasd234", URI.create("http://dev.zendesk.com/foo/"))
            );

            return Stream.of();
        }
    }

    @Command("account")
    public static class AccountCommands {

        @Command("zendesk")
        @Table(fields = "username token host")
        public Stream<Zendesk> zendeskList(@Option({"list", "l"}) boolean list, @Option("env") @Default("prod") Env env) {
            if (list) return Stream.of(
                    new Zendesk("foo@bar.com", "12341sdfqs", URI.create("http://zendesk.com/foo/")),
                    new Zendesk("orange@red.com", "asdfasd234", URI.create("http://dev.zendesk.com/foo/"))
            );

            return Stream.of();
        }

        @Command("zendesk")
        public String zendeskUpdate(@Required @Option({"update", "u"}) final boolean update, final Name name, final Zendesk zendesk, @Option("env") @Default("prod") Env env) {
            return "Updating " + zendesk;
        }

        @Command("zendesk")
        public String zendeskCreate(@Option({"create", "c"}) boolean update, final Name name, final Zendesk zendesk, @Option("env") @Default("prod") Env env) {
            if (update) {
                return "Updating " + zendesk;
            } else {
                return "Creating " + zendesk;
            }
        }

        @Command("zendesk")
        public String zendesk(@Option({"delete", "d"}) boolean update, final Name name, @Option("env") @Default("prod") Env env) {
            if (update) {
                return "Deleting " + name;
            }
            return "No action taken";
        }

        public enum Env {
            prod,
            dev
        }

        @Options
        public static class Zendesk {

            private final String username;
            private final String token;
            private final URI host;

            public Zendesk(@Option("username") String username, @Option("token") String token, @Option("host") URI host) {
                this.username = username;
                this.token = token;
                this.host = host;
            }
        }
    }

    public static class Name {
        private final String name;

        public Name(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
