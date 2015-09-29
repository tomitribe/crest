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
package org.tomitribe.crest.cli.impl;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class CommandParserTest {
    @Test
    public void single() {
        final CommandParser.Command[] args = new CommandParser().toArgs("a");
        assertEquals(1, args.length);
        assertEquals(singletonList("a"), asList(args[0].getArgs()));
    }

    @Test
    public void simple() {
        final CommandParser commandParser = new CommandParser();
        assertEquals(asList("a", "b", "c", "1234", "word"), asList(commandParser.toArgs("a b c 1234 word")[0].getArgs()));
        assertEquals(asList("a", "b", "-c", "--1234", "word"), asList(commandParser.toArgs("a b -c --1234 word")[0].getArgs()));
    }

    @Test
    public void quotes() {
        final CommandParser.Command[] args = new CommandParser().toArgs("a \"b\" c \"1234,76\" \\\\ \"sentence\\ \\\\\\\" chkdwc\"");
        assertEquals(1, args.length);
        assertEquals(
            asList("a", "b", "c", "1234,76", "\\", "sentence \\\" chkdwc"),
            asList(args[0].getArgs()));
    }

    @Test
    public void singleQuote() {
        final CommandParser commandParser = new CommandParser();
        assertEquals(
            asList("a", "b", "c", "1234,76", "\\", "sentence \\' chkdwc"),
            asList(commandParser.toArgs("a 'b' c '1234,76' \\\\ \"sentence\\ \\\\' chkdwc\"")[0].getArgs()));
        assertEquals(
            asList("a", "b", "c", "1234,76", "\\", "sentence \\' chkdwc"),
            asList(commandParser.toArgs("a 'b' c '1234,76' \\\\ 'sentence\\ \\\\\\' chkdwc'")[0].getArgs()));
    }

    @Test
    public void piping() {
        {
            final CommandParser.Command[] args = new CommandParser().toArgs("a|b");
            assertEquals(2, args.length);
            assertEquals(singletonList("a"), asList(args[0].getArgs()));
            assertEquals(singletonList("b"), asList(args[1].getArgs()));
        }
        {
            final CommandParser.Command[] args = new CommandParser().toArgs("test --option=value \"-quoted=ckdwc\\\\cekwcbw\" | grep \"==foo\"");
            assertEquals(2, args.length);
            assertEquals(asList("test", "--option=value", "-quoted=ckdwc\\cekwcbw"), asList(args[0].getArgs()));
            assertEquals(asList("grep", "==foo"), asList(args[1].getArgs()));
        }
    }
}
