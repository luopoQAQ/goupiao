package com.luopo.goupiao.exception;

import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//全局的异常映射处理
@ControllerAdvice //只能捕获controller抛出的异常，使用需要自顶一个MyException用来捕获所有异常
@ResponseBody   //拦截异常后直接通过json返回数据给客户端
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();

        if (e instanceof GlobalException) { //自定义全局异常
            GlobalException globalException = (GlobalException)e;
            return Result.error(globalException.getCm());

        }else if (e instanceof BindException) { //参数校验异常(自定义校验注解的不合法异常也会在这里抛出)
//            System.out.println("    进入绑定异常捕获");
            BindException bindException = (BindException)e;
            List<ObjectError> errors = bindException.getAllErrors();

            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else { //未知异常
            return Result.error(CodeMsg.SERVER_ERROR);

        }
    }


}
