package com.luopo.goupiao.vo;

import com.luopo.goupiao.validator.IsIdCard;
import com.luopo.goupiao.validator.IsMobile;

import javax.validation.constraints.NotNull;

public class RegisterVo {

    @NotNull
    private String userName;

    @NotNull
    private String password;

    @NotNull
    @IsMobile
    private String telephone;

    @NotNull
    private String realName;

    @NotNull
    @IsIdCard
    private String idCard;

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

