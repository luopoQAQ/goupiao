package com.luopo.goupiao.service;

import com.luopo.goupiao.mapper.SeatMapper;
import com.luopo.goupiao.pojo.Seat;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.redis.SeatStockKey;
import com.luopo.goupiao.vo.SeatStockVo;
import com.luopo.goupiao.vo.SeatTypeAndPriceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    SeatMapper seatMapper;

    @Autowired
    RedisService redisService;


    public List<SeatStockVo> getStock(int trainId,
                                      Date date,
                                      int fromStationId,
                                      int toStationId) {
        return seatMapper.getStock(trainId, date, fromStationId, toStationId);
    }

    //按日期、车次、出发站、到达站得到余票，有缓存
    public List<SeatStockVo> getStock(int trainId,
                                      int fromStationId,
                                      int toStationId,
                                      String date) {

//        List<SeatStockVo> seatStockVoList =
//                redisService.getList(SeatStockKey.getStockByArea,
//                        ""+trainId+"_"+fromStationId+"_"+toStationId+"_"+date,
//                        SeatStockVo.class);

//        if (null != seatStockVoList && !seatStockVoList.isEmpty()) {
//            return seatStockVoList;
//        }

        List<SeatStockVo> seatStockVoList = seatMapper.getStockDateString(trainId, fromStationId, toStationId, date);

//        redisService.setList(SeatStockKey.getStockByArea,
//                ""+trainId+"_"+fromStationId+"_"+toStationId+"_"+date,
//                seatStockVoList);

        return seatStockVoList;
    }

    public List<SeatTypeAndPriceVo> getSeatType(int trainId) {
        return seatMapper.getSeatType(trainId);
    }

    public List<String> getSeatTypeByTrainId(int trainId) {
        return seatMapper.getSeatTypeByTrainId(trainId);
    }

    public List<Seat> getSeatList(int trainId, int fromStationId, int toStationId, String seatType, String date) {
        return seatMapper.getSeatList(trainId, fromStationId, toStationId, seatType, date);
    }

    public Seat getSeatById(int trainId, int seatId) {
        return seatMapper.getSeatById(trainId, seatId);
    }
}
