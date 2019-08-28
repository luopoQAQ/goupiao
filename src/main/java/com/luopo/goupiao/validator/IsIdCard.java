package com.luopo.goupiao.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsIdCardValidator.class })
public @interface IsIdCard {

    boolean required() default true;

    String message() default "身份证号码格式错误";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

