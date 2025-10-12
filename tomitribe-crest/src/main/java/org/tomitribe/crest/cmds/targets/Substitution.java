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
package org.tomitribe.crest.cmds.targets;

import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.EnvDefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.lang.Substitutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.Locale.ROOT;

/**
 * @version $Revision$ $Date$
 */
public class Substitution {

    private static final Map<String, DefaultsContext> DEFAULTS_JVM_CONTEXTS = new HashMap<String, DefaultsContext>() {{
        put("env.", new EnvDefaultsContext());
        put("sys.", new SystemPropertiesDefaultsContext());
        for (final DefaultsContext defaultsContext : ServiceLoader.load(DefaultsContext.class)) {
            put(defaultsContext.getClass().getSimpleName().toLowerCase(ROOT), defaultsContext);
        }
    }};

    private Substitution() {
        // no-op
    }

    public static String format(final Target target, final Method method, final String input, final DefaultsContext df) {
        return format(target, method, input, df, new HashSet<String>());
    }

    private static String format(final Target target, final Method method, final String input,
                                 final DefaultsContext df, final Set<String> seen) {
        if (!seen.add(input)) {
            throw new IllegalStateException("Circular reference in " + input);
        }

        return new Substitutor() {
            @Override
            protected String getOrDefault(final String varName, final String varDefaultValue) {
                for (final Map.Entry<String, DefaultsContext> ctx : DEFAULTS_JVM_CONTEXTS.entrySet()) {
                    if (varName.startsWith(ctx.getKey())) {
                        final String value = ctx.getValue().find(varName.substring(ctx.getKey().length()));
                        if (value != null) {
                            return value;
                        }
                    }
                }
                final String value = df.find(varName);
                return value == null ? varDefaultValue : value;
            }
        }.replace(input);
    }
}
