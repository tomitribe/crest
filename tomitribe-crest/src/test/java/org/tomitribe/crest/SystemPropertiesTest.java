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
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;

/**
 * @version $Revision$ $Date$
 */
public class SystemPropertiesTest extends Assert {

    @Test
    public void dashD() throws Exception {

        final Main main = new Main(Orange.class);

        assertEquals("round", main.exec("property", "-Dshape=round", "shape"));
    }

    @Test
    public void systemPropertyDefaults() throws Exception {

        final Main main = new Main(Orange.class);

        final String user = System.getProperty("user.name");
        final String home = System.getProperty("user.dir");

        assertEquals(String.format("%s - %s", "joe", new File("/tmp/cool").getAbsolutePath()), main.exec("defaults", "--user=joe", "--home=/tmp/cool"));
        assertEquals(String.format("%s - %s", user, home), main.exec("defaults"));
    }

    public static class Orange {

        @Command
        public String property(final String name) {
            return System.getProperty(name);
        }

        @Command
        public String defaults(@Option("user") @Default("${user.name}") final String user,
                               @Option("home") @Default("${user.dir}") final File home) {
            return String.format("%s - %s", user, home.getAbsolutePath());
        }
    }
}
