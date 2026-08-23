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

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.ElementFilter;

/**
 * Translates the inline javadoc tags of a doc comment into the plain text
 * a man page shows, at capture time in the annotation processor.
 *
 * <ul>
 *   <li>{@code {@code X}} and {@code {@literal X}} become X</li>
 *   <li>{@code {@link Foo#bar(Baz)}} and {@code {@linkplain ...}} become
 *       the label when one is given, otherwise the shortened member
 *       reference (Foo.bar)</li>
 *   <li>{@code {@value Foo#CONSTANT}} becomes the constant's value where
 *       the APT Elements API can resolve it, the shortened reference text
 *       otherwise</li>
 *   <li>any other tag — unknown today or invented tomorrow — drops its
 *       braces and keeps its content, so a raw '{@' can never reach the
 *       rendered document</li>
 * </ul>
 */
public class JavadocInlineTags {

    private final Elements elements;
    private final TypeElement enclosing;

    private JavadocInlineTags(final Elements elements, final TypeElement enclosing) {
        this.elements = elements;
        this.enclosing = enclosing;
    }

    /**
     * Translates every inline tag in the doc comment.  The elements and
     * enclosing type give {@code {@value}} a chance to resolve constants;
     * either may be null, degrading to reference text.
     */
    public static String translate(final String javadoc, final Elements elements, final TypeElement enclosing) {
        if (javadoc == null) return null;
        return new JavadocInlineTags(elements, enclosing).translate(javadoc);
    }

    private String translate(final String text) {
        final StringBuilder out = new StringBuilder();

        int i = 0;
        while (i < text.length()) {
            final int start = text.indexOf("{@", i);

            if (start < 0) {
                out.append(text, i, text.length());
                break;
            }

            out.append(text, i, start);

            int j = start + 2;
            while (j < text.length() && Character.isLetterOrDigit(text.charAt(j))) {
                j++;
            }
            final String name = text.substring(start + 2, j);

            /*
             * Find the matching close brace, tracking nesting so
             * {@code {"key": "value"}} keeps its inner braces.
             */
            int depth = 1;
            int k = j;
            while (k < text.length() && depth > 0) {
                final char c = text.charAt(k);
                if (c == '{') depth++;
                if (c == '}') depth--;
                k++;
            }

            final String content;
            if (depth == 0) {
                content = text.substring(j, k - 1);
                i = k;
            } else {
                // Unclosed tag: everything to the end is its content
                content = text.substring(j);
                i = text.length();
            }

            out.append(translateTag(name, translate(content).trim()));
        }

        return out.toString();
    }

    private String translateTag(final String name, final String content) {
        switch (name) {
            case "code":
            case "literal":
                return content;
            case "link":
            case "linkplain":
                return link(content);
            case "value":
                return value(content);
            default:
                // The safety rule: unknown tags drop their braces and keep
                // their content, so '{@' is unrepresentable in output
                return content;
        }
    }

    /**
     * The label when one is given, the shortened member reference otherwise
     */
    private String link(final String content) {
        final int space = content.indexOf(' ');

        if (space > 0) {
            return content.substring(space + 1).trim();
        }

        return shorten(content);
    }

    /**
     * Turns a javadoc reference like {@code com.example.Foo#bar(Baz)} into
     * the short form a reader says out loud: {@code Foo.bar}
     */
    private static String shorten(final String reference) {
        String ref = reference;

        final int args = ref.indexOf('(');
        if (args >= 0) {
            ref = ref.substring(0, args);
        }

        final int hash = ref.indexOf('#');
        final String type = hash < 0 ? ref : ref.substring(0, hash);
        final String member = hash < 0 ? "" : ref.substring(hash + 1);

        final String simpleType = type.substring(type.lastIndexOf('.') + 1);

        if (member.isEmpty()) return simpleType;
        if (simpleType.isEmpty()) return member;
        return simpleType + "." + member;
    }

    /**
     * The referenced constant's value where the Elements API can resolve
     * it, the shortened reference text otherwise
     */
    private String value(final String content) {
        if (content.isEmpty()) return "";

        final String resolved = resolve(content);

        return resolved != null ? resolved : shorten(content);
    }

    private String resolve(final String reference) {
        if (elements == null) return null;

        final int hash = reference.indexOf('#');
        final String typePart = hash < 0 ? reference : reference.substring(0, hash);
        final String fieldPart = hash < 0 ? "" : reference.substring(hash + 1);

        if (fieldPart.isEmpty()) return null;

        final TypeElement type = resolveType(typePart);
        if (type == null) return null;

        for (final VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (!field.getSimpleName().contentEquals(fieldPart)) continue;

            final Object constant = field.getConstantValue();
            return constant == null ? null : String.valueOf(constant);
        }

        return null;
    }

    /**
     * Resolves the type part of a reference the way a reader would: empty
     * means the documented class itself; otherwise try the name as
     * written, then relative to each enclosing scope, then java.lang
     */
    private TypeElement resolveType(final String typePart) {
        if (typePart.isEmpty()) return enclosing;

        final TypeElement direct = elements.getTypeElement(typePart);
        if (direct != null) return direct;

        for (Element scope = enclosing; scope != null; scope = scope.getEnclosingElement()) {
            final String prefix;
            if (scope instanceof TypeElement) {
                prefix = ((TypeElement) scope).getQualifiedName().toString();
            } else if (scope instanceof PackageElement) {
                prefix = ((PackageElement) scope).getQualifiedName().toString();
            } else {
                continue;
            }

            final TypeElement relative = elements.getTypeElement(prefix + "." + typePart);
            if (relative != null) return relative;
        }

        return elements.getTypeElement("java.lang." + typePart);
    }
}
