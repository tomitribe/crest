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
package org.tomitribe.crest.help;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.IO;

import java.io.IOException;
import java.net.URL;

public class DocumentFormatterTest {


    @Test
    public void formatWidth86() throws Exception {
        assertWidth(86);
    }

    @Test
    public void formatWidth178() throws Exception {
        assertWidth(178);
    }

    @Test
    public void formatWidth106() throws Exception {
        assertWidth(106);
    }

    public void assertWidth(final int width) throws IOException {
        final Document document = RsyncManPage.getDocument();

        final DocumentFormatter documentFormatter = new DocumentFormatter(width, false);
        final String actual = documentFormatter.format(document);

        document.getElements();
        final ClassLoader loader = this.getClass().getClassLoader();
        final URL resource = loader.getResource("man/expected-" + width + ".txt");
        final String expected = IO.slurp(resource);

        Assert.assertEquals(expected, actual);
    }

}
