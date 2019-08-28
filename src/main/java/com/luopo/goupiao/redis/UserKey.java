package com.luopo.goupiao.redis;

public class UserKey extends BasePrefix {

    public static final int TOKEN_EXPIRE = 60*60 * 24 * 3;
    public static final int USER_EXPIRE = 60*60 * 24 * 3;

    public static UserKey token = new UserKey(TOKEN_EXPIRE, "Token_");
    public static UserKey getByUserName = new UserKey(USER_EXPIRE, "UserName_");

    private UserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
