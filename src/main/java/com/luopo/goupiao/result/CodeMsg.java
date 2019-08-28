package com.luopo.goupiao.result;

public class CodeMsg {

    private int code;
    private String msg;

    //通用的错误码
    public static CodeMsg SUCCESS = new CodeMsg(0, "成功");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务端异常");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "%s");
    public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500102, "请求非法");
    public static CodeMsg ACCESS_FREQUENT = new CodeMsg(500104, "访问太频繁！");

    //登录模块 5002XX
    public static CodeMsg USER_NAME_NOT_EXIST = new CodeMsg(500200, "用户名不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500201, "密码错误");
    public static CodeMsg NOT_LOGIN = new CodeMsg(500202, "未登录");

    //注册模块 5003XX
    public static CodeMsg USER_NAME_HAS_EXIST= new CodeMsg(500300, "用户名已存在,请选择其他用户名");

    //查询模块 5004XX
    public static CodeMsg TRAIN_NO_EXIST_ON_CONDITIONS = new CodeMsg(500400, "该区间内相关车次不存在");
    public static CodeMsg STATION_CAN_NOT_EQUAL = new CodeMsg(500400, "出发地和目的地不能相同");

    //抢票模块 5005XX
    public static CodeMsg GOUPIAO_FAIL = new CodeMsg(500500, "购票失败");
    public static CodeMsg VERIFY_CODE_GENERATE_ERROR = new CodeMsg(500502, "验证码生成异常");
    public static CodeMsg VERIFY_CODE_ERROR = new CodeMsg(500503, "验证码输入错误");
    public static CodeMsg HAS_NO_TRAIN_TICKET = new CodeMsg(500504, "余票不足");
    public static CodeMsg REPEATE_GOUPIAO = new CodeMsg(500505, "不可重复购买");
    public static CodeMsg ORDER_BY_USER_AND_DATE = new CodeMsg(500505, "您已购买该日当前车次车票");

    //更多...


    private CodeMsg(int code,String msg ) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CodeMsg [code=" + code + ", msg=" + msg + "]";
    }

    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);    //msg:"参数校验异常：%s"
        return new CodeMsg(code, message);
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }

}
