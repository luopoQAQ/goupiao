# goupiao

### 一个简单的火车票购票网站，主要关注在高并发情况下如何缓解抢票压力

#### 一、项目地址及技术栈： <br>
  http://49.235.18.252/index <br>
>后端：springboot + springmvc + mybatis + redis + rabbitMq + jsr303 + nginx <br>
>前端：html + JQuery + BootStrap + Thymeleaf <br>
>环境：数据库-mysql 服务器-nginx + tomcat

#### 二、主要功能： <br>
* 查询车次： <br>
  根据起始城市与出发日期查询在运营的车次信息 <br>
  根据发车时间顺序显示车次、始发站、终点站、发车时间、预计到达时间、历时、各座位类型下余票详细信息 <br>
* 查询接续换乘车次： <br>
  根据起始城市与出发日期查询在运营的**接续换乘**车次信息 <br>
  显示可以经过中间城市进行中转的所有方案，并显示每一个方案中换乘前后车次的相关信息 <br>
* 预定： <br>
  对于该区间内火车票有余票的车次可以进行车票预定，余票不足则预定button无法点击 <br>
* 购票： <br>
  显示即将购买的车票的详细信息，并在用户选择座位类型、输入验证码后可以提交订单 <br>
  订单提交后会提示购票成功（是否前往查看：是 否）和购票失败（失败原因） <br>
* 查询车票： <br>
  查询该用户购买的所有车票信息，根据购买时间进行排序，除了显示车票相关信息外，还显示该车票是否过期（超过发车时间则过期） <br>
* 退票： <br>
  在我的车票页面，对于没有过期的车票可以点击退票进行退票操作 <br>
* 注册、登录： <br>
  提交用户必备的详细信息可以进行注册，对于注册数据会有一定验证，如手机号、身份证号必须符合格式，用户名必须唯一等 <br>
  对于用户登录，如果三天之内曾登陆过，则下次访问网站会自动携带token，不需要再次手动登陆，以方便用户 <br>

#### 三、项目实现： <br>
###### 1. 数据库设计： <br>
>数据库一共包括六张表，分别是： <br>
* train：火车车次、类型信息 <br>
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/train.PNG)
* train_state：火车各个日期下状态信息 <br>
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/train_state.PNG)
* station：车次及与其相关站点具体信息 <br>
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/station.PNG)
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/station_index.PNG)
* seat：车次及与其相关的座位信息 <br>
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/seat.PNG)
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/seat_index.PNG)
* user_：用户信息 <br>
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/user_.PNG)
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/user_index.PNG)
* order_：火车票订单信息 <br>
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/order_.PNG)
![](https://github.com/luopoQAQ/goupiao/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1_image/order_index.PNG)

###### 2. 难点SQL脚本 
* 根据出发地(fromCity)、目的地(toCity)、出发日期(date)查询车次信息
  ···
  select t.train_name as trainName
    t.train_id as trainId, 
    s1.station_name as fromStationName, " +
    s2.station_name as toStationName, " +
    s1.station_id as fromStationId, " +
    s2.station_id as toStationId, " +
    s1.arrive_time as fromTime, " +
    s2.arrive_time as toTime " +
  from station as s1 " +
  left join station as s2 " +
    on s1.train_id = s2.train_id and s1.station_id < s2.station_id " +
  left join train t " +
    on s1.train_id = t.train_id " +
  where exists (" +
     select train_id " +
     from train_state ts " +
     where s1.train_id = ts.train_id " +
     and ts.date = #{date} and ts.state = '正常' " +
     ) " +
  and s1.city_name = #{fromCity} and s2.city_name = #{toCity} " +
  order by fromTime ")
  ···
* 根据出发地(fromCity)、目的地(toCity)、出发日期(date)查询**接续换乘**车次信息
  
* 查询余票信息
  
* 生成订单（实际上就是插入一条不含具体数据的order_,只有相关联的ID，具体数据因为涉及到几乎所有表，再在后续插入，使每条订单数据的插入效率更高）



























