package com.luopo.goupiao.redis;

public class TrainKey extends BasePrefix{

    public static TrainKey getTrainListPage = new TrainKey(20, "TrainListPage_");

    public static TrainKey getTrainList = new TrainKey(20, "TrainList_");

    public static TrainKey getTrainListPageZhongzhuan = new TrainKey(20, "TrainListPageZhongzhuan_");

    public static TrainKey getTrainListZhongzhuan = new TrainKey(20, "TrainListZhongzhuan_");

    public TrainKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
