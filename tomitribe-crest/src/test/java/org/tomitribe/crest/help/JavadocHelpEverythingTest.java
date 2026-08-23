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
package org.tomitribe.crest.help;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.Priority;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Every javadoc source that can participate in one command's man page,
 * composed into a single canonical document, with every participant
 * exercising the full Document syntax: a heading (an ALL-CAPS line),
 * paragraphs (blank-line separated), a bullet list (leading "- "), and a
 * preformatted block (indented four spaces past the javadoc margin) — in
 * addition to the @param entries documenting each option at its declaring
 * site.
 *
 * The DESCRIPTION inlines each participant's javadoc body in a fixed
 * order: the command method's own, then its @Options beans' in parameter
 * order (Transfer before Connection, though alphabetical order says
 * otherwise) — a bean nesting another bean inlines the nested one right
 * after itself (Compression inside Transfer) — then each interceptor's in
 * the order they run (@Priority order: Throttle at 2 before Audit at 7,
 * the reverse of how the command declares them), each interceptor first
 * composed with its own beans (Limits inside Throttle).
 *
 * Every javadoc body opens by naming its source, so the expected document
 * below reads as a map of what came from where.
 */
public class JavadocHelpEverythingTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Repo.class).main(env, new String[]{"help", "mirror"});

        assertEquals("NAME\n" +
                        "       mirror\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       mirror [options] String String\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The  mirror  command copies every artifact of a remote repository into a local one and\n" +
                        "       keeps the two identical from then on.\n" +
                        "\n" +
                        "MIRROR LIFECYCLE\n" +
                        "       A  first  run  transfers everything; later runs transfer only what changed since, so a\n" +
                        "       mirror stays fresh at a fraction of the original cost.  Every run walks the same three\n" +
                        "       phases:\n" +
                        "\n" +
                        "       o      scan the remote index for artifacts newer than their local copies\n" +
                        "\n" +
                        "       o      transfer the artifacts that changed\n" +
                        "\n" +
                        "       o      verify checksums and commit the updated index\n" +
                        "\n" +
                        "       A typical first mirror of a repository named central:\n" +
                        "\n" +
                        "           mirror --verbose central ./mirrors/central\n" +
                        "\n" +
                        "       The transfer options control how bytes move across the wire once a connection is made.\n" +
                        "\n" +
                        "CHUNK SIZING\n" +
                        "       Artifacts stream in fixed-size chunks and the chunk size is the main throughput lever:\n" +
                        "\n" +
                        "       o      small chunks give frequent progress and cheap retries\n" +
                        "\n" +
                        "       o      large chunks cut per-request overhead on fast, reliable links\n" +
                        "\n" +
                        "       A gigabit link between data centers usually wants:\n" +
                        "\n" +
                        "           mirror --chunk-size=1048576 central ./mirrors/central\n" +
                        "\n" +
                        "       The compression options control how artifacts shrink before they travel.\n" +
                        "\n" +
                        "CODECS\n" +
                        "       The codec trades processor time for bytes on the wire:\n" +
                        "\n" +
                        "       o      gzip is the safe default every server understands\n" +
                        "\n" +
                        "       o      zstd shrinks further and faster where the server supports it\n" +
                        "\n" +
                        "       o      none is fastest when artifacts are already compressed\n" +
                        "\n" +
                        "       Mirroring pre-compressed archives typically uses:\n" +
                        "\n" +
                        "           mirror --codec=none central ./mirrors/central\n" +
                        "\n" +
                        "       The connection options control how the client reaches the server.\n" +
                        "\n" +
                        "ADDRESSING\n" +
                        "       The  client  resolves  the server address once at startup and holds one connection for\n" +
                        "       the whole run:\n" +
                        "\n" +
                        "       o      a bare hostname connects on the default mirror port\n" +
                        "\n" +
                        "       o      a port is only needed where several mirrors share one host\n" +
                        "\n" +
                        "       Reaching the second mirror daemon on a shared host:\n" +
                        "\n" +
                        "           mirror --host=mirrors.example.org --port=2874 central ./mirrors/central\n" +
                        "\n" +
                        "       The  throttle  interceptor  slows  the  command  to  a pace the remote server accepts.\n" +
                        "\n" +
                        "RATE LIMITS\n" +
                        "       Requests  spend  from a per-minute budget and the throttle pauses the command whenever\n" +
                        "       the budget is gone. A pause also happens when:\n" +
                        "\n" +
                        "       o      the server answers with a retry-after header\n" +
                        "\n" +
                        "       o      the server closes the connection mid-request\n" +
                        "\n" +
                        "       o      three requests in a row exceed a second of latency\n" +
                        "\n" +
                        "       Mirroring politely from a rate-limited public server:\n" +
                        "\n" +
                        "           mirror --rate=30 central ./mirrors/central\n" +
                        "\n" +
                        "       The limit options bound what a single mirror run may consume.\n" +
                        "\n" +
                        "BUDGETS\n" +
                        "       Each  budget  is  enforced  independently  and  the run stops cleanly at the first one\n" +
                        "       exhausted:\n" +
                        "\n" +
                        "       o      burst bounds how many requests may go unthrottled\n" +
                        "\n" +
                        "       o      every budget resets when the run completes\n" +
                        "\n" +
                        "       Letting a nightly job sprint through small changes:\n" +
                        "\n" +
                        "           mirror --burst=64 central ./mirrors/central\n" +
                        "\n" +
                        "       The  audit interceptor records every invocation so operators can see who mirrored what\n" +
                        "       and when.\n" +
                        "\n" +
                        "AUDIT RECORDS\n" +
                        "       One  record  is  appended  per  invocation,  after  the  command completes, whether it\n" +
                        "       succeeded or failed:\n" +
                        "\n" +
                        "       o      who ran the command and from which host\n" +
                        "\n" +
                        "       o      the exact options and arguments used\n" +
                        "\n" +
                        "       o      the exit status and elapsed time\n" +
                        "\n" +
                        "       A record in the audit file looks like:\n" +
                        "\n" +
                        "           2026-08-18T14:02:11Z dblevins mirror central exit=0 41s\n" +
                        "\n" +
                        "ARGUMENTS\n" +
                        "       String the repository to copy from\n" +
                        "\n" +
                        "       String the repository to copy into\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --verbose\n" +
                        "              print each artifact as it lands rather than a final summary\n" +
                        "\n" +
                        "       --chunk-size=<Integer>\n" +
                        "              bytes sent per request; larger chunks favor throughput over feedback\n" +
                        "       \n" +
                        "              default: 65536\n" +
                        "\n" +
                        "       --codec=<String>\n" +
                        "              the algorithm artifacts are compressed with before sending\n" +
                        "       \n" +
                        "              default: gzip\n" +
                        "\n" +
                        "       --host=<String>\n" +
                        "              the server holding the repository to mirror\n" +
                        "       \n" +
                        "              default: localhost\n" +
                        "\n" +
                        "       --port=<Integer>\n" +
                        "              the port the server listens on\n" +
                        "       \n" +
                        "              default: 2873\n" +
                        "\n" +
                        "       --rate=<Integer>\n" +
                        "              requests permitted per minute before the throttle pauses\n" +
                        "       \n" +
                        "              default: 100\n" +
                        "\n" +
                        "       --burst=<Integer>\n" +
                        "              requests the throttle forgives before it starts pausing\n" +
                        "       \n" +
                        "              default: 16\n" +
                        "\n" +
                        "       --audit-file=<File>\n" +
                        "              the file invocation records are appended to\n",
                env.getOut().toString());
    }

    public static class Repo {

        /**
         * The mirror command copies every artifact of a remote repository
         * into a local one and keeps the two identical from then on.
         *
         * MIRROR LIFECYCLE
         *
         * A first run transfers everything; later runs transfer only what
         * changed since, so a mirror stays fresh at a fraction of the
         * original cost.  Every run walks the same three phases:
         *
         * - scan the remote index for artifacts newer than their local copies
         * - transfer the artifacts that changed
         * - verify checksums and commit the updated index
         *
         * A typical first mirror of a repository named central:
         *
         *     mirror --verbose central ./mirrors/central
         *
         * @param verbose print each artifact as it lands rather than a final summary
         * @param source the repository to copy from
         * @param dest the repository to copy into
         */
        @Command(interceptedBy = {AuditInterceptor.class, ThrottleInterceptor.class})
        public void mirror(@Option("verbose") final boolean verbose,
                           final Transfer transfer,
                           final Connection connection,
                           final String source,
                           final String dest) {
        }
    }

    /**
     * The transfer options control how bytes move across the wire once a
     * connection is made.
     *
     * CHUNK SIZING
     *
     * Artifacts stream in fixed-size chunks and the chunk size is the main
     * throughput lever:
     *
     * - small chunks give frequent progress and cheap retries
     * - large chunks cut per-request overhead on fast, reliable links
     *
     * A gigabit link between data centers usually wants:
     *
     *     mirror --chunk-size=1048576 central ./mirrors/central
     */
    @Options
    public static class Transfer {

        /**
         * @param chunkSize bytes sent per request; larger chunks favor throughput over feedback
         */
        public Transfer(@Option("chunk-size") @Default("65536") final Integer chunkSize,
                        final Compression compression) {
        }
    }

    /**
     * The compression options control how artifacts shrink before they
     * travel.
     *
     * CODECS
     *
     * The codec trades processor time for bytes on the wire:
     *
     * - gzip is the safe default every server understands
     * - zstd shrinks further and faster where the server supports it
     * - none is fastest when artifacts are already compressed
     *
     * Mirroring pre-compressed archives typically uses:
     *
     *     mirror --codec=none central ./mirrors/central
     */
    @Options
    public static class Compression {

        /**
         * @param codec the algorithm artifacts are compressed with before sending
         */
        public Compression(@Option("codec") @Default("gzip") final String codec) {
        }
    }

    /**
     * The connection options control how the client reaches the server.
     *
     * ADDRESSING
     *
     * The client resolves the server address once at startup and holds one
     * connection for the whole run:
     *
     * - a bare hostname connects on the default mirror port
     * - a port is only needed where several mirrors share one host
     *
     * Reaching the second mirror daemon on a shared host:
     *
     *     mirror --host=mirrors.example.org --port=2874 central ./mirrors/central
     */
    @Options
    public static class Connection {

        /**
         * @param host the server holding the repository to mirror
         * @param port the port the server listens on
         */
        public Connection(@Option("host") @Default("localhost") final String host,
                          @Option("port") @Default("2873") final Integer port) {
        }
    }

    /**
     * The limit options bound what a single mirror run may consume.
     *
     * BUDGETS
     *
     * Each budget is enforced independently and the run stops cleanly at
     * the first one exhausted:
     *
     * - burst bounds how many requests may go unthrottled
     * - every budget resets when the run completes
     *
     * Letting a nightly job sprint through small changes:
     *
     *     mirror --burst=64 central ./mirrors/central
     */
    @Options
    public static class Limits {

        /**
         * @param burst requests the throttle forgives before it starts pausing
         */
        public Limits(@Option("burst") @Default("16") final Integer burst) {
        }
    }

    @Priority(2)
    public static class ThrottleInterceptor {

        /**
         * The throttle interceptor slows the command to a pace the remote
         * server accepts.
         *
         * RATE LIMITS
         *
         * Requests spend from a per-minute budget and the throttle pauses
         * the command whenever the budget is gone.  A pause also happens
         * when:
         *
         * - the server answers with a retry-after header
         * - the server closes the connection mid-request
         * - three requests in a row exceed a second of latency
         *
         * Mirroring politely from a rate-limited public server:
         *
         *     mirror --rate=30 central ./mirrors/central
         *
         * @param rate requests permitted per minute before the throttle pauses
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("rate") @Default("100") final Integer rate,
                                final Limits limits) {
            return crestContext.proceed();
        }
    }

    @Priority(7)
    public static class AuditInterceptor {

        /**
         * The audit interceptor records every invocation so operators can
         * see who mirrored what and when.
         *
         * AUDIT RECORDS
         *
         * One record is appended per invocation, after the command
         * completes, whether it succeeded or failed:
         *
         * - who ran the command and from which host
         * - the exact options and arguments used
         * - the exit status and elapsed time
         *
         * A record in the audit file looks like:
         *
         *     2026-08-18T14:02:11Z dblevins mirror central exit=0 41s
         *
         * @param auditFile the file invocation records are appended to
         */
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext,
                                @Option("audit-file") final File auditFile) {
            return crestContext.proceed();
        }
    }
}
