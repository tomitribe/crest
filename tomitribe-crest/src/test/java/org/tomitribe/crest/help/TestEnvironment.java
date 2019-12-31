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
import java.util.Map;
import java.util.Properties;

public class TestEnvironment implements Environment {

    private final PrintString out = new PrintString();
    private final PrintString err = new PrintString();
    private final Properties properties = new Properties();
    private final HashMap<String, String> env = new HashMap<>();
    private final InputStream in;

    public TestEnvironment() {
        in = new ByteArrayInputStream(new byte[0]);
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

    public TestEnvironment env(final String name, final String value) {
        this.env.put(name, value);
        return this;
    }

    public TestEnvironment property(final String name, final String value) {
        this.properties.put(name, value);
        return this;
    }
}
