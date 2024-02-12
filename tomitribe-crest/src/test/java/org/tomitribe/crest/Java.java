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

import org.junit.Assert;
import org.tomitribe.util.Join;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

/**
 * @version $Revision$ $Date$
 */
public class Java {

    private final List<String> command = new ArrayList<>();
    private final Map<String, String> environment = new HashMap<>();

    private Java(final List<String> command, final Map<String, String> environment) {
        this.command.addAll(command);
        this.environment.putAll(environment);
    }

    public Result run(final String... args) {
        final ProcessBuilder java = javaProcess();
        try {
            java.environment().putAll(environment);
            java.command().addAll(command);
            java.command().addAll(Arrays.asList(args));
            return new Result(java.start());
        } catch (final Exception e) {
            throw new JavaExecutionException(java.command(), e);
        }
    }

    public static Result jar(final File jar, final String... args) throws IOException, ExecutionException, InterruptedException {
        final ProcessBuilder java = javaProcess();
        java.command().add("-jar");
        java.command().add(jar.getAbsolutePath());
        java.command().addAll(Arrays.asList(args));
        return new Result(java.start());
    }

    public static Result java(final String... args) throws IOException, ExecutionException, InterruptedException {
        final ProcessBuilder java = javaProcess();
        java.command().addAll(Arrays.asList(args));
        return new Result(java.start());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> command = new ArrayList<>();
        private final Map<String, String> environment = new HashMap<>();

        public Builder env(final String name, final String value) {
            environment.put(name, value);
            return this;
        }

        public Builder jar(final File jar) {
            command.add("-jar");
            command.add(jar.getAbsolutePath());
            return this;
        }

        public Builder arg(final String arg) {
            command.add(arg);
            return this;
        }

        public Builder classpath(final File... jars) {
            if (jars == null || jars.length == 0) {
                throw new IllegalArgumentException("No jars specified");
            }

            final String classpath = Stream.of(jars)
                    .map(File::getAbsolutePath)
                    .reduce((s, s2) -> s + File.pathSeparator + s2)
                    .get();

            command.add("-classpath");
            command.add(classpath);
            return this;
        }


        public Builder debug() {
            command.add("-Xdebug");
            command.add("-Xnoagent");
            command.add("-Djava.compiler=NONE");
            command.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");
            return this;
        }

        public Builder enableAssertions() {
            command.add("-ea");
            return this;
        }

        public Builder disableAssertions() {
            command.add("-da");
            return this;
        }

        public Builder ms(final String initial) {
            command.add("-Xms" + initial);
            return this;
        }

        public Builder mx(final String max) {
            command.add("-Xmx" + max);
            return this;
        }

        public Builder client() {
            command.add("-client");
            return this;
        }

        public Builder server() {
            command.add("-server");
            return this;
        }

        // Example of adding a generic method for `-XX` options
        public Builder xx(final String option, final String value) {
            command.add("-XX:" + option + "=" + value);
            return this;
        }

        public Builder agentlib(final String libName) {
            command.add("-agentlib:" + libName);
            return this;
        }

        public Builder agentlib(final String libName, final String options) {
            command.add("-agentlib:" + libName + "=" + options);
            return this;
        }


        public Builder agentpath(final String pathName) {
            command.add("-agentpath:" + pathName);
            return this;
        }

        public Builder agentpath(final String pathName, final String options) {
            command.add("-agentpath:" + pathName + "=" + options);
            return this;
        }


        public Builder javaagent(final String libName) {
            command.add("-javaagent:" + libName);
            return this;
        }

        public Builder javaagent(final String jarPath, final String options) {
            command.add("-javaagent:" + jarPath + "=" + options);
            return this;
        }

        public Builder d(final String name, final String value) {
            command.add("-D:" + name + "=" + value);
            return this;
        }

        public Builder copy() {
            final Builder copy = new Builder();
            copy.command.addAll(this.command);
            copy.environment.putAll(this.environment);
            return copy;
        }

        public Java build() {
            return new Java(command, environment);
        }
    }

    private static ProcessBuilder javaProcess() {
        final File javaHome = new File(System.getenv("JAVA_HOME"));
        File java = new File(new File(javaHome, "bin"), "java");

        if (!java.exists()) {
            java = new File(new File(javaHome, "bin"), "java.exe");
        }

        Assert.assertTrue(java.exists());
        Assert.assertTrue(java.canExecute());

        final ProcessBuilder builder = new ProcessBuilder();
        final List<String> command = builder.command();
        command.add(java.getAbsolutePath());
        return builder;
    }

    public static class Result {
        private final int exitCode;
        private final String out;
        private final String err;

        public Result(final Process process) throws InterruptedException, ExecutionException {
            final Future<Pipe> stout = Pipe.pipe(process.getInputStream(), System.out);
            final Future<Pipe> sterr = Pipe.pipe(process.getErrorStream(), System.err);

            this.exitCode = process.waitFor();
            this.out = stout.get().asString();
            this.err = sterr.get().asString();
        }

        public String getOut() {
            return out;
        }

        public String getErr() {
            return err;
        }

        public int getExitCode() {
            return exitCode;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "exitCode=" + exitCode +
                    ", out='" + out + '\'' +
                    ", err='" + err + '\'' +
                    '}';
        }
    }

    public static final class Pipe implements Runnable {

        private final InputStream in;
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final OutputStream[] cc;

        public Pipe(final InputStream in, final OutputStream... copy) {
            this.in = in;
            this.cc = copy;
        }

        public static Future<Pipe> pipe(final InputStream in, final OutputStream... copy) {
            final Pipe target = new Pipe(in, copy);

            final FutureTask<Pipe> task = new FutureTask<Pipe>(target, target);
            final Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

            return task;
        }

        public synchronized String asString() {
            return new String(out.toByteArray());
        }

        public synchronized void run() {
            try {
                int i = -1;

                final byte[] buf = new byte[1024];

                while ((i = in.read(buf)) != -1) {
                    out.write(buf, 0, i);
                    for (final OutputStream stream : cc) {
                        stream.write(buf, 0, i);
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class JavaExecutionException extends RuntimeException {
        public JavaExecutionException(final List<String> command, final Exception e) {
            super(String.format("Java command failed:%nargs:%s%nresult:%s", Join.join(" ", command), e));
        }
    }
}
