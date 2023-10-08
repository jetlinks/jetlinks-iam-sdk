package org.jetlinks.iam.core.utils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import java.util.Set;

/**
 * 输入描述.
 *
 * @author zhangji 2023/9/22
 */
public class ValidatorUtils {

    public static  <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(object);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.iterator().next().getMessage());
        }
    }
}
