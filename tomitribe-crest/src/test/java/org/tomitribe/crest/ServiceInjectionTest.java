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
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Err;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;
import org.tomitribe.crest.interceptor.security.RoleProvider;
import org.tomitribe.util.Duration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ServiceInjectionTest {
    @Test
    public void inject() throws Exception {
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment(
            Collections.<Class<?>, Object>singletonMap(RoleProvider.class, new CustomRoleProvider())));
        try {
            assertEquals("5 seconds1 HOURSok1ok2p2true", new Main(Command.class).exec("test", "p", "2"));
        } finally {
            Environment.ENVIRONMENT_THREAD_LOCAL.remove();
        }
    }

    @Test
    public void helpHidesTheseInternalParams() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream err = new PrintStream(out);
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment(
            Collections.<Class<?>, Object>singletonMap(RoleProvider.class, new CustomRoleProvider())) {
            @Override
            public PrintStream getError() {
                return err;
            }
        });
        try {
            new Main(new SystemPropertiesDefaultsContext(), Command.class).exec("test", "p", "not a number so will make failling and printing the cmd");
        } catch (final IllegalArgumentException iae) {
            // we expect this one actually
        }
        Environment.ENVIRONMENT_THREAD_LOCAL.remove();

        final String errorOutput = new String(out.toByteArray());
        assertFalse(errorOutput, errorOutput.contains("InputStream"));
        assertFalse(errorOutput, errorOutput.contains("Environment"));
        assertFalse(errorOutput, errorOutput.contains("RoleProvider"));
    }

    public static class Command {
        @org.tomitribe.crest.api.Command
        public String test(
            RoleProvider provider1,
            @Option("o1") @Default("5 seconds") final String o1,
            @Option("o2") @Default("1 hour") final Duration duration,
            @Err PrintStream err,
            @Out PrintStream out,
            RoleProvider provider2,
            final String val1, RoleProvider provider3, final int val2) {
            return o1 + duration.toString() + (err != null ? "ok1" : "ko") + (out != null ? "ok2" : "ko") + val1 + val2
                + Boolean.toString(provider1 != null && provider2 != null && provider3 != null);
        }
    }

    public static class CustomRoleProvider implements RoleProvider {
        @Override
        public boolean hasRole(final String role) {
            return false;
        }
    }
}
