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
package org.tomitribe.crest;

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.util.JarLocation;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author David Blevins
 */
public class Main {

    public static Map<String, Cmd> commands = new HashMap<String, Cmd>();

    static {
        try {
            final File file = JarLocation.jarLocation(Main.class);
            final AnnotationFinder finder = new AnnotationFinder(ClasspathArchive.archive(Main.class.getClassLoader(), file.toURI().toURL()));

            for (Method method : finder.findAnnotatedMethods(Command.class)) {
                final Cmd cmd = new Cmd(method);
                commands.put(cmd.getName(), cmd);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Throwable {
        final List<String> list = new ArrayList<String>();

        final String command = (list.size() == 0) ? "help" : list.remove(0);
        args = list.toArray(new String[list.size()]);

        final Cmd cmd = commands.get(command);

        if (cmd == null) {
            System.err.println("Unknown command: " + command);
            System.err.println();
            Help.main(args);
            return;
        }

        cmd.exec(args);
    }

    public static void add(Class<?> clazz) {
        commands.putAll(Cmd.get(clazz));
    }

    public static <A extends Annotation> A get(Annotation[] annotations, Class<A> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.equals(annotation.annotationType())) return (A) annotation;
        }
        return null;
    }

    public static class Reflection {

        public static Iterable<Parameter> params(final Method method) {
            return new Iterable<Parameter>() {
                @Override
                public Iterator<Parameter> iterator() {
                    return new Iterator<Parameter>() {
                        private int index = 0;

                        @Override
                        public boolean hasNext() {
                            return index < method.getParameterTypes().length;
                        }

                        @Override
                        public Parameter next() {
                            if (!hasNext()) throw new NoSuchElementException();
                            return new Parameter(method.getParameterAnnotations()[index], method.getParameterTypes()[index++]);
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }

        public static Iterable<Parameter> params(final Constructor constructor) {
            return new Iterable<Parameter>() {
                @Override
                public Iterator<Parameter> iterator() {
                    return new Iterator<Parameter>() {
                        private int index = 0;

                        @Override
                        public boolean hasNext() {
                            return index < constructor.getParameterTypes().length;
                        }

                        @Override
                        public Parameter next() {
                            if (!hasNext()) throw new NoSuchElementException();
                            return new Parameter(constructor.getParameterAnnotations()[index], constructor.getParameterTypes()[index++]);
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
    }

    public static class Parameter implements AnnotatedElement {

        private final Annotation[] annotations;
        private final Class<?> type;

        public Parameter(Annotation[] annotations, Class<?> type) {
            this.annotations = annotations;
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return getAnnotation(annotationClass) != null;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return get(annotations, annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return annotations;
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return getAnnotations();
        }
    }

}
