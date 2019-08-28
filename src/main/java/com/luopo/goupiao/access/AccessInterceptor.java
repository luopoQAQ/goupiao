package com.luopo.goupiao.access;

import com.alibaba.fastjson.JSON;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.AccessKey;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {  //获取handler，从中得到拦截的相关数据
            //根据cookie携带的token得到user，并存储在线程本地变量中，用于给后面的参数解析器进行解析
            //存储在线程本地变量而不是共有变量中，可以在多线程情况下保证数据私有不被篡改
            User user = getUser(request, response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod)handler;

            //获取被拦截的方法的注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin) {
                if(user == null) {
                    render(response, CodeMsg.NOT_LOGIN);    //如果没有用户信息（未登录）
                                                            // 则提交未登录的CodeMsg
                    return false;
                }
            }

            if (seconds == -1 && maxCount == -1) {
                return true;
            }
            else {
                AccessKey accessKey = AccessKey.accessWithLimit(seconds);
                Integer num = redisService.get(accessKey, key + "_" + user.getUserId(), Integer.class);
                if(num == null) {
                    redisService.set(accessKey, key + "_" + user.getUserId(), 1);
                }else if(num < maxCount) {
                    redisService.incr(accessKey,    //redis的自增1操作是原子的，即线程安全的哦
                            key + "_" + user.getUserId());
                }else {
                    render(response, CodeMsg.ACCESS_FREQUENT); //超过最大访问次数，则提交访问太过频繁错误给客户端
                    return false;
                }
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        response.setContentType("application/json; charset=UTF-8");

        OutputStream out = response.getOutputStream();  //得到向response输出的流
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));   //写入错误信息CodeMsg转json再二进制的数据
        out.flush();    //立即返回数据
        out.close();
    }

    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(UserService.COOKIE_TOKEN_NAME);
        String cookieToken = getCookieValue(request, UserService.COOKIE_TOKEN_NAME);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String tokenName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(tokenName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

}