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
package org.tomitribe.crest.term;

import org.tomitribe.crest.table.Lines;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Unfortunately there is no way to test this in a test case.
 *
 * You need to be a user with an actual terminal session.
 */
public class Screen {

    private Screen() {
    }

    public static String exec() throws IOException {
        return exec(true, "stty", "-a");
    }

    public static int guessWidth() {
        try {
            return parseWidth(exec());
        } catch (Exception e) {
            return -1;
        }
    }

    public static int parseWidth(final String output) {
        return Stream.of(Lines.split(output))
                .filter(s -> s.contains(" columns;"))
                .map(s -> s.split(";"))
                .flatMap(Stream::of)
                .map(String::trim)
                .filter(s -> s.endsWith(" columns"))
                .map(s -> s.split("[= ]+"))
                .filter(strings -> strings.length == 2)
                .map(strings -> strings[0])
                .map(String::trim)
                .map(Integer::parseInt)
                .findFirst()
                .orElse(-1);
    }

    public static String exec(boolean redirectInput, final String... cmd) throws IOException {
        Objects.requireNonNull(cmd);
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (redirectInput) {
                pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            }
            Process p = pb.start();
            String result = waitAndCapture(p);
            if (p.exitValue() != 0) {
                if (result.endsWith("\n")) {
                    result = result.substring(0, result.length() - 1);
                }
                throw new IOException("Error executing '" + String.join(" ", (CharSequence[]) cmd) + "': " + result);
            }
            return result;
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException("Command interrupted").initCause(e);
        }
    }

    public static String waitAndCapture(Process p) throws IOException, InterruptedException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream in = null;
        InputStream err = null;
        OutputStream out = null;
        try {
            int c;
            in = p.getInputStream();
            while ((c = in.read()) != -1) {
                bout.write(c);
            }
            err = p.getErrorStream();
            while ((c = err.read()) != -1) {
                bout.write(c);
            }
            out = p.getOutputStream();
            p.waitFor();
        } finally {
            close(in, out, err);
        }

        return bout.toString();
    }

    private static void close(final Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

}
