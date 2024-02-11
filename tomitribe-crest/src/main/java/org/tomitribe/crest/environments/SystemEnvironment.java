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
package org.tomitribe.crest.environments;


import org.tomitribe.crest.val.BeanValidation;
import org.tomitribe.crest.val.BeanValidationImpl;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SystemEnvironment implements Environment {
    private final Map<Class<?>, Object> services;
    private final PrintStream out;
    private final PrintStream err;
    private final InputStream in;
    private final Properties properties;
    private final String name;
    private final String version;

    public SystemEnvironment(final Map<Class<?>, Object> services) {
        this(services, System.out, System.err, System.in, System.getProperties(), null, null);
    }

    protected SystemEnvironment(final Map<Class<?>, Object> services,
                                final PrintStream out,
                                final PrintStream err,
                                final InputStream in,
                                final Properties properties, final String name, final String version) {
        this.services = new HashMap<>(services);
        this.out = out;
        this.err = err;
        this.in = in;
        this.properties = properties;
        this.name = name;
        this.version = version;
        init();
    }

    public SystemEnvironment() {
        this(new HashMap<>());
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public String getCommandVersion() {
        return version;
    }

    @Override
    public PrintStream getOutput() {
        return out;
    }

    @Override
    public PrintStream getError() {
        return err;
    }

    @Override
    public InputStream getInput() {
        return in;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public <T> T findService(Class<T> type) {
        return type.cast(services.get(type));
    }

    protected void init() {
        services.put(BeanValidationImpl.class, BeanValidation.create(this::findService));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private String version;
        private Map<Class<?>, Object> services = new HashMap<>();
        private PrintStream out;
        private PrintStream err;
        private InputStream in;
        private Properties properties;

        private Builder() {
        }


        public Builder services(Map<Class<?>, Object> services) {
            this.services = services;
            return this;
        }

        public Builder service(Class<?> type, Object service) {
            this.services.put(type, service);
            return this;
        }

        public Builder out(PrintStream out) {
            this.out = out;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder err(PrintStream err) {
            this.err = err;
            return this;
        }

        public Builder in(InputStream in) {
            this.in = in;
            return this;
        }

        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public SystemEnvironment build() {
            return new SystemEnvironment(services, out, err, in, properties, name, version);
        }
    }
}
