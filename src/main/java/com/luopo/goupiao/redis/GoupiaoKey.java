package com.luopo.goupiao.redis;

public class GoupiaoKey extends BasePrefix{


    //验证码有效时间5分钟
    public static GoupiaoKey getGoupiaoVerifyCode = new GoupiaoKey(300, "GoupiaoVerifyCode_");

    //临时path有效时间15s
    public static GoupiaoKey getGoupiaoPath = new GoupiaoKey(15, "GoupiaoPath_");

    //stock有效时间30天
    public static GoupiaoKey getStockOnArea = new GoupiaoKey(60*60*24*30, "StockOnArea_");

    //stock有效时间30天
    public static GoupiaoKey isDBNoStock = new GoupiaoKey(60*60*24*30, "NoStock_");

    public GoupiaoKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }


}
