/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest.help;

import org.tomitribe.crest.environments.Environment;
import org.tomitribe.util.PrintString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestEnvironment implements Environment {

    private final PrintString out;
    private final PrintString err;
    private final Properties properties;
    private final Map<String, String> env;
    private final InputStream in;
    private final String name;
    private final String version;
    private final List<Object> globalOptions = new CopyOnWriteArrayList<>();

    public TestEnvironment(final PrintString out,
                           final PrintString err,
                           final Properties properties,
                           final Map<String, String> env,
                           final InputStream in,
                           final String name,
                           final String version) {
        this.out = out;
        this.err = err;
        this.properties = properties;
        this.env = env;
        this.in = in;
        this.name = name;
        this.version = version;
    }

    public PrintString getOut() {
        return out;
    }

    public PrintString getErr() {
        return err;
    }

    public InputStream getIn() {
        return in;
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

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Map<String, String> getEnv() {
        return env;
    }

    @Override
    public <T> T findService(final Class<T> type) {
        return null;
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
    public List<Object> getGlobalOptions() {
        return globalOptions;
    }

    @Override
    public void setGlobalOptions(final List<Object> objects) {
        this.globalOptions.addAll(objects);
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private PrintString out = new PrintString();
        private PrintString err = new PrintString();
        private Properties properties = new Properties();
        private Map<String, String> env = new HashMap<>();
        private InputStream in;
        private String name;
        private String version;

        private Builder() {
            in = new ByteArrayInputStream(new byte[0]);
            env("NOCOLOR", "");
            env("NOLESS", "");
        }


        public Builder env(final String name, final String value) {
            this.env.put(name, value);
            return this;
        }

        public Builder property(final String name, final String value) {
            this.properties.put(name, value);
            return this;
        }

        public Builder out(PrintString out) {
            this.out = out;
            return this;
        }

        public Builder err(PrintString err) {
            this.err = err;
            return this;
        }

        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder env(Map<String, String> env) {
            this.env = env;
            return this;
        }

        public Builder in(InputStream in) {
            this.in = in;
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

        public TestEnvironment build() {

            return new TestEnvironment(out, err, properties, env, in, name, version);
        }
    }
}
