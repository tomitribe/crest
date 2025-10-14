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

import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.val.BeanValidationImpl;
import org.tomitribe.util.Join;
import org.tomitribe.util.reflect.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.tomitribe.crest.cmds.CmdMethod.NO_PREFIX;

public class GlobalSpec {

    private final List<Param> parameters;
    private final DefaultsContext defaultsFinder;
    private final Spec spec;
    private final BeanValidationImpl beanValidation;
    private final List<ParameterMetadata> parameterMetadata;

    public GlobalSpec(final BeanValidationImpl beanValidation, final DefaultsContext defaultsFinder, final List<Class<?>> classes) {
        this.beanValidation = beanValidation;
        this.defaultsFinder = defaultsFinder;
        this.spec = new Spec(beanValidation);

        final List<Parameter> collect = classes.stream()
                .map(aClass -> new Parameter(aClass.getAnnotations(), aClass, aClass))
                .collect(Collectors.toList());

        this.parameters = spec.buildParams(beanValidation, null, NO_PREFIX, null, collect);
        this.parameterMetadata = CmdMethod.buildApiParameterViews(parameters);
    }

    public List<Object> parse(final String... rawArgs) {
        final Arguments args = new Arguments(defaultsFinder, spec, rawArgs);

        final Needed needed = new Needed(spec.getArguments().size());

        final List<CmdMethod.Value> converted = CmdMethod.convert(args, needed, parameters);

        if (!args.getList().isEmpty()) {
            throw new IllegalArgumentException("Excess arguments: " + Join.join(", ", args.getList()));
        }

        if (!args.getOptions().isEmpty()) {
            throw new IllegalArgumentException("Unknown arguments: " + Join.join(", ", CmdMethod.STRING_NAME_CALLBACK, args.getOptions().keySet()));
        }

        return CmdMethod.toArgs(converted);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DefaultsContext defaultsContext;
        private BeanValidationImpl beanValidation;
        private final List<Class<?>> classes = new ArrayList<>();

        public Builder defaults(final DefaultsContext defaultsContext) {
            this.defaultsContext = defaultsContext;
            return this;
        }

        public Builder beanValidation(final BeanValidationImpl beanValidation) {
            this.beanValidation = beanValidation;
            return this;
        }

        public Builder optionsClass(final Class<?> clazz) {
            this.classes.add(clazz);
            return this;
        }

        public GlobalSpec build() {
            return new GlobalSpec(beanValidation, defaultsContext, classes);
        }
    }

}
