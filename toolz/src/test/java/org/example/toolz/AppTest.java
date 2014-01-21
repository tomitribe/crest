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
package org.example.toolz;


import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.Main;

/**
 * Unit test for simple App.
 */
public class AppTest extends Assert {

    @Test
    public void testApp() throws Exception {
        final Main main = new Main(new ClassesArchive(App.class));

        assertEquals("Hello, World!", main.exec("hello"));
        assertEquals("Hello, Wisconsin!", main.exec("hello", "--name=Wisconsin"));
        assertEquals("Hola, Ecuador!", main.exec("hello", "--name=Ecuador", "--language=ES"));
    }
}
