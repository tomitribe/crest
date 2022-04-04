package $package;

import org.tomitribe.crest.val.Exists;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
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

@Exists
@Documented
@javax.validation.Constraint(validatedBy = {IsFile.Constraint.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface IsFile {
    Class<?>[] groups() default {};

    String message() default "{$groupId.IsFile.message}";

    Class<? extends Payload>[] payload() default {};

    public static class Constraint implements ConstraintValidator<IsFile, File> {

        @Override
        public void initialize(IsFile constraintAnnotation) {
        }

        @Override
        public boolean isValid(File file, ConstraintValidatorContext context) {
            return file.isFile();
        }
    }
}
