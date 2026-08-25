/*
 * Copyright 2026 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.interceptor;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.cmds.targets.TargetProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Interceptor instances are obtained from the Main's TargetProvider, the
 * same way command instances are.  A custom provider (an IoC container,
 * for example) can therefore construct an interceptor with dependencies
 * it could never receive through a no-arg constructor.
 */
public class InterceptorTargetProviderTest {

    @Test
    public void interceptorComesFromTheTargetProvider() throws Exception {
        final List<Class<?>> requested = new ArrayList<>();

        final TargetProvider provider = clazz -> {
            requested.add(clazz);
            return new SimpleBean(null) {
                @Override
                public Object newInstance(final Class<?> declaringClass) {
                    if (declaringClass == GreetingInterceptor.class) {
                        return new GreetingInterceptor("Bonjour");
                    }
                    return super.newInstance(declaringClass);
                }
            };
        };

        final Main main = Main.builder()
                .command(Commands.class)
                .command(GreetingInterceptor.class)
                .provider(provider)
                .build();

        assertEquals("Bonjour, Pierre", main.exec("greet", "Pierre"));
        assertTrue(requested.toString(), requested.contains(GreetingInterceptor.class));
    }

    /**
     * Without a provider that knows how to build it, an interceptor lacking
     * a no-arg constructor fails at execution, as it always has.
     */
    @Test
    public void defaultProviderStillNeedsANoArgConstructor() throws Exception {
        final Main main = new Main(Commands.class, GreetingInterceptor.class);

        try {
            main.exec("greet", "Pierre");
            fail("Expected instantiation to fail");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("default constructor"));
        }
    }

    @CrestInterceptor
    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Greeting {
    }

    @Greeting
    public static class GreetingInterceptor {
        private final String greeting;

        public GreetingInterceptor(final String greeting) {
            this.greeting = greeting;
        }

        @CrestInterceptor
        public Object intercept(final CrestContext context) {
            return greeting + ", " + context.proceed();
        }
    }

    public static class Commands {
        @Greeting
        @Command("greet")
        public static String greet(final String name) {
            return name;
        }
    }
}
