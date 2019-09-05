package com.luopo.goupiao.mq;

import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.pojo.*;
import com.luopo.goupiao.redis.GoupiaoKey;
import com.luopo.goupiao.redis.OrderKey;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.service.*;
import com.luopo.goupiao.util.SeatTypeUtil;
import com.luopo.goupiao.vo.SeatStockVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeatService seatService;

    @Autowired
    GoupiaoService goupiaoService;

    @Autowired
    TrainService trainService;

    @Autowired
    StationService stationService;

    //监听由队列名为GOUPIAO_QUEUE的队列里的消息
    @RabbitListener(queues=MQConfig.GOUPIAO_QUEUE)
    public void receive(String message) throws ParseException {
        log.info("  >> receive message : "+message);

        GoupiaoMessage goupiaoMessage = RedisService.stringToBean(message, GoupiaoMessage.class);

        User user = goupiaoMessage.getUser();
        int trainId = goupiaoMessage.getTrainId();
        int fromStationId = goupiaoMessage.getFromStationId();
        int toStationId = goupiaoMessage.getToStationId();
        int seatType = goupiaoMessage.getSeatType();
        String date = goupiaoMessage.getDate();
        String dateBefore = goupiaoMessage.getDateBefore();

//        List<SeatStockVo> seatStockVoList = seatService.getStock(trainId,
//                fromStationId, toStationId, date);
//
//        int stock = -1;
//        for (SeatStockVo it : seatStockVoList) {
//            if (it.getSeatType().equals(SeatTypeUtil.seatType[seatType])) {
//                stock = it.getStock();
//            }
//        }
//
//        if(stock <= 0) {
//            redisService.set(GoupiaoKey.getStockOnArea,
//                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + date,
//                    0);
//            redisService.set(GoupiaoKey.isDBNoStock,
//                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + date,
//                    -1);
//            return;
//        }

        //判断是否已经抢到了
        Order order = orderService.getOrder(user.getUserId(), trainId,
                fromStationId, toStationId, date);
        if(order != null) {
            return;
        }

        order = null;

        //从MQ取出消息，先检查DB标志是否为-1，若是，则说明无票
        //若不存在或者为-1说明可以抢票
        //得到随机未被加入订单的座位id 写入订单
        //重复写入，直到得到缓存里的相应余票为0
        //利用唯一索引来保证seatId不会重复，即不会卖超
        while (true) {
            Integer stockByDB = redisService.get(GoupiaoKey.isDBNoStock,
                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + date,
                    Integer.class);

            if (null != stockByDB && stockByDB == -1) { //尝试购票
                //在缓存里设置无票标志，方便于前端轮询结果时查看
                redisService.set(GoupiaoKey.getStockOnArea,
                        "" + trainId + "_" + fromStationId + "_"
                                + toStationId + "_" + seatType + "_" + date,
                        0);
                break;
            } else {
                goupiaoService.goupiao(user, trainId,
                        fromStationId, toStationId, seatType, date);

                //检查缓存是否有订单存在
                order = orderService.getOrder(user.getUserId(), trainId,
                        fromStationId, toStationId, date);
                if (null != order) {
                    //如果订单存在，说明抢购成功，则结束
                    break;
                }
            }
        }

        Train train = trainService.getTrainById(trainId);
        if (null != order) {
            log.info("  >> 用户 : "+user.getUserName()+"购买 " + date + " "
                    + train.getTrainName() + "车次成功");

            //如订单提交成功则补充订单信息
            order.setTrainName(train.getTrainName());

            Seat seat = seatService.getSeatById(order.getTrainId(), order.getSeatId());
            order.setPrice(seat.getPrice());
            order.setSeatType(seat.getSeatType());
            order.setSeatLocation(seat.getSeatLocation());
            order.setCarriage(seat.getCarriage());

            Station fromStation = stationService.getStationByTrainIdAndStationId(trainId, fromStationId);
            order.setFromStationName(fromStation.getStationName());
            String fromTimeStr = fromStation.getArriveTime();
            fromTimeStr = dateBefore + " " + fromTimeStr.substring(3, fromTimeStr.length()) + ":00";
            //String转Date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date fromTime =  dateFormat.parse(fromTimeStr);
            order.setFromTime(fromTime);

            Station toStation = stationService.getStationByTrainIdAndStationId(trainId, toStationId);
            order.setToStationName(toStation.getStationName());

            order.setState("正常");

            //String转sqlDate
//            DateFormat dateFormatSql = new SimpleDateFormat("yyyy-MM-dd");
//            java.sql.Date dateSql = null;   //注意此处是只有年与日的java.sql.Date
//            try {
//                dateSql = new java.sql.Date(dateFormat.parse(date).getTime());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            order.setDate(dateSql);

            orderService.updateOrder(order);
        }
        else {
            log.info("  >> 用户 : "+user.getUserName()+"购买 " + date + " "
                    + train.getTrainName() + "车次失败");
        }

    }
}
