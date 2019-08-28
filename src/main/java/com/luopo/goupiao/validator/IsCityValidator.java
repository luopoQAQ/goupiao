package com.luopo.goupiao.validator;

import com.luopo.goupiao.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsCityValidator implements ConstraintValidator<IsCity, String> {

    private boolean required = false;

    public void initialize(IsCity constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required) {
            return ValidatorUtil.isCity(value);
        }else {
            if(StringUtils.isEmpty(value)) {
                return true;
            }else {
                return ValidatorUtil.isCity(value);
            }
        }
    }

}