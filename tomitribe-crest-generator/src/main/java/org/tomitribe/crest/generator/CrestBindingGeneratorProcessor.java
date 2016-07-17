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
package org.tomitribe.crest.generator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes({
        "org.apache.deltaspike.core.api.config.ConfigProperty"
})
public class CrestBindingGeneratorProcessor extends AbstractProcessor {
    private String basePck;

    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        final Map<String, String> options = processingEnv.getOptions();
        basePck = options.get("crest.basePackage");
        if (basePck == null) {
            basePck = "org.tomitribe.crest.generator.generated";
        }
    }

    private final Map<String, Binding> mappings = new HashMap<>();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            if (!mappings.isEmpty()) { // avoid to redo it again and again
                return false;
            }

            processingEnv.getMessager().printMessage(NOTE, "Processing configuration annotations");
            mappings.clear();

            for (final TypeElement annotation : annotations) {
                if (annotation.getKind() != ANNOTATION_TYPE) {
                    continue;
                }

                try {
                    final Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotation);
                    for (final Element e : annotatedElements) {
                        for (final AnnotationMirror am : e.getAnnotationMirrors()) {
                            if (!DeclaredType.class.isInstance(e.asType()) ||
                                    !TypeElement.class.isInstance(DeclaredType.class.cast(e.asType()).asElement()) ||
                                    !TypeElement.class.isInstance(am.getAnnotationType().asElement())) {
                                continue;
                            }

                            final Name qualifiedName = TypeElement.class.cast(am.getAnnotationType().asElement()).getQualifiedName();
                            if (qualifiedName.contentEquals("org.apache.deltaspike.core.api.config.ConfigProperty")) {
                                processDeltaSpike(e, am);
                            }
                        }
                    }
                } catch (final Throwable t) {
                    processingEnv.getMessager().printMessage(ERROR, t.getMessage());
                }
            }


            try {
                dump(processingEnv.getFiler());
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    private void dump(final Filer filer) throws IOException {
        for (final Map.Entry<String, Binding> binding : mappings.entrySet()) {
            final Binding bindingValue = binding.getValue();
            final JavaFileObject sourceFile = filer.createSourceFile(basePck + '.' + bindingValue.className);

            try (final Writer writer = sourceFile.openWriter()) {
                writer.write("package " + basePck + ";\n\n");
                writer.write("import java.util.Collections;\n");
                writer.write("import java.util.Map;\n");
                writer.write("import java.util.HashMap;\n\n");
                writer.write("import org.apache.deltaspike.core.api.config.ConfigResolver;\n");
                writer.write("import org.apache.deltaspike.core.spi.config.ConfigSource;\n");
                if (bindingValue.hasDefault) {
                    writer.write("import org.tomitribe.crest.api.Default;\n");
                }
                writer.write("import org.tomitribe.crest.api.Option;\n\n");
                writer.write("import static java.util.Collections.singletonList;\n\n");
                writer.write("public class " + bindingValue.className + " {\n");

                for (final Map.Entry<String, Entry> e : bindingValue.entries.entrySet()) {
                    writer.write("    private " + e.getValue().type + " " + e.getValue().varName + ";\n");
                }
                writer.write("\n");

                writer.write("    public " + bindingValue.className + "(\n");

                final Iterator<Map.Entry<String, Entry>> iterator = bindingValue.entries.entrySet().iterator();
                final StringBuilder constructorBody = new StringBuilder();
                while (iterator.hasNext()) {
                    final Map.Entry<String, Entry> entry = iterator.next();
                    final Entry value = entry.getValue();
                    final String var = value.varName;
                    writer.write("        @Option(\"" + entry.getKey().replace('.', '-') + "\")");
                    if (value.defaultValue != null) {
                        writer.write(" @Default(\"" + value.defaultValue + "\")");
                    }
                    writer.write(" " + value.type + " " + var);
                    constructorBody.append("        this.").append(var).append(" = ").append(var).append(";\n");
                    constructorBody.append("        ____properties.put(\"").append(binding.getKey()).append('.').append(value.key)
                            .append("\", String.valueOf(").append(var).append("));\n");
                    if (iterator.hasNext()) {
                        writer.write(",\n");
                    } else {
                        writer.write(") {\n");
                        writer.write("        final Map<String, String> ____properties = new HashMap<>();\n");
                        writer.write(constructorBody.toString());
                    }
                }
                writer.write("        ConfigResolver.addConfigSources(Collections.<ConfigSource>singletonList(new ConfigSource() {\n" +
                        "            @Override\n" +
                        "            public int getOrdinal() {\n" +
                        "                return 0;\n" +
                        "            }\n" +
                        "\n" +
                        "            @Override\n" +
                        "            public Map<String, String> getProperties() {\n" +
                        "                return ____properties;\n" +
                        "            }\n" +
                        "\n" +
                        "            @Override\n" +
                        "            public String getPropertyValue(final String key) {\n" +
                        "                return ____properties.get(key);\n" +
                        "            }\n" +
                        "\n" +
                        "            @Override\n" +
                        "            public String getConfigName() {\n" +
                        "                return \"crest-" + binding.getKey() + "\";\n" +
                        "            }\n" +
                        "\n" +
                        "            @Override\n" +
                        "            public boolean isScannable() {\n" +
                        "                return true;\n" +
                        "            }\n" +
                        "        }));");
                // TODO: add ConfigResolver.addConfigSource(new ...)...
                writer.write("    }\n");

                for (final Map.Entry<String, Entry> e : bindingValue.entries.entrySet()) {
                    final String varName = e.getValue().varName;
                    final String methodSuffix = Character.toUpperCase(varName.charAt(0)) + varName.substring(1);
                    writer.write("\n    public " + e.getValue().type + " get" + methodSuffix + "() {\n");
                    writer.write("        return " + varName + ";\n");
                    writer.write("    }\n\n");
                    writer.write("    public void set" + methodSuffix + "(final " + e.getValue().type + " " + varName + ") {\n");
                    writer.write("        this." + varName + " = " + varName + ";\n");
                    writer.write("    }\n");
                }
                writer.write("\n");

                writer.write("}\n");
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private void processDeltaSpike(final Element e, final AnnotationMirror am) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> values = am.getElementValues();
        String name = null;
        String defaultValue = null;
        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            final Name simpleName = entry.getKey().getSimpleName();
            if (simpleName.contentEquals("name")) {
                name = entry.getValue().getValue().toString();
            } else if (simpleName.contentEquals("defaultValue")) {
                defaultValue = entry.getValue().getValue().toString();
                if ("org.apache.deltaspike.NullValueMarker".equals(defaultValue)) {
                    defaultValue = null;
                }
            }
        }
        onEntry(name, defaultValue, TypeElement.class.cast(DeclaredType.class.cast(e.asType()).asElement()).getQualifiedName().toString());
    }

    private static String toVar(final String key) {
        final StringBuilder builder = new StringBuilder(key.length());
        boolean toLower = true;
        boolean toUpper = false;
        for (int i = 0; i < key.length(); i++) {
            final char c = key.charAt(i);
            switch (c) {
                case '.':
                    toUpper = true;
                    toLower = false;
                    break;
                default:
                    if (toLower) {
                        builder.append(Character.toLowerCase(c));
                        toLower = toUpper = false;
                    } else if (toUpper) {
                        builder.append(Character.toUpperCase(c));
                        toLower = toUpper = false;
                    } else {
                        builder.append(c);
                    }
            }
        }
        return builder.toString().replace(" ", "_");
    }

    private static String toClassName(final String key) {
        return Character.toUpperCase(key.charAt(0)) + toVar(key.substring(1));
    }

    private void onEntry(final String name, final String defaultValue, final String type) {
        final int sep = name.indexOf('.');
        if (sep < 0) {
            return;
        }
        final String subName = name.substring(sep + 1);
        final String key = name.substring(0, sep);
        Binding binding = mappings.get(key);
        if (binding == null) {
            binding = new Binding(toClassName(key));
            mappings.put(key, binding);
        }
        if (!binding.hasDefault && defaultValue != null) {
            binding.hasDefault = true;
        }
        binding.entries.put(subName, new Entry(defaultValue, type, subName));
    }

    @Override
    public Set<String> getSupportedOptions() {
        return singleton(SourceVersion.latestSupported().name());
    }

    private static final class Binding {
        private boolean hasDefault = false;
        private final String className;
        private final Map<String, Entry> entries = new HashMap<>();

        private Binding(final String className) {
            this.className = className;
        }
    }

    private static final class Entry {
        private final String defaultValue;
        private final String type;
        private final String varName;
        private final String key;

        private Entry(final String defaultValue, final String type, final String key) {
            this.defaultValue = defaultValue;
            this.type = type.replace("java.lang.", "");
            this.varName = toVar(key);
            this.key = key;
        }
    }
}
