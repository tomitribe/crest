/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest.val;

import javax.validation.Payload;
import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@javax.validation.Constraint(validatedBy = {Exists.Constraint.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface Exists {
    Class<?>[] groups() default {};

    String message() default "{org.tomitribe.crest.val.Exists.message}";

    Class<? extends Payload>[] payload() default {};

    public static class Constraint extends ConstraintAdapter<Exists, File> {
        @Override
        public boolean isValid(File value) {
            return value.exists();
        }
    }
}