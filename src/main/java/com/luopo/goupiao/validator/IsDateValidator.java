package com.luopo.goupiao.validator;

import com.luopo.goupiao.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//校验类需要实现ConstraintValidator接口
//接口使用了泛型，需要指定两个参数，第一个自定义注解类，第二个为需要校验的数据类型
//实现接口后要override两个方法，分别为initialize方法和isValid方法
//其中initialize为初始化方法，可以在里面做一些初始化操作，isValid方法就是我们最终需要的校验方法
//在valid校验中，如果校验不通过，会产生BindException异常
//捕捉到异常后可以获取到defaultMessage也就是自定义注解中定义的内容

public class IsDateValidator implements ConstraintValidator<IsDate, String> {
    private boolean required = false;

    public void initialize(IsDate constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    public boolean isValid(String dateSqlStr, ConstraintValidatorContext context) {
        if(required) {
            return ValidatorUtil.isDate(dateSqlStr);
        }else {
            if(StringUtils.isEmpty(dateSqlStr)) {
                return true;
            }else {
                return ValidatorUtil.isDate(dateSqlStr);
            }
        }
    }
}