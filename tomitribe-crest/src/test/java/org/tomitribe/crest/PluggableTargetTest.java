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

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cmds.targets.SimpleBean;

import static org.junit.Assert.assertEquals;

public class PluggableTargetTest {

    @Test
    public void mustFailWithDefaultTarget() throws Exception {
        final Main main = new Main(Orange.class);
        try {
            main.exec("color", "orange");
            throw new AssertionError("Expected an exception to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals(
                "java.lang.IllegalArgumentException: Can't instantiate `class org.tomitribe.crest" +
                ".PluggableTargetTest$Orange` using default constructor.",
                e.getMessage());
        }
    }

    @Test
    public void shouldBeAbleToUsePluggableTarget() throws Exception {
        final Main main = Main.builder()
                              .command(Orange.class)
                              .provider(clazz -> new SimpleBean(null) {
                                  @Override
                                  public Object newInstance(final Class<?> declaringClass) {
                                      return new Orange("light ");
                                  }
                              })
                              .build();
        final String result = (String) main.exec("color", "orange");
        assertEquals("light orange", result);
    }


    public static class Orange {

        private final String variant;

        public Orange(final String variant) {
            // this is just to ensure that the default SimpleBean (Target) won't work
            this.variant = variant;
        }

        @Command
        public String color(final String name) {
            return variant + name;
        }

    }

}
