package com.luopo.goupiao.controller;

import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.service.UserService;
import com.luopo.goupiao.util.MD5Util;
import com.luopo.goupiao.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Configuration
@RequestMapping("")
public class RegisterController {

    @Autowired
    UserService userService;

    @PostMapping("/doRegister")
    @ResponseBody
    public Result<String> doRegister(HttpServletRequest request, HttpServletResponse response,
                                     Model model, @Valid RegisterVo registerVo ) {

        User user = userService.getByUserName(registerVo.getUserName());

        if (null != user) {
            return Result.error(CodeMsg.USER_NAME_HAS_EXIST);
        }

        user = new User();
        user.setUserName(registerVo.getUserName());
        //注意存在数据库中的是提交的表单在加密的DBPass
        user.setPassword(MD5Util.formPassToDBPass(registerVo.getPassword(), MD5Util.salt));
        user.setTelephone(registerVo.getTelephone());
        user.setRealName(registerVo.getRealName());
        user.setIdCard(registerVo.getIdCard());
        user.setState("正常");
        user.setSalt(MD5Util.salt);

        userService.setUser(user);

        return Result.success("注册成功！");
    }

}
