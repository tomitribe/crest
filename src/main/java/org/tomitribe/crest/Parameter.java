/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
* @version $Revision$ $Date$
*/
public class Parameter implements AnnotatedElement {

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
        for (Annotation annotation : annotations) {
            if (annotationClass.equals(annotation.annotationType())) return (T) annotation;
        }
        return null;
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
