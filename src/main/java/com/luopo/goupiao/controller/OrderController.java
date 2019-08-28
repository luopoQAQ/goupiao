package com.luopo.goupiao.controller;

import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.pojo.Order;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @RequestMapping("/order")
    public String info(Model model, User user) {
        if(user == null) {
            return "redirect:/pages/login.htm";
        }

        model.addAttribute("user", user);

        List<Order> orderList= orderService.getOrderByUser(user.getUserId());

        model.addAttribute("orderList", orderList);

        return "order";
    }


}
