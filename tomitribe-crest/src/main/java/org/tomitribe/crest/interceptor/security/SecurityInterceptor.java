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

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.environments.Environment;

import javax.annotation.security.RolesAllowed;

public class SecurityInterceptor {
    @CrestInterceptor
    public Object secure(final CrestContext crestContext) {
        final RolesAllowed rolesAllowed = crestContext.getMethod().getAnnotation(RolesAllowed.class);
        if (rolesAllowed != null) {
            final RoleProvider provider = Environment.ENVIRONMENT_THREAD_LOCAL.get().findService(RoleProvider.class);
            if (provider == null) {
                throw new IllegalStateException("No RoleProvider registered, security interceptor can't work.");
            }
            for (final String role : rolesAllowed.value()) {
                if (provider.hasRole(role)) {
                    return crestContext.proceed();
                }
            }
            throw new IllegalArgumentException("User is not allowed to perform this operation");
        }
        return crestContext.proceed();
    }
}
