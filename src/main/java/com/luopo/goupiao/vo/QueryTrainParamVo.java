package com.luopo.goupiao.vo;

import com.luopo.goupiao.validator.IsCity;
import com.luopo.goupiao.validator.IsDate;

import javax.validation.constraints.NotNull;

public class QueryTrainParamVo {
    @NotNull(message = "出发地不能为空")
    @IsCity
    private String fromCity;   //自定义的参数校验注解，判断city是否为得发侵入

    @NotNull(message = "目的地不能为空")
    @IsCity
    private String toCity;

    @NotNull(message = "出发日不能为空")
    @IsDate
    private String date;  //自定义的参数校验注解，判断date是否为非法校验侵入

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
