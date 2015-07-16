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

import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.Arguments;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.ComplexParam;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.reflect.Parameter;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OptionBuilder implements ParameterBuilder {
    private static final String[] NO_PREFIX = {""};

    @Override
    public Class<? extends Annotation> marker() {
        return Option.class;
    }

    @Override
    public ParamMeta buildParameter(final CmdMethod method, final Parameter parameter) {
        if (parameter.getType().isAnnotationPresent(Options.class)) {
            return new ParamMeta(new ComplexParam(method, parameter, parameter.getType()), null, Collections.<String>emptyList());
        }
        if (parameter.isAnnotationPresent(Defaults.class)) {
            throw new IllegalArgumentException("Simple option doesn't support @Defaults, use @Default please");
        }

        final Option option = parameter.getAnnotation(Option.class);
        final String[] values = option.value();

        final Defaults defaults = parameter.getAnnotation(Defaults.class);
        final String[] prefixes = defaults == null ? NO_PREFIX : option.value();

        final String shortName = values[0];
        final String mainOption = prefixes[0] + shortName;
        String def = null;
        if (defaults != null) {
            for (final Defaults.DefaultMapping defaultMapping : defaults.value()) {
                if (shortName.equals(defaultMapping.name())) {
                    def = defaultMapping.value();
                    break;
                }
            }
        }

        final Map<String, String> aliases = new HashMap<String, String>(prefixes.length * (1 + values.length));
        for (final String prefix : prefixes) {
            final String key = prefix + mainOption;
            if (!key.equals(mainOption) && aliases.put(key, shortName) != null) {
                throw new IllegalArgumentException("Duplicate alias: " + key);
            }

            for (int i = 1; i < values.length; i++) {
                final String fullAlias = prefix + values[i];
                if (aliases.put(fullAlias, shortName) != null) {
                    throw new IllegalArgumentException("Duplicate alias: " + fullAlias);
                }
            }
        }

        return new ParamMeta(new OptionParam(parameter, mainOption, def), mainOption, new HashSet<String>(aliases.keySet()));
    }

    @Override
    public Object create(final CmdMethod method, final Param parameter, final Arguments arguments, final Arguments.Needed needed) {
        if (ComplexParam.class.isInstance(parameter)) {
            return ComplexParam.class.cast(parameter).convert(arguments, needed);
        }

        final String optionValue = OptionParam.class.isInstance(parameter) ?
                OptionParam.class.cast(parameter).getName() : parameter.getAnnotation(Option.class).value()[0];
        final String value = arguments.getOptions().remove(optionValue);

        if (parameter.isListable()) {
            return CmdMethod.convert(parameter, OptionParam.getSeparatedValues(value), optionValue);
        }
        return Converter.convert(value, parameter.getType(), optionValue);
    }
}
