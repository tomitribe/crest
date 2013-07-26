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

import org.tomitribe.crest.util.ObjectMap;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Help {

    public static void main(String[] args) {
        System.out.println("Commands: ");
        System.out.printf("   %-20s", "");
        System.out.println();


        for (String command : Main.commands.keySet()) {
            System.out.printf("   %-20s%n", command);
        }
    }

    public static void help(PrintStream out, String usage, Class<?> options) {

        out.println(usage);
        options(options, System.out);
    }

    public static void options(Class<?> o, final PrintStream out) {
        out.println();
        out.println("Properties: ");
        out.printf("   %-20s %s", "", "(default)");
        out.println();

        for (Field field : o.getFields()) {
            try {
                out.printf("   %-20s %s", field.getName(), field.get(null));
                out.println();
            } catch (IllegalAccessException e) {
            }
        }
    }


    public static void printHelp(Class clazz, PrintStream out) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Map<String, Object> options = Help.getOptions(clazz);
        printHelp(clazz, options, out);
    }

    public static void printHelp(Class clazz, Map<String, Object> options, PrintStream out) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        printUsage(clazz, out);
        printOptions(options, out);
    }

    public static Map<String, Object> getOptions(Class clazz) {
        try {
            final Class<?> optionsClass = Class.forName(clazz.getName() + "$O", true, clazz.getClassLoader());
            return new ObjectMap(optionsClass);
        } catch (ClassNotFoundException e) {
            return Collections.EMPTY_MAP;
        }
    }

    private static void printOptions(Map<String, Object> options, PrintStream out) {
        out.println();
        out.println("Options: ");
        out.printf("   %-20s   %s%n", "", "(default)");

        for (Map.Entry<String, Object> entry : options.entrySet()) {
            if (entry instanceof ObjectMap.Member) {
                ObjectMap.Member<String, Object> member = (ObjectMap.Member<String, Object>) entry;
                out.printf("   --%-20s %s%n", entry.getKey() + "=<" + member.getType().getSimpleName() + ">", toString(entry));
            } else {
                out.printf("   --%-20s %s%n", entry.getKey(), toString(entry));

            }
        }
    }

    private static void printUsage(Class clazz, PrintStream out) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            final Method usage = clazz.getMethod("usage");
            out.println();
            out.print("Usage: ");
            out.println(usage.invoke(null));
        } catch (NoSuchMethodException e) {

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object toString(Map.Entry<String, Object> entry) {
        try {
            final Object value = entry.getValue();
            if (value == null) return "<none>";
            return value.toString();
        } catch (Exception e) {
            return "<unknown>";
        }
    }
}
