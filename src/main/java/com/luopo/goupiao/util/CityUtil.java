package com.luopo.goupiao.util;

import com.luopo.goupiao.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class CityUtil {
    public static List<String> cityList;

    @Autowired
    public static StationService stationService;

    public static boolean isExist(String city) {
        return cityList.contains(city);
    }

}
