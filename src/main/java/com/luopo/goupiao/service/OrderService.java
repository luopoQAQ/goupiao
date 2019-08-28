package com.luopo.goupiao.service;

import com.luopo.goupiao.mapper.OrderMapper;
import com.luopo.goupiao.pojo.Order;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.OrderKey;
import com.luopo.goupiao.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    RedisService redisService;

    public Order getOrder(int userId, int trainId, int fromStationId,
                          int toStationId, String date) {
        Order order = redisService.get(OrderKey.getOrder,
                ""+userId+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+date,
                Order.class);

        if (order != null) {
            return order;
        }

        order = orderMapper.getOrder(userId, trainId, fromStationId, toStationId, date);

        if (null != order) {
            redisService.set(OrderKey.getOrder,
                    ""+userId+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+date, order);

            return order;
        }

        return null;
    }


    public void createOrder(User user, int trainId, int fromStationId, int toStationId, int seatId, String date) {
        orderMapper.addOrder(user, trainId,
                fromStationId, toStationId, seatId, date);
    }

    public void updateOrder(Order order) {
        orderMapper.updateOrder(order);
    }

    public List<Order> getOrderByUser(int userId) {
        return orderMapper.getOrderByUser(userId);
    }

    public void deleteOrder(int orderId) {
        orderMapper.deleteOrder(orderId);
    }

    public Order getOrderByUserAndDate(int userId, String date, int trainId) {
        return orderMapper.getOrderByUserAndDate(userId, date, trainId);
    }
}
