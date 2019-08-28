package com.luopo.goupiao.redis;

public class AccessKey extends BasePrefix{

    public static AccessKey accessWithLimit(int expireSeconds) {
        return new AccessKey(expireSeconds, "AccessWithLimit_");
    }

    private AccessKey( int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
