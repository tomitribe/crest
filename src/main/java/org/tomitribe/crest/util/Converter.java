/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest.util;


import org.tomitribe.crest.util.editor.Editors;

import java.beans.PropertyEditor;

/**
 * @version $Revision$ $Date$
 */
public class Converter {
    public static Object convert(Object value, Class<?> targetType, final String name) {
        if (value == null) return value;

        final Class<? extends Object> actualType = value.getClass();

        if (targetType.isPrimitive()) {
            targetType = boxPrimitive(targetType);
        }

        if (targetType.isAssignableFrom(actualType)) return value;

        if (Number.class.isAssignableFrom(actualType) && Number.class.isAssignableFrom(targetType)) return value;

        if (!(value instanceof String)) {
            final String message = String.format("Expected type '%s' for '%s'. Found '%s'", targetType.getName(), name, actualType.getName());
            throw new IllegalArgumentException(message);
        }

        final String stringValue = (String) value;

        if (Enum.class.isAssignableFrom(targetType)) {
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            return Enum.valueOf(enumType, stringValue.toUpperCase());
        }

        try {
            // Force static initializers to run
            Class.forName(targetType.getName(), true, targetType.getClassLoader());
        } catch (ClassNotFoundException e) {
        }

        final PropertyEditor editor = Editors.get(targetType);

        if (editor == null) {
            final String message = String.format("Cannot convert to '%s' for '%s'. No PropertyEditor", targetType.getName(), name);
            throw new IllegalArgumentException(message);
        }

        editor.setAsText(stringValue);
        return editor.getValue();
    }

    private static Class<?> boxPrimitive(Class<?> targetType) {
        if (targetType == byte.class) return Byte.class;
        if (targetType == char.class) return Character.class;
        if (targetType == short.class) return Short.class;
        if (targetType == int.class) return Integer.class;
        if (targetType == long.class) return Long.class;
        if (targetType == float.class) return Float.class;
        if (targetType == double.class) return Double.class;
        if (targetType == boolean.class) return Boolean.class;
        return targetType;
    }
}
