//package com.luopo.goupiao.util;
//
//import com.luopo.goupiao.mapper.TrainMapper;
//import com.luopo.goupiao.pojo.Train;
//import com.luopo.goupiao.redis.RedisService;
//import com.luopo.goupiao.redis.TrainKey;
//import com.luopo.goupiao.service.SeatService;
//import com.luopo.goupiao.vo.SeatStockVo;
//import com.luopo.goupiao.vo.SeatTypeAndPriceVo;
//import com.luopo.goupiao.vo.TrainVo;
//import com.luopo.goupiao.vo.ZhongzhuanVo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.sql.SQLOutput;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.HashMap;
//import java.util.List;
//
//public class TestUtil {
//
//    public static void main(String[] args) {
//        String date = "2019-08-25";
//        System.out.println(date);
//
//        System.out.println(Integer.parseInt("12"));
//    }
//
//}
//
//
//
//package com.luopo.goupiao.service;
//
//        import com.alibaba.druid.util.StringUtils;
//        import com.luopo.goupiao.access.AccessLimit;
//        import com.luopo.goupiao.mapper.TrainMapper;
//        import com.luopo.goupiao.pojo.Train;
//        import com.luopo.goupiao.pojo.User;
//        import com.luopo.goupiao.redis.RedisService;
//        import com.luopo.goupiao.redis.TrainKey;
//        import com.luopo.goupiao.result.Result;
//        import com.luopo.goupiao.vo.SeatStockVo;
//        import com.luopo.goupiao.vo.SeatTypeAndPriceVo;
//        import com.luopo.goupiao.vo.TrainVo;
//        import com.luopo.goupiao.vo.ZhongzhuanVo;
//        import org.springframework.beans.factory.annotation.Autowired;
//        import org.springframework.boot.autoconfigure.AutoConfigureOrder;
//        import org.springframework.stereotype.Service;
//
//        import javax.naming.Reference;
//        import java.util.Date;
//        import java.text.DateFormat;
//        import java.text.ParseException;
//        import java.text.SimpleDateFormat;
//        import java.util.HashMap;
//        import java.util.List;
//
//@Service
//public class TrainService {
//
//    @Autowired
//    TrainMapper trainMapper;
//
//    @Autowired
//    SeatService seatService;
//
//    @Autowired
//    RedisService redisService;
//
//    public List<Integer> getAllTrainId() {
//        return trainMapper.getAllTrainId();
//    }
//
//    public List<TrainVo> listTrainVo(String fromCity, String toCity, String dateStr) {
//
//        //String转sqlDate
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        java.util.Date date = null;
//        java.sql.Date dateSql = null;   //注意此处是只有年与日的java.sql.Date
//        try {
//            date = dateFormat.parse(dateStr);
//            dateSql = new java.sql.Date(date.getTime());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        //先获取数据缓存（数据缓存更方便于trainDeatil取数据）
//        List<TrainVo> trainList = (List<TrainVo>)redisService.getList(TrainKey.getTrainList,
//                ""+fromCity+"_"+toCity+"_"+date, TrainVo.class);
//        if(null != trainList && (!trainList.isEmpty()) ) {
////            System.out.println("    >> 直接获取trainList数据缓存");
//            return trainList;
//        }
//
//        List<TrainVo> trainVoList = trainMapper.listTrainVo(fromCity, toCity, dateSql);
//
//        for (TrainVo it : trainVoList) {
//            //passTime的计算
//            int tianFrom = Integer.valueOf(it.getFromTime().substring(0, 2));
//            int shiFrom = Integer.valueOf(it.getFromTime().substring(3, 5));
//            int fenFrom = Integer.valueOf(it.getFromTime().substring(6, 8));
//
//            int tianTo = Integer.valueOf(it.getToTime().substring(0, 2));
//            int shiTo = Integer.valueOf(it.getToTime().substring(3, 5));
//            int fenTo = Integer.valueOf(it.getToTime().substring(6, 8));
//
//            int num = tianTo * 24*60 + shiTo * 60 + fenTo - tianFrom * 24*60 - shiFrom * 60 - fenFrom;
//            int tianPassTime = num / (24*60);
//            num %= (24*60);
//            int shiPassTime = num / 60;
//            num %= 60;
//            int fenPassTime = num;
//
//            it.setPastTime("" + String.format("%02d",tianPassTime)
//                    + ":" + String.format("%02d",shiPassTime)
//                    + ":" +  String.format("%02d",fenPassTime));
//
//            //设置model得来的一些数据
//            it.setDate(dateSql);
//            it.setFromCity(fromCity);
//            it.setToCity(toCity);
//        }
//
//        //计算座位余量 和 票价
//        for (TrainVo it : trainVoList) {
//            int numOfStock = 0;
//
//            HashMap<String, Integer> stockMap = new HashMap<>();
//            HashMap<String, Double> priceMap = new HashMap<>();
//
//            List<SeatTypeAndPriceVo> typeList = seatService.getSeatType(it.getTrainId());
//
//            for (SeatTypeAndPriceVo itt : typeList) {
//                //初始化该类型车次拥有的作为类型所有余票为0，为后面view的展示对没有余票的方便辨别
//                //（为0则说明有该类型票，但是都卖光了）
//                stockMap.put(chineseTypeToEnglish(itt.getSeatType()), 0);
//
//                //设置作为类型对于的车票价格
//                priceMap.put(chineseTypeToEnglish(itt.getSeatType()), itt.getPrice());
//            }
//
//            it.setStockMap(stockMap);
//            it.setPriceMap(priceMap);
//
//            List<SeatStockVo> seatStockVoList = seatService.getStock(it.getTrainId(),
//                    dateSql, it.getFromStationId(), it.getToStationId());
//
//            for (SeatStockVo itt : seatStockVoList) {
//                numOfStock += itt.getStock();
//                stockMap.put(chineseTypeToEnglish(itt.getSeatType()), itt.getStock());
//            }
//
//            it.setStockMap(stockMap);
//            it.setFlagOfNoStock(numOfStock == 0);
//        }
//
//        //设置数据缓存，有效时期 30 秒
//        if(null != trainList && (!trainList.isEmpty()) ) {
////            System.out.println("    >> 设置trainList数据缓存");
//            redisService.setList(TrainKey.getTrainList,
//                    ""+fromCity+"_"+toCity+"_"+date, trainList);
//        }
//
//        return trainVoList;
//    }
//
//    //用作将数据库中中文的座位类型改为英文，方便映射
//    public String chineseTypeToEnglish(String chineseType) {
//        if ("商务座".equals(chineseType)) return "shangWuZuo";
//        else if ("一等座".equals(chineseType)) return "yiDengZuo";
//        else if ("二等座".equals(chineseType)) return "erDengZuo";
//        else if ("软卧".equals(chineseType)) return "ruanWo";
//        else if ("硬卧".equals(chineseType)) return "yingWo";
//        else if ("硬座".equals(chineseType)) return "yingZuo";
//        else if ("无座".equals(chineseType)) return "wuZuo";
//        else return "qiTa";
//    }
//
//    public Train getTrainById(int trainId) {
//        return trainMapper.getTrainById(trainId);
//    }
//
//    public List<ZhongzhuanVo> listTrainVoZhongzhuan(String fromCity, String toCity, String date) {
//        return trainMapper.listTrainVoZhongzhuan(fromCity, toCity, date);
//    }
//
//    public TrainVo getTrainZhongzhuan(int trainId, int fromStationId, int toStationId) {
//        return trainMapper.getTrainZhongzhuan(trainId,
//                fromStationId, toStationId);
//
//    }
//}
