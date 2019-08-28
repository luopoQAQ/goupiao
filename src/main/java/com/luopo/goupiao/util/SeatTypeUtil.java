package com.luopo.goupiao.util;

public class SeatTypeUtil {
    public static String seatType[] = {"shangWuZuo", "yiDengZuo", "erDengZuo", "ruanWo",
            "yingWo", "yingZuo", "wuZuo"};

    public static int getSeatId(String string) {
        for (int i = 0; i < seatType.length; i++) {
            if (seatType[i] == chineseTypeToEnglish(string)) {
                return i;
            }
        }

        return -1;
    }

    //用作将数据库中中文的座位类型改为英文，方便映射
    public static String chineseTypeToEnglish(String chineseType) {
        if ("商务座".equals(chineseType)) return "shangWuZuo";
        else if ("一等座".equals(chineseType)) return "yiDengZuo";
        else if ("二等座".equals(chineseType)) return "erDengZuo";
        else if ("软卧".equals(chineseType)) return "ruanWo";
        else if ("硬卧".equals(chineseType)) return "yingWo";
        else if ("硬座".equals(chineseType)) return "yingZuo";
        else if ("无座".equals(chineseType)) return "wuZuo";
        else return "qiTa";
    }

    public static String englishTypeToChinese(String chineseType) {
        if ("shangWuZuo".equals(chineseType)) return "商务座";
        else if ("yiDengZuo".equals(chineseType)) return "一等座";
        else if ("erDengZuo".equals(chineseType)) return "二等座";
        else if ("ruanWo".equals(chineseType)) return "软卧";
        else if ("yingWo".equals(chineseType)) return "硬卧";
        else if ("yingZuo".equals(chineseType)) return "硬座";
        else if ("wuZuo".equals(chineseType)) return "无座";
        else return "qiTa";
    }


}
