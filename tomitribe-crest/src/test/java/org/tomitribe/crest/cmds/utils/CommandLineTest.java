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
package org.tomitribe.crest.cmds.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommandLineTest {

    @Test
    public void testStandardCommandLine() {
        String[] args = CommandLine.translateCommandline("git pull --verbose=true");
        assertEquals(3, args.length);
        assertEquals("git", args[0]);
        assertEquals("pull", args[1]);
        assertEquals("--verbose=true", args[2]);
    }

    @Test
    public void testQuotedCommandLine() {
        String[] args = CommandLine.translateCommandline("\"git\" \"pull\" \"--verbose=true\"");
        assertEquals(3, args.length);
        assertEquals("git", args[0]);
        assertEquals("pull", args[1]);
        assertEquals("--verbose=true", args[2]);
    }
    
    @Test
    public void testQuotedOption() {
        String[] args = CommandLine.translateCommandline("git pull --verbose=\"true\"");
        assertEquals(3, args.length);
        assertEquals("git", args[0]);
        assertEquals("pull", args[1]);
        assertEquals("--verbose=true", args[2]);
    }
    
    @Test
    public void testQuotedOptionWithASpace() {
        String[] args = CommandLine.translateCommandline("set user --name=\"Tomitribe Crest\"");
        assertEquals(3, args.length);
        assertEquals("set", args[0]);
        assertEquals("user", args[1]);
        assertEquals("--name=Tomitribe Crest", args[2]);
    }
}
