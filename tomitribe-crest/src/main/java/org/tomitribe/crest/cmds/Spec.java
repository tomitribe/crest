/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.val.BeanValidationImpl;
import org.tomitribe.util.reflect.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Spec {
    private final Map<String, OptionParam> options = new LinkedHashMap<>();
    private final Map<String, OptionParam> aliases = new TreeMap<>();
    private final List<Param> arguments = new LinkedList<>();
    private final BeanValidationImpl beanValidation;

    public Spec(final BeanValidationImpl beanValidation) {
        this.beanValidation = beanValidation;
    }

    public List<Param> buildParams(final BeanValidationImpl beanValidation, final String globalDescription, final String[] inPrefixes,
                                   final Defaults.DefaultMapping[] defaultsMapping, final Iterable<Parameter> params) {
        final String[] prefixes = inPrefixes == null ? CmdMethod.NO_PREFIX : inPrefixes;
        final List<Param> parameters = new ArrayList<>();
        for (final Parameter parameter : params) {

            if (parameter.isAnnotationPresent(Option.class)) {

                final Option option = parameter.getAnnotation(Option.class);

                final Options options = parameter.getType().getAnnotation(Options.class);
                if (options != null) {

                    final Defaults defaultMappings = parameter.getAnnotation(Defaults.class);
                    final Defaults.DefaultMapping[] directMapping = parameter.getDeclaredAnnotationsByType(Defaults.DefaultMapping.class);
                    final ComplexParam complexParam = new ComplexParam(this, beanValidation,
                            option.value(), option.description(),
                            directMapping != null ? directMapping : defaultMappings.value(),
                            parameter, options.nillable());

                    parameters.add(complexParam);

                } else {
                    if (parameter.isAnnotationPresent(Defaults.class)) {
                        throw new IllegalArgumentException("Simple option doesnt support @Defaults, use @Default please");
                    }

                    final String shortName = option.value()[0];
                    final String mainOption = prefixes[0] + shortName;
                    String def = null;
                    String description = option.description();
                    if (defaultsMapping != null) {
                        for (final Defaults.DefaultMapping mapping : defaultsMapping) {
                            if (mapping.name().equals(shortName)) {
                                def = mapping.value();
                                if (!mapping.description().isEmpty()) {
                                    def = mapping.description();
                                }
                                break;
                            }
                        }
                    }
                    final OptionParam optionParam = new OptionParam(parameter, mainOption, def, (globalDescription != null ? globalDescription : "") + description);

                    final OptionParam existing = this.options.put(mainOption, optionParam);
                    if (existing != null) {
                        throw new IllegalArgumentException("Duplicate option: " + mainOption);
                    }

                    for (int i = 1; i < prefixes.length; i++) {
                        final String key = prefixes[i] + optionParam.getName();
                        final OptionParam existingAlias = this.aliases.put(key, optionParam);

                        if (existingAlias != null) {
                            throw new IllegalArgumentException("Duplicate alias: " + key);
                        }
                    }

                    for (int i = 1; i < option.value().length; i++) {
                        final String alias = option.value()[i];
                        for (final String prefix : prefixes) {
                            final String fullAlias = prefix + alias;
                            final OptionParam existingAlias = this.aliases.put(fullAlias, optionParam);

                            if (existingAlias != null) {
                                throw new IllegalArgumentException("Duplicate alias: " + fullAlias);
                            }
                        }
                    }

                    parameters.add(optionParam);
                }
            } else if (parameter.getType().isAnnotationPresent(Options.class)) {

                final ComplexParam complexParam = new ComplexParam(this, beanValidation, null, null, null, parameter, parameter.getType().getAnnotation(Options.class).nillable());

                parameters.add(complexParam);

            } else {

                final Param e = new Param(parameter);
                this.arguments.add(e);
                parameters.add(e);
            }
        }

        return parameters;
    }

    public Map<String, OptionParam> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    public Map<String, OptionParam> getAliases() {
        return Collections.unmodifiableMap(aliases);
    }

    public List<Param> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public Map<String, String> getDefaults() {
        final Map<String, String> options = new HashMap<>();

        for (final OptionParam parameter : this.getOptions().values()) {
            options.put(parameter.getName(), parameter.getDefaultValue());
        }

        return options;
    }

}
