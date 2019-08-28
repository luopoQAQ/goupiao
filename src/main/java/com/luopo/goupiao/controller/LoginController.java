package com.luopo.goupiao.controller;

import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.service.UserService;
import com.luopo.goupiao.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("")
public class LoginController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

//    @RequestMapping("/toLogin")
//    public String toLogin() {
//        return "/pages/login.htm";
//    }

    @RequestMapping("/doLogin")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response,
                                  @Valid LoginVo loginVo) {
        String token = userService.login(response, loginVo);
        return Result.success(token);
    }
}
