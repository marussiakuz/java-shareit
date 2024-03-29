package ru.practicum.shareit.booking.validator;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ValueOfEnumOrNullValidator.class)
public @interface ValueOfEnum {
    boolean isNullEnabled() default false;
    Class<? extends Enum<?>> enumClass();
    String message() default "Unknown state: UNSUPPORTED_STATUS";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
