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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Defaults.DefaultMapping;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.processors.OptionParam;

@RunWith(DefaultInterpolationTest.DefaultCasesRunner.class)
public class DefaultInterpolationTest {
    @Command
    @Expected("test-sys-value")
    public String fromSystemProperty(@Option("value") @Default("${sys.test.env.value}") final String value) {
        return value;
    }

    @Command
    @Expected("test-env-value")
    public String fromEnv(@Option("value") @Default("${env.TEST_ENV_VALUE}") final String value) {
        return value;
    }

    @Command
    @Expected("fallback")
    public String fromFallback(@Option("value") @Default("${foo:-fallback}") final String value) {
        return value;
    }

    @Command
    @Expected("test-env-value")
    public String envSetWithFallback(@Option("value") @Default("${env.TEST_ENV_VALUE:-fallback}") final String value) {
        return value;
    }

    @Command
    @Expected("fallback")
    public String envNotSetWithFallback(@Option("value") @Default("${env.TEST_ENV_VALUE_NOT_SET:-fallback}") final String value) {
        return value;
    }

    @Command
    @Expected("c,b,a")
    public String defaultOnList(
            @DefaultMapping(
                name = "value",
                value = "a,b,c")
            @Option("") final Binding binding) {
        Collections.reverse(binding.value);
        return String.join(",", binding.value);
    }

    @Options
    public static class Binding {
        private final List<String> value;

        public Binding(@Option("value") final List<String> value) {
            this.value = value;
        }
    }

    public static class DefaultCasesRunner extends BlockJUnit4ClassRunner {
        private List<FrameworkMethod> tests;

        public DefaultCasesRunner(final Class<?> klass) throws InitializationError {
            super(klass);
        }

        @Override // just ensure we can implement the invoker trivially
        protected void collectInitializationErrors(final List<Throwable> errors) {
            for (final FrameworkMethod method : computeTestMethods()) {
                if (method.getReturnType() != String.class) {
                    errors.add(new IllegalArgumentException(method.getMethod() + " should return a String"));
                }
                final Command command = method.getAnnotation(Command.class);
                if (command == null || !command.value().isEmpty()) {
                    errors.add(new IllegalArgumentException(method.getMethod() + " should have @Command on it without any value"));
                }
                final Class<?>[] parameterTypes = method.getMethod().getParameterTypes();
                if (parameterTypes.length != 1 ||
                        (parameterTypes[0] != String.class && !parameterTypes[0].isAnnotationPresent(Options.class))) {
                    errors.add(new IllegalArgumentException(method.getMethod() + " should have one @Options or String parameter"));
                }
            }
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            return tests == null ? (tests = getTestClass().getAnnotatedMethods(Expected.class)) : tests;
        }

        @Override
        protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final String commandName = method.getName();
                    final String expectedOutput = method.getAnnotation(Expected.class).value();
                    final Object actualOutput = new Main(method.getMethod().getDeclaringClass()).exec(commandName);
                    assertEquals(expectedOutput, actualOutput);
                }
            };
        }
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface Expected {
        String value();
    }
}
