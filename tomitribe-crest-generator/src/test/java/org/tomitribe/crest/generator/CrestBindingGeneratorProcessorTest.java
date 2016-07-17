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
package org.tomitribe.crest.generator;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;

public class CrestBindingGeneratorProcessorTest {
    @Test
    public void generate() throws Exception {
        try (final URLClassLoader loader = new URLClassLoader(
                new URL[] {new File("target/generated-test-sources/test-annotations").toURI().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            final Object i = loader.loadClass("org.tomitribe.crest.generator.generated.App").getConstructor(String.class, Integer.class)
                    .newInstance("base", 1234);
            assertEquals("base", i.getClass().getMethod("getServiceBase").invoke(i));
            assertEquals(1234, i.getClass().getMethod("getServiceRetries").invoke(i));
            assertEquals("1234", ConfigResolver.getPropertyValue("app.service.retries"));
            assertEquals("base", ConfigResolver.getPropertyValue("app.service.base"));
        }
    }

    public static class DeltaspikeBean {
        @Inject
        @ConfigProperty(name = "app.service.base", defaultValue = "http://localhost:8080")
        private String base;

        @Inject
        @ConfigProperty(name = "app.service.retries")
        private Integer retries;
    }
}
