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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;

import java.util.Map;

public class SubCommandsTest extends Assert {

    private final Map<String, Cmd> commands = org.tomitribe.crest.Commands.get(Git.class);

    @Test
    public void test() throws Exception {
        final Cmd git = commands.get("git");

        assertEquals("cmd:push repo:foo", git.exec("push", "foo"));
        assertEquals("cmd:pull repo:foo", git.exec("pull", "foo"));
    }


    @Test
    public void testNoSuchCommand() throws Exception {
        final Cmd git = commands.get("git");

        try {
            git.exec("update", "foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testMissingCommand() throws Exception {
        final Cmd git = commands.get("git");

        try {
            git.exec();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }


    @Command
    public static class Git {

        @Command
        public String push(@Option("verbose") boolean verbose, String repo) {
            return "cmd:push repo:" + repo;
        }

        @Command
        public String pull(@Option("verbose") boolean verbose, String repo) {
            return "cmd:pull repo:" + repo;
        }
    }
}
