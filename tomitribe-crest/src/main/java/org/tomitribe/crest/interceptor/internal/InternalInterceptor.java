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
package org.tomitribe.crest.interceptor.internal;

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.Priority;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.ComplexParam;
import org.tomitribe.crest.cmds.Spec;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.interceptor.InterceptorAnnotationNotFoundException;
import org.tomitribe.crest.interceptor.InvalidInterceptorPriorityException;
import org.tomitribe.crest.interceptor.UnresolvedInterceptorAnnotationException;
import org.tomitribe.crest.val.BeanValidationImpl;
import org.tomitribe.util.reflect.Reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.tomitribe.crest.cmds.CmdMethod.NO_PREFIX;

public class InternalInterceptor {

    public static final double DEFAULT_PRIORITY = 5d;

    private static final Object[] NO_OPTIONS = new Object[0];

    private final Target target;
    private final Method method;
    private final Class<?> clazz;
    private final double priority;

    /**
     * The interceptor's own option declarations, parsed from the parameters
     * of the @CrestInterceptor method.  These merge into the spec of every
     * command the interceptor is bound to.
     */
    private final Spec spec;

    /**
     * The @CrestInterceptor method's parameters minus the CrestContext,
     * in declaration order.  Converted per execution and passed to
     * {@link #intercept(CrestContext, Object[])}.
     */
    private final List<Param> optionParams;

    private final int contextIndex;
    private final int parameterCount;

    public InternalInterceptor(final Target target, final Method method, final Class<?> clazz) {
        this.target = target;
        this.method = method;
        this.clazz = clazz;
        this.priority = readPriority(clazz);

        final BeanValidationImpl beanValidation = ofNullable(Environment.ENVIRONMENT_THREAD_LOCAL.get())
                .map(e -> e.findService(BeanValidationImpl.class))
                .orElse(null);

        this.spec = new Spec(beanValidation);
        final List<Param> params = spec.buildParams(beanValidation, null, NO_PREFIX, null, Reflection.params(method));
        CmdMethod.buildApiParameterViews(params);

        int contextIndex = -1;
        final List<Param> optionParams = new ArrayList<>();

        for (int i = 0; i < params.size(); i++) {
            final Param param = params.get(i);

            if (param instanceof OptionParam || param instanceof ComplexParam) {
                optionParams.add(param);
                continue;
            }

            if (CrestContext.class.equals(param.getType()) && contextIndex < 0) {
                contextIndex = i;
                continue;
            }

            throw new IllegalArgumentException(String.format("Interceptor %s method %s may only declare" +
                            " @Option parameters, @Options beans and one CrestContext parameter." +
                            "  Parameter %s (%s) is not allowed." +
                            "  Remove it or annotate it with @Option",
                    clazz.getName(), method.getName(), i + 1, param.getType().getName()));
        }

        if (contextIndex < 0) {
            throw new IllegalArgumentException(String.format("Interceptor %s method %s must declare a" +
                            " CrestContext parameter.  Add one, e.g.:" +
                            " public Object %s(final CrestContext crestContext)",
                    clazz.getName(), method.getName(), method.getName()));
        }

        this.contextIndex = contextIndex;
        this.parameterCount = params.size();
        this.optionParams = Collections.unmodifiableList(optionParams);
    }

    /**
     * The valid range is greater than 0 and less than 11.  The endpoints are
     * deliberately excluded so no interceptor can take the last spot: there
     * is always room to slot in before or after any priority.  The negated
     * comparison also rejects NaN, which would otherwise scramble the sort.
     */
    private static double readPriority(final Class<?> clazz) {
        final Priority priority = clazz.getAnnotation(Priority.class);

        if (priority == null) {
            return DEFAULT_PRIORITY;
        }

        final double value = priority.value();

        if (!(value > 0 && value < 11)) {
            throw new InvalidInterceptorPriorityException(clazz, value);
        }

        return value;
    }

    public Object intercept(final CrestContext crestContext) {
        return intercept(crestContext, NO_OPTIONS);
    }

    public Object intercept(final CrestContext crestContext, final Object[] options) {
        final Object[] args = new Object[parameterCount];

        int option = 0;
        for (int i = 0; i < parameterCount; i++) {
            args[i] = i == contextIndex ? crestContext : options[option++];
        }

        try {
            return target.invoke(method, args);
        } catch (final InvocationTargetException e) {
            return throwRuntime(e.getCause());
        } catch (final IllegalAccessException e) {
            return throwRuntime(e);
        }
    }

    private static Object throwRuntime(final Throwable cause) { // try to propagate if possible
        throw RuntimeException.class.isInstance(cause) ? RuntimeException.class.cast(cause) : new IllegalStateException(cause);
    }

    public double getPriority() {
        return priority;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Spec getSpec() {
        return spec;
    }

    public List<Param> getOptionParams() {
        return optionParams;
    }

    public boolean hasOptions() {
        return !optionParams.isEmpty();
    }

    /**
     * Resolves the interceptor keys bound to a command method into the
     * chain that will actually run.  Keys may be interceptor classes or
     * custom interceptor annotations registered in the supplied registry.
     *
     * The same interceptor may be reachable through several keys, e.g. an
     * interceptor class carrying two custom annotations both used on the
     * method.  Such duplicates are removed so an interceptor runs once.
     * The surviving interceptors are sorted by {@link Priority}, ties
     * keeping the order the keys were declared on the method.
     */
    public static List<InternalInterceptor> resolve(final Map<Class<?>, InternalInterceptor> registry, final Class<?>[] keys) {
        final List<InternalInterceptor> chain = new ArrayList<>();
        final Set<InternalInterceptor> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        for (final Class<?> key : keys) {
            InternalInterceptor interceptor = registry.get(key);

            if (interceptor == null) {

                if (key.isAnnotation()) {
                    throw new UnresolvedInterceptorAnnotationException(key);
                }

                interceptor = from(key);
            }

            if (seen.add(interceptor)) {
                chain.add(interceptor);
            }
        }

        chain.sort(Comparator.comparingDouble(InternalInterceptor::getPriority));

        return chain;
    }

    public static InternalInterceptor from(final Class<?> clazz){
        for (final Method method : clazz.getMethods()) {
            if (Object.class == method.getDeclaringClass()) {
                continue;
            }

            final CrestInterceptor interceptor = method.getAnnotation(CrestInterceptor.class);
            if (interceptor != null) {
                return new InternalInterceptor(new SimpleBean(null), method, clazz);
            }
        }

        throw new InterceptorAnnotationNotFoundException(clazz);
    }
}
