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
package org.tomitribe.crest.cli.impl.command;

import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.ByteArrayInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import static java.util.Collections.singletonMap;

// just for classloading
public final class JSonP {
    private JSonP() {
        // no-op
    }

    public static void format(final String content, final PrintStream out) {
        final JsonProvider provider = JsonProvider.provider();
        try (JsonReader reader = provider.createReaderFactory(Collections.<String, Object>emptyMap())
                .createReader(new ByteArrayInputStream(content.getBytes("UTF-8")));
             JsonWriter writer = provider.createWriterFactory(singletonMap(JsonGenerator.PRETTY_PRINTING, "true"))
                     .createWriter(new FilterOutputStream(out) {
                         @Override
                         public void close() throws IOException {
                             super.flush(); // stdout shouldnt get closed
                         }
                     });) {
            writer.write(reader.read());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
