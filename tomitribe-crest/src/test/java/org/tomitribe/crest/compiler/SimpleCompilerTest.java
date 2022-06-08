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
package org.tomitribe.crest.compiler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleCompilerTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void compile() throws Exception {
        final Path source = folder.getRoot().toPath().resolve("Source.java");
        Files.write(source, ("" +
                "@org.tomitribe.crest.api.Command\n" +
                "class MyCommand2 {\n" +
                "  @org.tomitribe.crest.api.Command\n" +
                "  public static String test() { return \"ok\"; }\n" +
                "}\n" +
                "public class Source {\n" +
                "}\n" +
                "\n" +
                "").getBytes(StandardCharsets.UTF_8));

        final SimpleCompiler.MemoryLoader loader = SimpleCompiler.compile(source.toString());
        assertEquals(
                asList("MyCommand2", "Source"),
                loader.ownedClasses().map(Class::getName).sorted().collect(toList()));

        final Class<?> myCommand2 = loader.loadClass("MyCommand2");
        assertTrue(myCommand2.isAnnotationPresent(Command.class));

        final Main main = new Main(myCommand2);
        assertEquals("ok", main.exec("myCommand2", "test"));
    }
}
