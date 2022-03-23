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
package org.tomitribe.crest.arthur;

import org.apache.geronimo.arthur.impl.nativeimage.ArthurNativeImageConfiguration;
import org.apache.geronimo.arthur.impl.nativeimage.generator.DefautContext;
import org.apache.geronimo.arthur.spi.model.ClassReflectionModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Editor;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.crest.table.TableInterceptor;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CrestExtensionTest {
    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void generate() throws Exception {
        final Method foo = Enclosing.class.getMethod("foo");

        final CrestExtension extension = new CrestExtension();
        final ArthurNativeImageConfiguration configuration = new ArthurNativeImageConfiguration();
        final DefautContext context = new DefautContext(
                configuration,
                annot -> {
                    if (Editor.class == annot) {
                        return singleton(MyEditor.class);
                    }
                    if (Table.class == annot) {
                        return singleton(TableInterceptor.class);
                    }
                    return Collections.emptySet();
                },
                annot -> singleton(foo),
                annot -> {
                    throw new UnsupportedOperationException();
                },
                a -> {
                    throw new UnsupportedOperationException();
                },
                singletonMap("workingDirectory", temp.getRoot().getAbsolutePath()));
        extension.execute(context);

        final Collection<ClassReflectionModel> reflections = context.getReflections();
        assertEquals(4, reflections.size());

        final Path spiFile = temp.getRoot().toPath().resolve("crest-commands.txt");
        assertTrue(Files.exists(spiFile));
        assertEquals(asList("org.tomitribe.crest.EditorLoader", Enclosing.class.getName(), TableInterceptor.class.getName()), Files.readAllLines(spiFile));

        final Path spi2File = temp.getRoot().toPath().resolve("crest-editors.txt");
        assertTrue(Files.exists(spi2File));
        assertEquals(singletonList(MyEditor.class.getName()), Files.readAllLines(spi2File));

        assertEquals(
                asList(
                        "-H:TomitribeCrestEditors=" + spi2File,
                        "-H:TomitribeCrestCommands=" + spiFile),
                configuration.getCustomOptions());
    }

    @Editor(String.class)
    public static class MyEditor {
    }

    public static class Enclosing {
        @Command
        @Table
        public static void foo() {
        }
    }
}
