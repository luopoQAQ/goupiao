package com.luopo.goupiao.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsDateValidator.class })     //自定义校验
public @interface IsDate {

    boolean required() default true;

    String message() default "您所查询的日期超出售卖范围(明日至未来30日内)";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
