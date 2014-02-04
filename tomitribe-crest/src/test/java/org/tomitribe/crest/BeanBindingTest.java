/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest;

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.OptionBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BeanBindingTest {
    @Test
     public void exec() {
        Commands.get(MyBeanBindingCommand.class).get("cmd").exec("--host=foo", "--port=1234", "--verbose", "--optional=done");
        assertNotNull(MyBeanBindingCommand.params);
        assertEquals("foo:1234/true/done", MyBeanBindingCommand.params);
    }

    @Test
    public void exec2() {
        Commands.get(MyBeanBindingCommand.class).get("cmd2").exec("--host=foo", "--port=1234", "--verbose", "5");
        assertNotNull(MyBeanBindingCommand.params);
        assertEquals("foo:1234/true/opt/5", MyBeanBindingCommand.params);
    }

    public static class MyBeanBindingCommand {
        private static String params = null;

        @Command
        public static void cmd(final Configuration configuration) {
            params = configuration.host + ":" + configuration.port
                    + "/" + configuration.verbose + "/" + configuration.optional;
        }

        @Command
        public static void cmd2(final Configuration configuration, final int retry) {
            params = configuration.host + ":" + configuration.port
                    + "/" + configuration.verbose + "/" + configuration.optional + "/" + retry;
        }
    }

    @OptionBean
    public static class Configuration {
        @Option("host")
        private String host;

        @Option("port")
        private int port;

        @Option("verbose")
        private boolean verbose;

        @Option("optional")
        @Default("opt")
        private String optional;
    }
}
