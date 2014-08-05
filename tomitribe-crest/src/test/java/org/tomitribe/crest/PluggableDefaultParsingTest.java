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

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.contexts.DefaultsContext;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class PluggableDefaultParsingTest {
    @Test
    public void testPlugInNewDefaultsContext() throws Exception {
        final String user = System.getProperty("user.name");
        final String new_user = String.format("NOT%s", user);

        final Main main = new Main(new FixedValueDefaultsContext(new_user), Orange.class);

        final Object out = main.exec("defaults");

        assertEquals(out, String.format("Hello %s", new_user));
    }

    public static class Orange {

        @Command
        public String property(final String name) {
            return System.getProperty(name);
        }

        @Command
        public String defaults(@Option("user") @Default("${user.name}") final String user) {
            return String.format("Hello %s", user);
        }
    }

    public static class FixedValueDefaultsContext implements DefaultsContext {
        private final String value;

        FixedValueDefaultsContext(final String value) {
            this.value = value;
        }

        @Override
        public String find(final Target cmd, final Method commandMethod, final String key) {
            return value;
        }
    }
}
