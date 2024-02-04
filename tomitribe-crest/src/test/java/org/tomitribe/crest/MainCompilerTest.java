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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.compiler.SimpleCompiler;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainCompilerTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void compile() throws Exception {
        final Path source = folder.getRoot().toPath().resolve("Source.java");
        Files.write(source, ("" +
                "//-- import org.tomitribe.crest.api.Command;\n" +
                "\n" +
                "//-- @Command\n" +
                "class MyCommand2 {\n" +
                "  //-- @Command\n" +
                "  public static String test() { return \"ok\"; }\n" +
                "}\n" +
                "\n" +
                "").getBytes(StandardCharsets.UTF_8));
        assertEquals("ok", new Main(emptyList()).exec("--crest.source=" + source, "myCommand2", "test"));
    }
}
