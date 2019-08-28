package com.luopo.goupiao.util;

import java.util.UUID;

public class UUIDUtil {

    //生成唯一序列ID，并将'-'消除
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}