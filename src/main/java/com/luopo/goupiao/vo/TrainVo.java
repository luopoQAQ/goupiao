package com.luopo.goupiao.vo;

import java.sql.Date;
import java.util.HashMap;

public class TrainVo {
    //多表查询相关
    private String trainName;
    private int trainId;
    private String fromStationName;
    private String toStationName;
    private int fromStationId;
    private int toStationId;
    private String fromTime;
    private String toTime;

    private String pastTime;

    //seat相关（stock 和 price）
    private HashMap<String, Integer> stockMap;
    private HashMap<String, Double> priceMap;
    private boolean flagOfNoStock;

    //model相关
    private java.sql.Date date;
    private String fromCity;
    private String toCity;

    //为什么可以在这里存放user数据呢？？？应该从model传更合理，这里是公用的！与用户无关的
//    //user相关
//    private String realName;
//    private String idCard;
//    private String telephone;

    public HashMap<String, Double> getPriceMap() {
        return priceMap;
    }

    public void setPriceMap(HashMap<String, Double> priceMap) {
        this.priceMap = priceMap;
    }

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

    public boolean isFlagOfNoStock() {
        return flagOfNoStock;
    }

    public void setFlagOfNoStock(boolean flagOfNoStock) {
        this.flagOfNoStock = flagOfNoStock;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public HashMap<String, Integer> getStockMap() {
        return stockMap;
    }

    public void setStockMap(HashMap<String, Integer> stockMap) {
        this.stockMap = stockMap;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public int getFromStationId() {
        return fromStationId;
    }

    public void setFromStationId(int fromStationId) {
        this.fromStationId = fromStationId;
    }

    public int getToStationId() {
        return toStationId;
    }

    public void setToStationId(int toStationId) {
        this.toStationId = toStationId;
    }

    public String getPastTime() {
        return pastTime;
    }

    public void setPastTime(String pastTime) {
        this.pastTime = pastTime;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getFromStationName() {
        return fromStationName;
    }

    public void setFromStationName(String fromStationName) {
        this.fromStationName = fromStationName;
    }

    public String getToStationName() {
        return toStationName;
    }

    public void setToStationName(String toStationName) {
        this.toStationName = toStationName;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }
}
