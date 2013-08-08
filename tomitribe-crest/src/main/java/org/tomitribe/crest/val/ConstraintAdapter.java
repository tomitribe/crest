/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest.val;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
* @version $Revision$ $Date$
*/
public class ConstraintAdapter<A extends Annotation, T> implements ConstraintValidator<A, T> {
    @Override
    public void initialize(A constraintAnnotation) {
    }

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        return isValid(value);
    }

    public boolean isValid(T value) {
        return false;
    }
}
