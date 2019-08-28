package com.luopo.goupiao.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    public static final String salt = "613211";

    //第一次MD5加密，原始密码转表单密码（表单提交的为MD5加密后的密码）
    public static String inputPassToFormPass(String inputPass) {
        String str = "" + salt.charAt(1) + salt.charAt(2)
                + inputPass + salt.charAt(3) + salt.charAt(4);
        return md5(str);
    }

    //第二次MD5加密，表单密码再加密，用来保存在数据库中
    public static String formPassToDBPass(String formPass, String salt) {
        String str = "" + salt.charAt(1) + salt.charAt(2)
                + formPass + salt.charAt(3) + salt.charAt(4);
        return md5(str);
    }

    public static String inputPassToDbPass(String inputPass, String saltDB) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, saltDB);
        return dbPass;
    }

}
