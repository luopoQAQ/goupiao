package com.luopo.goupiao.redis;

public class BasePrefix implements KeyPrefix {

    private int expireSeconds;
    private String prefix;

    public BasePrefix(String prefix) {//0代表永不过期
        this(0, prefix);
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int getExpireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        //前缀前加上了类名，以方便区分每一个类相同值
        String className = this.getClass().getSimpleName();
        return className+"_" + prefix;
    }
}
