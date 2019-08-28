package com.luopo.goupiao.controller;

import com.luopo.goupiao.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index(Model model, User user) {
        model.addAttribute("user", user);
        return ("home");
    }

}
