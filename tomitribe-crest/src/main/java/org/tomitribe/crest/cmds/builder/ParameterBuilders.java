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
package org.tomitribe.crest.cmds.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public final class ParameterBuilders {
    public static final List<ParameterBuilder> DEFAULTS = // order is important, that is why it is a list
            asList(new OptionsBuilder(), new OptionBuilder(), new StdinBuilder(), new StdoutBuilder(), new StderrBuilder());

    private ParameterBuilders() {
        // no-op
    }

    public static Map<Class<?>, ParameterBuilder> map(final List<ParameterBuilder> injectors) {
        final Map<Class<?>, ParameterBuilder> injectorMap = new LinkedHashMap<Class<?>, ParameterBuilder>();
        for (final ParameterBuilder injector : injectors) {
            injectorMap.put(injector.marker(), injector);
        }
        return injectorMap;
    }
}
