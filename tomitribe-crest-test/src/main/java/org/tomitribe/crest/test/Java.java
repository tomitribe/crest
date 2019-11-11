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
package org.tomitribe.crest.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @version $Revision$ $Date$
 */
public class Java {

    public static Result java(final String... args) {
        try {
            final ProcessBuilder java = javaProcess();
            java.command().addAll(Arrays.asList(args));
            return new Result(java.start());
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ProcessBuilder javaProcess() {
        final File javaHome = new File(System.getenv("JAVA_HOME"));
        File java = new File(new File(javaHome, "bin"), "java");

        if (!java.exists()) {
            java = new File(new File(javaHome, "bin"), "java.exe");
        }

        if (!java.exists()) throw new IllegalStateException("No java executable found");
        if (!java.canExecute()) throw new IllegalStateException("Not executable: " + java.getAbsolutePath());

        final ProcessBuilder builder = new ProcessBuilder();
        final List<String> command = builder.command();
        command.add(java.getAbsolutePath());
        return builder;
    }

    public static class Result {
        private final String out;
        private final String err;
        private final int exitCode;

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
}
