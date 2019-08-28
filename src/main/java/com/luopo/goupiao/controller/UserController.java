package com.luopo.goupiao.controller;

import com.luopo.goupiao.access.AccessLimit;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.vo.QueryTrainDetailParamVo;
import com.luopo.goupiao.vo.TrainVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("")
public class UserController {

    @AccessLimit()
    @GetMapping(value="/user")
    @ResponseBody
    public Result<User> getUser(HttpServletRequest request, HttpServletResponse response,
                                       Model model, User user ) {
//        System.out.println("    >> 用户 : " + user.getUserName() + " 访问了该网站(/user)");

        return Result.success(user);
    }


}
