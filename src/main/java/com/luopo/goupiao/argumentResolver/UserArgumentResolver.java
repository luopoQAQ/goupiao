package com.luopo.goupiao.argumentResolver;

import com.luopo.goupiao.access.UserContext;
import com.luopo.goupiao.pojo.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//处理方法参数解析器，用于解析User参数，并将其添加至参数解析器中

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        //得到参数类型，判断是否是目标参数User

        return clazz == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return UserContext.getUser();   //从线程本地变量中得到该访问被拦截时此服务器set的现成本地变量
                                        //获取User对象
    }
}
