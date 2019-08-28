package com.luopo.goupiao.access;

import com.luopo.goupiao.pojo.User;

public class UserContext {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();

    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    public static User getUser() {
        return userThreadLocal.get();
    }

}
