/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;


import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.util.Converter;
import org.tomitribe.crest.util.Join;
import org.tomitribe.crest.util.ObjectMap;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Cmd {

    private final Object bean;
    private final Method method;
    private final String name;

    public Cmd(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
        this.name = name(method);
    }

    private static String name(Method method) {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) return method.getName();
        return value(command.value(), method.getName());
    }

    public Cmd(Method method) {
        this(null, method);
    }

    public String getUsage() {
        final String usage = usage();

        if (usage != null) {
            if (!usage.startsWith(name)) {
                return name + " " + usage;
            } else {
                return usage;
            }
        }

        final List<Object> args = new ArrayList<Object>();

        for (Parameter parameter : Reflection.params(method)) {
            if (parameter.getAnnotation(Option.class) != null) {
                continue;
            }
            args.add(parameter.getType().getSimpleName());
        }

        return String.format("%s %s %s", name, args.size() == method.getParameterTypes().length ? "" : "[options]", Join.join(" ", args));
    }

    private String usage() {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) return null;
        if ("".equals(command.usage())) return null;
        return command.usage();
    }

    public static Map<String, Cmd> get(Class<?> clazz) {
        Map<String, Cmd> map = new HashMap<String, Cmd>();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                final Cmd cmd = new Cmd(method);
                map.put(cmd.getName(), cmd);
            }
        }
        return map;
    }

    public String getName() {
        return name;
    }

    public Object exec(String... rawArgs) {
        validate();

        final List<Object> args;
        try {
            args = parseArgs(rawArgs);
        } catch (Exception e) {
            help(System.err);
            throw new IllegalArgumentException(e);
        }

        try {
            final Object[] array = args.toArray();
            method.getName();
            return method.invoke(bean, array);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void validate() {
        if (bean == null && !Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("Method not static : " + method.getName());
        }
    }

    private void help(PrintStream out) {
        out.println();
        out.print("Usage: ");
        out.println(getUsage());
        out.println();
        out.println("Options: ");
        out.printf("   %-20s   %s%n", "", "(default)");

        for (Map.Entry<String, String> entry : getOptions().entrySet()) {
            if (entry instanceof ObjectMap.Member) {
                ObjectMap.Member<String, String> member = (ObjectMap.Member<String, String>) entry;
                out.printf("   --%-20s %s%n", entry.getKey() + "=<" + member.getType().getSimpleName() + ">", entry.getValue());
            } else {
                out.printf("   --%-20s %s%n", entry.getKey(), entry.getValue());
            }
        }
    }

    private List<Object> parseArgs(String... rawArgs) {
        final List<String> list = new ArrayList<String>();

        final Map<String, String> options = getOptions();

        final List<String> invalid = new ArrayList<String>();

        // Read in and apply the options specified on the command line
        for (String arg : rawArgs) {
            if (arg.startsWith("--")) {

                final String name;
                final String value;

                if (arg.indexOf("=") > 0) {
                    name = arg.substring(arg.indexOf("--") + 2, arg.indexOf("="));
                    value = arg.substring(arg.indexOf("=") + 1);
                } else {
                    name = arg.substring(arg.indexOf("--") + 2);
                    value = "true";
                }

                if (options.containsKey(name)) {
                    options.put(name, value);
                } else {
                    invalid.add(name);
                }
            } else {
                list.add(arg);
            }
        }

        final List<Object> args = new ArrayList<Object>();

        for (Parameter parameter : Reflection.params(method)) {
            final Option option = parameter.getAnnotation(Option.class);
            if (option != null) {
                final String value = options.remove(option.value());
                args.add(Converter.convert(value, parameter.getType(), option.value()));
            } else if (list.size() > 0) {
                final String value = list.remove(0);
                args.add(Converter.convert(value, parameter.getType(), "[" + parameter.getType().getSimpleName() + "]"));
            } else {
                throw new IllegalArgumentException("Missing argument [" + parameter.getType().getSimpleName() + "]");
            }
        }

        if (list.size() > 0) {
            throw new IllegalArgumentException("Excess arguments: " + Join.join(", ", list));
        }

        if (options.size() > 0) {
            throw new IllegalArgumentException("Unknown arguments: " + Join.join(", ", new Join.NameCallback() {
                @Override
                public String getName(Object object) {
                    return "--" + object;
                }
            }, options.keySet()));
        }
        return args;
    }

    public Map<String, String> getOptions() {
        final Map<String, String> options = new HashMap<String, String>();

        for (Parameter parameter : Reflection.params(method)) {
            final Option option = parameter.getAnnotation(Option.class);

            if (option == null) continue;

            final Default def = parameter.getAnnotation(Default.class);

            options.put(option.value(), def == null ? null : def.value());
        }

        return options;
    }


    public static String value(String value, String defaultValue) {
        return value == null || value.length() == 0 ? defaultValue : value;
    }

}
