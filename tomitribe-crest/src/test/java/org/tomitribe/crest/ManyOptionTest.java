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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManyOptionTest extends TestCase{
    private final Map<String, Cmd> commands = org.tomitribe.crest.cmds.processors.Commands.get(Commands.class);

    public void testString() throws Exception {
        commands.get("doString").exec("--foo=2", "--foox=3", "-f=5");
    }

    public static class Commands {

        @Command
        public void doString(@Option({"foo", "foox", "f"}) final List<String> list) {
            assertNotNull(list);

            final Iterator<String> it = list.iterator();
            assertEquals("2", it.next());
            assertEquals("3", it.next());
            assertEquals("5", it.next());
            assertFalse(it.hasNext());
        }
    }
}
