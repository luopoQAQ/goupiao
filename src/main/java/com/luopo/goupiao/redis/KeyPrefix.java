package com.luopo.goupiao.redis;

public interface KeyPrefix {

    public int getExpireSeconds();

    public String getPrefix();

}
