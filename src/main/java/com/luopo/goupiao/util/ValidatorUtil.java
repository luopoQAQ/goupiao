package com.luopo.goupiao.util;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    private static final Pattern mobile_pattern = Pattern.compile("^((13[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");  //1开头后跟
    private static final Pattern idCard_pattern = Pattern.compile("(^\\d{18})");

    public static boolean isIdCard(String value) {
        if(StringUtils.isEmpty(value)) {
            return false;
        }
        Matcher m = idCard_pattern.matcher(value);
        return m.matches();
    }

    public static boolean isMobile(String src) {
        if(StringUtils.isEmpty(src)) {
            return false;
        }
        Matcher m = mobile_pattern.matcher(src);
        return m.matches();
    }

    public static boolean isCity(String src) {
        if(StringUtils.isEmpty(src)) {
            return false;
        }
        return CityUtil.isExist(src);
    }

    public static boolean isDate(String dateSqlStr) {
        if (StringUtils.isEmpty(dateSqlStr)) {
            return false;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date dateSql = null;   //注意此处是只有年与日的java.sql.Date
        try {
            dateSql = new java.sql.Date(dateFormat.parse(dateSqlStr).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        java.sql.Date dateToday = new java.sql.Date(new java.util.Date().getTime());

        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd"); //制定日期格式
        Calendar c = Calendar.getInstance();
        java.util.Date date = new java.util.Date();
        c.setTime(date);
        c.add(Calendar.DATE,30); //将当前日期加一个月
        java.sql.Date dateOneMonthAgo =
                new java.sql.Date(c.getTime().getTime());   //calendar.getTime是date
                                                            //date.getTime是long类型毫秒

        //dateSql必须在 (今天, 30日后) 左开右开区间内
        return (dateToday.before(dateSql) && dateOneMonthAgo.after(dateSql));
    }

}
