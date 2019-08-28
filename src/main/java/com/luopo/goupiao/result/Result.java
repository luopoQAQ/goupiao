package com.luopo.goupiao.result;

public class Result<T> {

    private int code;
    private String msg;
    private T data;

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    //正常处理
    public static  <T> Result<T> success(T data){
        return new Result<T>(data);
    }

    //异常情况下的结果
    public static  <T> Result<T> error(CodeMsg codeMsg){
        return new Result<T>(codeMsg);
    }

    //seccess情况下调用的构造器
    public Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    //error情况下调用的构造器
    private Result(CodeMsg codeMsg) {
        if(codeMsg != null) {
            this.code = codeMsg.getCode();
            this.msg = codeMsg.getMsg();
            this.data = (T)"出现异常";
        }
    }

    private Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
