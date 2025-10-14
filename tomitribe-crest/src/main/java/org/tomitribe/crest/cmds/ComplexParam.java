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

import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Err;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.val.BeanValidationImpl;
import org.tomitribe.util.Join;
import org.tomitribe.util.reflect.Parameter;
import org.tomitribe.util.reflect.Reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComplexParam extends Param {

    private final List<Param> parameters;
    private final Constructor<?> constructor;
    private final boolean nullable;
    private final BeanValidationImpl beanValidation;
    private final Spec spec;

    public ComplexParam(final Spec spec, final BeanValidationImpl beanValidation, final String[] prefixes, final String globalDescription,
                        final Defaults.DefaultMapping[] defaults, final Parameter parent, final boolean nullable) {
        super(parent);
        this.spec = spec;
        this.beanValidation = beanValidation;
        this.constructor = selectConstructor(parent);
        this.parameters = Collections.unmodifiableList(spec.buildParams(beanValidation, globalDescription, prefixes, defaults, Reflection.params(constructor)));
        this.nullable = nullable;
    }


    private Constructor<?> selectConstructor(final Parameter parent) {
        final List<Constructor<?>> constructors = Arrays.asList(parent.getType().getConstructors());
        constructors.sort(Comparator.comparing(Object::toString));

        if (constructors.size() == 1) {
            return constructors.get(0);
        }

        final Constructor<?> annotatedConstructor = constructors.stream()
                .filter(this::isAnnotated)
                .findFirst()
                .orElse(null);

        if (annotatedConstructor != null) {
            return annotatedConstructor;
        }

        return constructors.get(0);
    }

    private boolean isAnnotated(final Constructor<?> constructor) {
        for (final Annotation[] annotations : constructor.getParameterAnnotations()) {
            for (final Annotation annotation : annotations) {
                final Class<? extends Annotation> type = annotation.annotationType();
                if (Option.class.equals(type)) return true;
                if (Default.class.equals(type)) return true;
                if (Required.class.equals(type)) return true;
                if (Out.class.equals(type)) return true;
                if (In.class.equals(type)) return true;
                if (Err.class.equals(type)) return true;
            }
        }
        return false;
    }

    public CmdMethod.Value convert(final Arguments arguments, final Needed needed) {
        final List<CmdMethod.Value> converted = CmdMethod.convert(arguments, needed, getParameters());
        if (nullable) {
            boolean allNull = true;
            for (final CmdMethod.Value val : converted) {
                if (val.isProvided()) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                return new CmdMethod.Value(null, false);
            }
        }

        try {
            final Object[] args = CmdMethod.toArgs(converted).toArray(new Object[converted.size()]);
            if (beanValidation != null) {
                beanValidation.validateParameters(constructor, args);
            }
            return new CmdMethod.Value(constructor.newInstance(args), true);

        } catch (InvocationTargetException e) {

            throw CmdMethod.toRuntimeException(e.getCause());

        } catch (Exception e) {

            throw CmdMethod.toRuntimeException(e);
        }
    }

    public List<Param> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "ComplexParam{" +
                "index=" + getIndex() +
                ", type=" + getType() +
                ", parameters=" + Join.join(" ", parameters) +
                '}';
    }
}
