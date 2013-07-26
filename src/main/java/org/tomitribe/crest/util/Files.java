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
package org.tomitribe.crest.util;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class Files {

    public static File file(String... parts) {
        File dir = null;
        for (String part : parts) {
            if (dir == null) {
                dir = new File(part);
            } else {
                dir = new File(dir, part);
            }
        }

        return dir;
    }

    public static File file(File dir, String... parts) {
        for (String part : parts) {
            dir = new File(dir, part);
        }

        return dir;
    }

    public static List<File> collect(final File dir, final String regex) {
        return collect(dir, Pattern.compile(regex));
    }

    public static List<File> collect(final File dir, final Pattern pattern) {
        return collect(dir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                return pattern.matcher(file.getAbsolutePath()).matches();
            }
        });
    }

    public static boolean visit(final File dir, final String regex, Visitor visitor) {
        return visit(dir, Pattern.compile(regex), visitor);
    }

    public static boolean visit(final File dir, final Pattern pattern, Visitor visitor) {
        return visit(dir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                return pattern.matcher(file.getAbsolutePath()).matches();
            }
        }, visitor);
    }


    public static List<File> collect(File dir, FileFilter filter) {
        final List<File> accepted = new ArrayList<File>();
        if (filter.accept(dir)) accepted.add(dir);

        final File[] files = dir.listFiles();
        if (files != null) for (File file : files) {
            accepted.addAll(collect(file, filter));
        }

        return accepted;
    }

    public static interface Visitor {
        public boolean visit(File file);
    }

    public static boolean visit(File dir, FileFilter filter, Visitor visitor) {
        if (!filter.accept(dir)) return false;

        if (dir.isFile()) {
            visitor.visit(dir);
        }
        final File[] files = dir.listFiles();
        if (files != null) for (File file : files) {
            final boolean visit = visit(file, filter, visitor);
            if (!visit) return false;
        }

        return true;
    }

    public static void exists(File file, String s) {
        if (!file.exists()) throw new IllegalStateException(s + " does not exist: " + file.getAbsolutePath());
    }

    public static void exists(File file) {
        exists(file, "File");
    }

    public static void dir(File file) {
        if (!file.isDirectory()) throw new IllegalStateException("Not a directory: " + file.getAbsolutePath());
    }

    public static void file(File file) {
        if (!file.isFile()) throw new IllegalStateException("Not a file: " + file.getAbsolutePath());
    }

    public static void writable(File file) {
        if (!file.canWrite()) throw new IllegalStateException("Not writable: " + file.getAbsolutePath());
    }

    public static void readable(File file) {
        if (!file.canRead()) throw new IllegalStateException("Not readable: " + file.getAbsolutePath());
    }

    public static File rename(File from, File to) {
        if (!from.renameTo(to)) throw new IllegalStateException("Could not rename " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
        return to;
    }

    public static void remove(File file) {
        if (file == null) return;
        if (!file.exists()) return;

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                remove(child);
            }
        }
        if (!file.delete()) {
            throw new IllegalStateException("Could not delete file: " + file.getAbsolutePath());
        }
    }

    public static void mkdir(File file) {
        if (file.exists()) {
            dir(file);
            return;
        }
        if (!file.mkdir()) throw new RuntimeException("Cannot mkdir: " + file.getAbsolutePath());
    }

    public static File tmpdir() {
        try {
            final File file = File.createTempFile("temp", "dir");
            if (!file.delete()) throw new IllegalStateException("Cannot make temp dir.  Delete failed");
            mkdir(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mkparent(File file) {
        mkdirs(file.getParentFile());
    }

    public static File mkparent(File dir, String... parts) {
        final File file = file(dir, parts);
        mkparent(file);
        return file;
    }

    public static File mkdirs(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) throw new RuntimeException("Cannot mkdirs: " + file.getAbsolutePath());
        } else {
            dir(file);
        }

        return file;
    }

    public static File resolve(File absolutePath, File path) {
        if (path == null) throw new IllegalArgumentException("path is null");
        if (path.isAbsolute()) return path;

        if (absolutePath == null) throw new IllegalArgumentException("absolutePath is null");
        absolute(absolutePath);

        return new File(absolutePath, path.getPath());
    }

    public static void absolute(File path) {
        if (!path.isAbsolute()) throw new IllegalArgumentException("absolutePath is not absolute: " + path.getPath());
    }

    public static String format(double size) {
        if (size < 1024) return String.format("%.0f B", size);
        if ((size /= 1024) < 1024) return String.format("%.0f KB", size);
        if ((size /= 1024) < 1024) return String.format("%.0f MB", size);
        if ((size /= 1024) < 1024) return String.format("%.1f GB", size);
        if ((size /= 1024) < 1024) return String.format("%.1f TB", size);

        return "unknown";
    }
}