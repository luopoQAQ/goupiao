package com.luopo.goupiao.controller;

import com.luopo.goupiao.access.AccessLimit;
import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.mq.GoupiaoMessage;
import com.luopo.goupiao.mq.MQSender;
import com.luopo.goupiao.pojo.Order;
import com.luopo.goupiao.pojo.Station;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.GoupiaoKey;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.redis.SeatStockKey;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.service.*;
import com.luopo.goupiao.util.CityUtil;
import com.luopo.goupiao.util.SeatTypeUtil;
import com.luopo.goupiao.vo.SeatStockVo;
import com.luopo.goupiao.vo.TrainVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/goupiao")
public class GoupiaoController implements InitializingBean  {

    //一次尝试

    // 该车次在确定日期内在from至to区间内已无车票 的map为true
    // 该车次在确定日期内所有车票都售罄 的map为true
    //当在redis层发现库存 < 0 时，则设置为true（两种情况）
    //则剩余的所有访问都在这一层被直接拦截，不需要访问redis层
    private HashMap<String, Boolean> hasNoStockMap =  new HashMap<String, Boolean>();

    public HashMap<String, Boolean> getHasNoStockMap() {
        return hasNoStockMap;
    }

    public void setHasNoStockMap(HashMap<String, Boolean> hasNoStockMap) {
        this.hasNoStockMap = hasNoStockMap;
    }

    @Autowired
    private GoupiaoService goupiaoService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderService orderService;

    @Autowired
    MQSender sender;

    @Autowired
    TrainService trainService;

    @Autowired
    StationService stationService;

    @Autowired
    SeatService seatService;

    //系统初始化
    //加载所有可能区间在未来一个月内的每天的余票的缓存
    public void afterPropertiesSet() throws Exception {
        List<Integer> trainIdList = trainService.getAllTrainId();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //制定日期格式
        Calendar c = Calendar.getInstance();
        java.util.Date date = new java.util.Date();
        c.setTime(date);

        for (int i=0; i<29; i++) {
            c.add(Calendar.DATE,1); //将当前日期加一天

            //获取日期
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateFormat.format(c.getTime());

            for (int trainId : trainIdList) {
                List<Integer> stationIdList = stationService.getStationListByTrainId(trainId);

                List<String> seatTypeList = seatService.getSeatTypeByTrainId(trainId);

                for (String seatTypeStr : seatTypeList) {
                    int seatType = SeatTypeUtil.getSeatId(seatTypeStr);
                    if (seatType == -1) {
                        throw new GlobalException(CodeMsg.SERVER_ERROR);
                    }

                    for (int fromStationId : stationIdList) {
                        for (int toStationId : stationIdList) {
                            if (fromStationId < toStationId) {
                                String fromTime = trainService
                                        .getTrainZhongzhuan(trainId, fromStationId, toStationId)
                                        .getFromTime();

                                int dayNum = Integer.parseInt(fromTime.substring(0, 2));
                                if (dayNum != 0) {
                                    Calendar cIn = Calendar.getInstance();
                                    cIn.setTime(c.getTime());
                                    cIn.add(Calendar.DATE, -dayNum); //将当前日期减dayNum天，这才是查询余票和下订单时的真正日期
                                    dateStr = dateFormat.format(cIn.getTime());
                                }

//                                hasNoStockMap.put("" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + dateStr, false);

                                //在seatService里面会直接设置缓存
                                List<SeatStockVo> seatStockVoList = seatService.getStock(trainId, fromStationId, toStationId, dateStr);

                                for (SeatStockVo seatStockVo : seatStockVoList) {
                                    int seatTypeIn = SeatTypeUtil.getSeatId(seatStockVo.getSeatType());
                                    int stockIn = seatStockVo.getStock();
                                    redisService.set(GoupiaoKey.getStockOnArea,
                                            "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatTypeIn + "_" + dateStr,
                                            stockIn);
                                }
                            }
                        }
                    }
                }
            }
        }

        //每隔5s刷新一次本地缓存（在退票后某些情况下最多有5s延迟）
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                    System.out.println("开始清除");
                    hasNoStockMap.clear();

                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //每隔30s清空一次本地标识，防止数据较多，同时也是更新退票后同步redis与本地标识
        thread.start();
    }

    @GetMapping(value="/result")
    @ResponseBody
    public Result<Integer> goupiaoResult(Model model,
                                      User user,
                                      @NotNull int trainId,
                                      @NotNull int fromStationId,
                                      @NotNull int toStationId,
                                      @NotNull int seatType,
                                      @NotNull String date) throws ParseException {
//        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }

        //如果购票成功，返回1
        //如果余票标志还未修改，则为0，继续轮询
        //如果余票标志为true，则直接返回-1，表示购票失败
        int result = goupiaoService.getGoupiaoResult(user.getUserId(), trainId, fromStationId,
                toStationId, seatType, date);
        return Result.success(result);
    }

    @AccessLimit(seconds=10, maxCount=10, needLogin=true)
    @PostMapping(value="/{path}/do")
    @ResponseBody
    public Result<Integer> goupiao(Model model,
                                   User user,
                                   int trainId,
                                   int fromStationId,
                                   int toStationId,
                                   int seatType,
                                   String date,
                                   @PathVariable("path") String path) throws ParseException {
        if(user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || seatType<0 || date.isEmpty()) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        model.addAttribute("user", user);

        //验证path
        boolean check = goupiaoService.checkPath(user, trainId, fromStationId,
                toStationId, seatType, date, path);

        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //！！！dateKey是处理后的date，因为可能存在购买该车次日期是之前的
        String dateKey = date;

        int dayNum = Integer.parseInt(trainService
                .getTrainZhongzhuan(trainId, fromStationId, toStationId)
                .getFromTime()
                .substring(0, 2));

        if (dayNum != 0) {
            Calendar cIn = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            cIn.setTime(df.parse(date));
            cIn.add(Calendar.DATE, -dayNum); //将当前日期减dayNum天，这才是查询余票和下订单时的真正日期
            dateKey = df.format(cIn.getTime());
        }

        //判断用户是否购买该车次当日车票
        Order orderByUserAndDate = orderService.getOrderByUserAndDate(user.getUserId(), dateKey, trainId);
        if (null != orderByUserAndDate) {
            return Result.error(CodeMsg.ORDER_BY_USER_AND_DATE);
        }

        //内存标记，减少redis访问
        Boolean noStockOnAreaAndSeatType = hasNoStockMap.get(
                ""+trainId+"_"+fromStationId+"_"+toStationId+"_"+seatType+"_"+dateKey);

        if (null!=noStockOnAreaAndSeatType && noStockOnAreaAndSeatType) {
            return Result.error(CodeMsg.HAS_NO_TRAIN_TICKET);
        }

        //否则该车次该区间内车票暂时不为0
        //预减库存
        long stockOnArea = redisService.decr(GoupiaoKey.getStockOnArea,
                ""+trainId+"_"+fromStationId+"_"+toStationId+"_"+seatType+"_"+dateKey);

        if(stockOnArea < 0) {
            //map标记置true,使map层开始阻拦
            hasNoStockMap.put(
                    ""+trainId+"_"+fromStationId+"_"+toStationId+"_"+seatType+"_"+dateKey, true);
            return Result.error(CodeMsg.HAS_NO_TRAIN_TICKET);
        }

        //判断该用户是否重复购票
        Order order = orderService.getOrder(user.getUserId(), trainId,
                fromStationId, toStationId, dateKey);
        if(null != order) {
            return Result.error(CodeMsg.REPEATE_GOUPIAO);
        }

        //如果map,redis都没有显示余票不足，且该用户没有重复购买，
        //则将请求放入消息队列，异步削峰处理后再进行 DB 层的操作
        //入队
        GoupiaoMessage goupiaoMessage = new GoupiaoMessage();
        goupiaoMessage.setUser(user);
        goupiaoMessage.setTrainId(trainId);
        goupiaoMessage.setFromStationId(fromStationId);
        goupiaoMessage.setToStationId(toStationId);
        goupiaoMessage.setDate(dateKey);
        goupiaoMessage.setDateBefore(date);
        goupiaoMessage.setSeatType(seatType);

        sender.sendGoupiaoMessage(goupiaoMessage);

        //!!!并未成功购买，只是交由消息队列进一步处理
        return Result.success(0);
    }

    @AccessLimit(seconds=10, maxCount=10, needLogin=true)
    @GetMapping("/path")
    @ResponseBody
    public Result<String> getGoupiaoPath(HttpServletRequest request,
                                         User user,
                                         int trainId,
                                         int fromStationId,
                                         int toStationId,
                                         int seatType,
                                         String date,
                                         String verifyCodeStr) {
        if(user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || seatType <0 || date.length()==0 ) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        int verifyCode;
        try {
            verifyCode = Integer.parseInt(verifyCodeStr);
        } catch (Exception e) {
            return Result.error(CodeMsg.VERIFY_CODE_ERROR);
        }

        boolean check = goupiaoService.checkVerifyCode(user, trainId, fromStationId, toStationId, date, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.VERIFY_CODE_ERROR);
        }

        //验证码验证通过，且参数校验正常，可以生成随机访问地址了
        String path  = goupiaoService.createGoupiaoPath(user, trainId, fromStationId, toStationId, seatType, date);

        //返回随机生成的path
        return Result.success(path);
    }

    //返回生成的随机验证码
    @AccessLimit(seconds=10, maxCount=10, needLogin=true)
    @GetMapping(value="/verifyCode")
    @ResponseBody
    public Result<String> getGoupiaoVerifyCode(HttpServletResponse response,
                                               User user,
                                               int trainId,
                                               int fromStationId,
                                               int toStationId,
                                               String date) {
        if(user == null) {
            return Result.error(CodeMsg.NOT_LOGIN);
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || date.length()==0) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        try {
            BufferedImage image  = goupiaoService.createVerifyCode(user, trainId, fromStationId, toStationId, date);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.GOUPIAO_FAIL);
        }
    }

}
