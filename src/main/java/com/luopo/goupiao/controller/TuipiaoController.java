package com.luopo.goupiao.controller;

import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.pojo.Order;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.GoupiaoKey;
import com.luopo.goupiao.redis.OrderKey;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.service.OrderService;
import com.luopo.goupiao.service.SeatService;
import com.luopo.goupiao.util.SeatTypeUtil;
import com.luopo.goupiao.util.UUIDUtil;
import com.luopo.goupiao.vo.SeatStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Controller
public class TuipiaoController {
    @Autowired
    OrderService orderService;

    @Autowired
    GoupiaoController goupiaoController;

    @Autowired
    RedisService redisService;

    @Autowired
    SeatService seatService;

    @PostMapping("/tuipiao")
    @ResponseBody
    public Result<String> tuipiao(Model model, User user,
                               int orderId,
                               int trainId,
                               int fromStationId,
                               int toStationId,
                               String seatType,
                               @RequestParam("date") String date ) throws ParseException, InterruptedException {
        if(user == null) {
            throw new GlobalException(CodeMsg.NOT_LOGIN);
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || seatType.length()==0 || date.length()==0) {
            throw new GlobalException(CodeMsg.REQUEST_ILLEGAL);
        }
        model.addAttribute("user", user);

        Map<String, Boolean> hasNoStockMap = goupiaoController.getHasNoStockMap();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(dateFormat.parse(date));

        String uuid = UUIDUtil.uuid();

        //加锁
        redisService.lock("tuipiao", uuid);

//        Thread.sleep(5000);

        //删除orderId对应订单
        orderService.deleteOrder(orderId);

        int seatTypeInt = SeatTypeUtil.getSeatId(seatType);

        // 先删除订单载更新相应缓存
        redisService.delete(OrderKey.getOrder,
                ""+user.getUserId()+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+date);

        //删除DB上的无票缓存标志
        redisService.delete(GoupiaoKey.isDBNoStock,
                "" + trainId + "_" + fromStationId
                        + "_" + toStationId + "_" + seatTypeInt + "_" + dateStr);

        //更新redis预减库存值
        List<SeatStockVo> seatStockVoList = seatService.getStock(trainId, fromStationId, toStationId, dateStr);

        for (SeatStockVo seatStockVo : seatStockVoList) {
            int seatTypeIn = SeatTypeUtil.getSeatId(seatStockVo.getSeatType());
            int stockIn = seatStockVo.getStock();
            redisService.set(GoupiaoKey.getStockOnArea,
                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatTypeIn + "_" + dateStr,
                    stockIn);
        }

        //设置本地无票标志为false（只能设置该台tomcat上，对于其他服务器上的不能设置）
//        hasNoStockMap.put(
//                ""+trainId+"_"+fromStationId
//                        +"_"+toStationId+"_"+seatTypeInt+"_"+dateStr, false);
//

        //解锁
        redisService.unlock("tuipiao", uuid);

        return Result.success("成功");
    }

}
