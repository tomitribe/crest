/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest;

import junit.framework.TestCase;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.util.Duration;
import org.tomitribe.util.Size;

import java.util.Map;

public class OptionsObjectsCanBeNullTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testCanBeNull() throws Exception {
        commands.get("canBeNull").exec(null);
    }

    public void testSomeNull() throws Exception {
        commands.get("somenull").exec(null, "--min-bytes=10");
    }

    public void testSomeNull2() throws Exception {
        commands.get("somenull2").exec(null, "--min-bytes=10");
    }

    public void testSomeNull3() throws Exception {
        commands.get("somenull3").exec(null, "--response-code=3");
    }

    public void testMixed() throws Exception {
        commands.get("mixed").exec(null, "--response-code=3", "--max-bytes=4bytes");
    }

    /**
     * No arguments are passed, but the @Options object constructor
     * lists a default value so should still be created.
     *
     * @throws Exception
     */
    public void testNeverNull() throws Exception {
        commands.get("nevernull").exec(null);
        commands.get("nevernull2").exec(null, "--max-time=12seconds");
        commands.get("nevernull3").exec(null, "--max-time=12seconds", "--min-time=8seconds");
    }

    public static class Commands {

        @Command
        public void canBeNull(ResponseCode code) {
            assertNull(code);
        }

        @Command
        public void somenull(Bytes bytes) {
            assertNull(bytes.getMax());
            assertNotNull(bytes.getMin());
        }

        @Command
        public void somenull2(ResponseCode code, Bytes bytes) {
            assertNull(code);

            assertNull(bytes.getMax());
            assertNotNull(bytes.getMin());
        }

        @Command
        public void somenull3(ResponseCode code, Bytes bytes) {
            assertNotNull(code);
            assertNotNull(code.getCode());
            assertEquals(3, code.getCode().intValue());

            assertNull(bytes);
        }

        @Command
        public void mixed(ResponseCode code, Bytes bytes) {
            assertNotNull(code);
            assertNotNull(code.getCode());
            assertEquals(3, code.getCode().intValue());

            assertNotNull(bytes);
            assertNull(bytes.getMin());
            assertNotNull(bytes.getMax());
            assertEquals(new Size("4bytes"), bytes.getMax());
        }

        @Command
        public void nevernull(Time time) {
            assertNotNull(time);
            assertEquals(new Duration("2 seconds"), time.getMin());
            assertNull(time.getMax());
        }

        @Command
        public void nevernull2(Time time) {
            assertNotNull(time);
            assertEquals(new Duration("2 seconds"), time.getMin());
            assertEquals(new Duration("12 seconds"), time.getMax());
        }

        @Command
        public void nevernull3(Time time) {
            assertNotNull(time);
            assertEquals(new Duration("8 seconds"), time.getMin());
            assertEquals(new Duration("12 seconds"), time.getMax());
        }

    }


    @Options
    public static class ResponseCode {

        private final Integer code;

        public ResponseCode(@Option("response-code") Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }

    @Options
    public static class Bytes {

        private final Size min;
        private final Size max;

        public Bytes(@Option("min-bytes") Size min, @Option("max-bytes") Size max) {
            this.min = min;
            this.max = max;
        }

        public Size getMin() {
            return min;
        }

        public Size getMax() {
            return max;
        }
    }

    @Options
    public static class Time {

        private final Duration min;
        private final Duration max;

        public Time(@Option("min-time") @Default("2 seconds") Duration min, @Option("max-time") Duration max) {
            this.min = min;
            this.max = max;
        }

        public Duration getMin() {
            return min;
        }

        public Duration getMax() {
            return max;
        }
    }
}
