package com.luopo.goupiao.service;

import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.pojo.Order;
import com.luopo.goupiao.pojo.Seat;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.GoupiaoKey;
import com.luopo.goupiao.redis.OrderKey;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.result.Result;
import com.luopo.goupiao.util.MD5Util;
import com.luopo.goupiao.util.SeatTypeUtil;
import com.luopo.goupiao.util.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class GoupiaoService {
    private static char[] ops = new char[] {'+', '-', '*'};

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private TrainService trainService;

    public BufferedImage createVerifyCode(User user, int trainId, int fromStationId,
                                          int toStationId,
                                          String date) {
        if(user == null) {
            return null;
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || date.length()==0) {
            return null;
        }

        int width = 100;
        int height = 32;

        //创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        //背景色
        g.setColor(new Color(0xD4DCDB));
        g.fillRect(0, 0, width, height);

        //边框色
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);

        Random rdm = new Random();
        //制造噪音
        for (int i = 0; i < 100; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }

        // 生成随机算术串
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String verifyCode = "("+ num1 + op1 + num2 + ")" + op2 + num3;
        int ans = calcAns(calcAns(num1, num2, op1), num3, op2);

        g.setColor(new Color(221, 0, 117));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 12, 22);
        g.dispose();

        //把验证码存到redis中
        redisService.set(GoupiaoKey.getGoupiaoVerifyCode,
                "" + user.getUserId()+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+date, ans);
        //输出图片
        return image;
    }

    private int calcAns(int num1, int num2, char op) throws GlobalException {
        if (op == '+')
            return num1 + num2;
        else if (op == '-')
            return num1 - num2;
        else if (op == '*')
            return num1 * num2;
        else {
            throw new GlobalException(CodeMsg.VERIFY_CODE_GENERATE_ERROR);
        }
    }

    public boolean checkVerifyCode(User user, int trainId, int fromStationId,
                                   int toStationId, String date, int verifyCode) {
        if(user == null) {
            return false;
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || date.isEmpty()) {
            return false;
        }

        Integer codeRedis =  redisService.get(GoupiaoKey.getGoupiaoVerifyCode,
                user.getUserId()+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+date, Integer.class);

        if(codeRedis == null || codeRedis != verifyCode ) {
            return false;
        }

        //验证成功后则删除该验证码，因为用不到了
        redisService.delete(GoupiaoKey.getGoupiaoVerifyCode,
                user.getUserId()+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+date);
        return true;
    }

    public String createGoupiaoPath(User user, int trainId, int fromStationId, int toStationId, int seatType, String date) {
        if(user == null) {
            return null;
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || seatType<0 || date.isEmpty()) {
            return null;
        }

        String str = MD5Util.md5("QAQ"+UUIDUtil.uuid()+"QWQ");

        //将临时path设置于缓存中，方便验证
        redisService.set(GoupiaoKey.getGoupiaoPath,
                user.getUserId()+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+seatType+"_"+date.toString(), str);

        return str;
    }

    public boolean checkPath(User user, int trainId, int fromStationId,
                             int toStationId, int seatType, String date, String path) {
        if(user == null) {
            return false;
        }
        if(trainId <0 || fromStationId<0 || toStationId<0 || seatType<0 || date.isEmpty()) {
            return false;
        }

        String pathOld = redisService.get(GoupiaoKey.getGoupiaoPath,
                user.getUserId()+"_"+trainId+"_"+fromStationId+"_"+toStationId+"_"+seatType+"_"+date, String.class);

        return path.equals(pathOld);
    }

    //这东西不能只靠唯一索引来约束
    //唯一索引只能起一部分作用，最终还是要靠数据库事务的串行化来约束来执行
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void goupiao(User user,
                        int trainId,
                        int fromStationId,
                        int toStationId,
                        int seatType,
                        String date) {
        //得到最先未被加入订单的座位id 写入订单
        //与秒杀的区别不同之处：
        //!!!一个非常重要的因素，即秒杀的商品是单一的，是有stock列的，可以直接从stock值减一来操作
        //但是，车票是不可以的，因为没办法每天都为数据库的所有作为进行更新，那样数据过于庞大，且复杂（或许可以？但是我是没办法实现这么大的工作量的）
        //所以我的设计是：先得到匹配conditions但不在order内的seatid，从中随机选择一个
        // （随机的重要性！如果按照某一规律，所有请求都试图写入同一个seatId那么必然只有一个成功，剩下的都会失败）
        //然后直接插入订单（利用数据库的唯一索引与事物机制，保证数据插入不会重复（好像序列化了也不需要唯一索引了，，，））

        List<Seat> seatList =
                seatService.getSeatList(trainId, fromStationId, toStationId,
                        SeatTypeUtil.englishTypeToChinese(SeatTypeUtil.seatType[seatType]), date);

        if (seatList.isEmpty()) {
            //数据库中余票为0，设置DB标志余票为-1，设置redis标志标志为true
            redisService.set(GoupiaoKey.isDBNoStock,
                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + date,
                    -1);
//            redisService.set(GoupiaoKey.getStockOnArea,
//                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + date,
//                    0);
            return;
        }

        //否则说明数据库暂时有票
        int seatIndex = (int) (Math.random() * (seatList.size()));
        int seatId = seatList.get(seatIndex).getSeatId();

        orderService.createOrder(user, trainId,
                    fromStationId, toStationId, seatId, date);
    }

    public int getGoupiaoResult(int userId, int trainId, int fromStationId,
                                int toStationId, int seatType, String date) throws ParseException {
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

        date = dateKey;

        Order order = orderService.getOrder(userId, trainId,
                fromStationId, toStationId, date);
        if(order != null) { //购票成功
            return 1;
        }else {
            // 如果是相当于存在stock一般秒杀架构中会存在以下问题：
            // 可能此时订单正在生成，同时也没有余票了，那么如果订单没有生成好
            // 则得到的order可能也是null的，但是可能这里返回了-1给了客户端之后，数据库中订单又插入好了
            // 这就出现了前端显示余票不足，但是再查看订单发现订单又已经生成的不一致性
            // 但是！！！这里不存在这个问题，因为我们院设置stock字段，isDBNoStock的标志是在
            // 消息队列取出数据处理时，根据order查出的实时余票数量来判断的，即order未插入，那么就不会显示没有余票了，避免了这个问题
            Integer isOver = redisService.get(GoupiaoKey.isDBNoStock,
                    "" + trainId + "_" + fromStationId + "_" + toStationId + "_" + seatType + "_" + date,
                    Integer.class);
            if(isOver!=null && isOver==-1) {
                return -1;
            }else {
                return 0;
            }
        }


    }
}
