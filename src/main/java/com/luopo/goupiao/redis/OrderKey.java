package com.luopo.goupiao.redis;

public class OrderKey extends BasePrefix{
    public static OrderKey getOrder = new OrderKey("order_");

    public OrderKey(String prefix) {
        super(60*5, prefix);
    }
}
