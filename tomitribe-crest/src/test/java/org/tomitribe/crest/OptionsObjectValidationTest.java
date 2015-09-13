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
import org.apache.bval.constraints.NotEmpty;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.Cmd;

import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Map;

public class OptionsObjectValidationTest extends TestCase {

    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testOptionsAndArgs() throws Exception {
        final Cmd validation = commands.get("validation");

        validation.exec(null, "--key=color", "--value=orange", "red", "blue://foo");

        try {
            validation.exec(null, "--key=color", "--value=orange", "", "blue://foo");
            fail();
        } catch (final ConstraintViolationException e) {
        }

        try {
            validation.exec(null, "--key=color", "--value=", "red", "blue://foo");
            fail();
        } catch (final ConstraintViolationException e) {
        }
    }

    public static class Commands {

        @Command
        public void validation(Bean bean) {
            assertNotNull(bean);

            assertEquals("color", bean.getKey());
            assertEquals("orange", bean.getValue());
            assertEquals("red", bean.getString());
            assertEquals(URI.create("blue://foo"), bean.getUri());
        }
    }

    @Options
    public static class Bean {

        private final String key;
        private final String value;
        private final String string;
        private final URI uri;

        public Bean(@Option("key") final String key, @NotEmpty @Option("value") final String value, @NotEmpty String string, URI uri) {
            this.key = key;
            this.value = value;
            this.string = string;
            this.uri = uri;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getString() {
            return string;
        }

        public URI getUri() {
            return uri;
        }
    }
}
