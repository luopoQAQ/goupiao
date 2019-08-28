package com.luopo.goupiao.redis;

public class SeatStockKey extends BasePrefix {

    public static SeatStockKey getStockByArea =
            new SeatStockKey(60*60*24*30, "StockByArea_");

    public SeatStockKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

}
