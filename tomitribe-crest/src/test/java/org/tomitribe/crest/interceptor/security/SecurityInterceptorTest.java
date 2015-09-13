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
package org.tomitribe.crest.interceptor.security;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;

import javax.annotation.security.RolesAllowed;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SecurityInterceptorTest {
    @Test
    public void ok() throws Exception {
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment(Collections.<Class<?>, Object>singletonMap(RoleProvider.class, new RoleProvider() {
            @Override
            public boolean hasRole(final String role) {
                return "test".equals(role);
            }
        })));
        try {
            assertEquals("ok", new Main(MyCmd.class, SecurityInterceptor.class).exec("val"));
        } finally {
            Environment.ENVIRONMENT_THREAD_LOCAL.remove(); // we hacked the default env so let clear it
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ko() throws Exception {
        Environment.ENVIRONMENT_THREAD_LOCAL.set(new SystemEnvironment(Collections.<Class<?>, Object>singletonMap(RoleProvider.class, new RoleProvider() {
            @Override
            public boolean hasRole(final String role) {
                return !"test".equals(role);
            }
        })));
        try {
            new Main(MyCmd.class, SecurityInterceptor.class).exec("val");
        } finally {
            Environment.ENVIRONMENT_THREAD_LOCAL.remove(); // we hacked the default env so let clear it
        }
    }

    public static class MyCmd {
        @Command(interceptedBy = SecurityInterceptor.class)
        @RolesAllowed("test")
        public static String val() {
            return "ok";
        }
    }
}
