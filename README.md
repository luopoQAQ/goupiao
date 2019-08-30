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
* 根据出发地(fromCity)、目的地(toCity)、出发日期(date)查询车次信息（车次信息经过处理会补充余票信息）
  ```Sql
  select t.train_name as trainName
    t.train_id as trainId, 
    s1.station_name as fromStationName, 
    s2.station_name as toStationName, 
    s1.station_id as fromStationId, 
    s2.station_id as toStationId, 
    s1.arrive_time as fromTime, 
    s2.arrive_time as toTime 
  from station as s1 
  left join station as s2 
    on s1.train_id = s2.train_id and s1.station_id < s2.station_id 
  left join train t 
    on s1.train_id = t.train_id 
  where not exists (
     select train_id 
     from train_state ts 
     where s1.train_id = ts.train_id 
     and ts.date = #{date} and ts.state = '停运' 
  ) 
  and s1.city_name = #{fromCity} and s2.city_name = #{toCity} 
  order by fromTime 
  ```
  
* 根据出发地(fromCity)、目的地(toCity)、出发日期(date)查询**接续换乘**车次信息（查询到这些数据后还需要进一步处理才能转换成前端需要的trainList）
  ```Sql
  select station1.train_id as firstTrainId,
    station1.station_id as firstFromStationId, 
    station2.station_id as firstToStationId, 
    station3.train_id as secondTrainId, 
    station3.station_id as secondFromStationId, 
    station4.station_id as secondToStationId 
  from station as station1 
  left join station as station2 
    on station1.train_id = station2.train_id and station1.station_id < station2.station_id 
  left join station as station3 
    on station2.city_name = station3.city_name and station2.train_id <> station3.train_id 
       and station2.arrive_time < station3.arrive_time 
  left join station as station4  
    on station3.train_id = station4.train_id and station3.station_id < station4.station_id 
  left join train_state as state1  
    on station1.train_id = state1.train_id and DATE_FORMAT(state1.date,'%Y-%m-%d') = #{date} and state1.state = '停运' 
  left join train_state as state4 
    on station4.train_id = state4.train_id and DATE_FORMAT(state4.date,'%Y-%m-%d') = #{date} and state4.state = '停运' 
  where state1.train_id is null and state4.train_id is null 
    and station1.city_name = #{fromCity} and station4.city_name = #{toCity} 
  ```
  
* 查询余票信息
  ```Sql
  select s.seat_type as seat_type, 
  count(s.seat_type) as stock  
  from seat as s  
  where s.train_id = #{trainId}  
  and not exists (  
     select 1  
     from order_  
     where s.seat_id = order_.seat_id 
     and order_.train_id = #{trainId}  
     and DATE_FORMAT(order_.date,'%Y-%m-%d') = #{date}  
     and ((order_.from_station_id >= #{fromStationId} and order_.from_station_id < #{toStationId})  
         or (order_.to_station_id > #{fromStationId} and order_.to_station_id <= #{toStationId})) 
  )
  group by s.seat_type ")
  ```
  
* 生成订单（实际上就是插入一条不含具体数据的order_,只有相关联的ID，具体数据因为涉及到几乎所有表，再在后续插入，使每条订单数据的插入效率更高）
  ```Sql
  insert into order_ (user_id, user_name, id_card, telephone, real_name, 
    train_id, from_station_id, to_station_id, seat_id, date ) 
  values ( #{user.userId}, #{user.userName}, 
    #{user.idCard}, #{user.telephone}, #{user.realName},
    #{trainId}, #{fromStationId}, #{toStationId}, #{seatId}, STR_TO_DATE(#{date},'%Y-%m-%d') ) 
  ```
  
###### 3. 基本架构及后端具体实现
> 用户访问 http://49.235.18.252/index 地址，实际上访问的是Nginx服务器 <br>
> 对于静态文件(.js/.css/.htm/.jpeg等)，直接访问本地缓存（采用**nginx代理缓存**，而不是动静态分离） <br>
> 对于其他文件则采用**负载均衡**，默认使用轮询方式访问部署好的两个tomcat服务器（8080与8081端口下） <br>
> 具体细节问题的解决与实现如下： <br>

* 首页（index接口与home.html） <br>
  * 参数校验： <br>
  因为前端的校验无法防止恶意用户，为了避免恶意用户对输入的查询参数（出发地、目的地、出发日期）恶意选择，在系统初始化的时候，会直接从数据库select所有的城市（对于某些城市可能确实存在，但是数据库中没有（懒得插入太多数据了），也会在参数校验时不通过）并保存本地，对于参数出发地、目的地一定是存在于该本地cityList中，如果不存在，则参数校验失败，通过AJAX返回给前端一个error；对于日期同样也只能查询次日至未来30日内。 <br>
  具体实现通过@Constraint(validatedBy={Validator.class})注解定义自定义校验注解 与 实现ConstraintValidator<IsCity, String>中isValid的具体校验方法。 <br>

* 车次信息页（trainList/trainListZhongzhuan接口 与 train_list/train_list_zhongzhuan.html页面） <br>
  * 页面缓存 <br>
  利用thymeleafViewResolver.getTemplateEngine().process()手动渲染thymeleaf模板，对车次信息页面进行缓存 <br>
  利用redis缓存时，键值为出发地城市、目的地、日期 <br>
  
  * 缓存穿透 <br>
  因为采用了参数校验，所以完全避免了用户恶意访问，造成缓存穿透，给数据库造成 太大压力的问题 <br>

* 详细信息页（trainDetail接口 与 train_detail.htm页面） <br>[GoupiaoController.java](https://github.com/luopoQAQ/goupiao/blob/master/src/main/java/com/luopo/goupiao/controller/GoupiaoController.java)[GoupiaoService](https://github.com/luopoQAQ/goupiao/blob/master/src/main/java/com/luopo/goupiao/service/GoupiaoService.java)
  * 页面静态化 <br>
  train_detail页面实现了完全的静态化（相对于其他页面采用thymeleaf模板来说），因此次页面可以缓存在nginx中，对于具体车次详细数据的获取采用AJAX，对于进一步处理（购票接口等）更为方便 <br>
  
  * 验证码 <br>
  验证码生成用的是swing，网上一搜就有（友情提示：因为验证码所需要的歪歪斜斜的Candara字体是windows下的，如果想在linux系统上也正常显示的话，也要在linux上自己传一个），对于验证码采用了一个访问限制（10s内只能访问15次），防止恶意刷码 <br>
  
  * 接口防刷 <br>
  接口防刷的具体实现使用了redis <br>
  对于用户的访问，在拦截器阶段进行拦截，handlerMethod.getMethodAnnotation(AccessLimit.class)获取AccessLimit注解（一个自定义注解，三个值分别标记了登陆限制、时间限制、登陆次数限制），如果有时间限制与登录次数限制，则直接从redis获取userId作为key的访问次数值，如果超过限制次数，则直接在拦截器拦截，return false <br>
  
  * 如何限制单位时间内访问次数？ <br>
  利用redis的有效期，如果有效期过去，该值自然会失效，那么说明未访问，可以设置访问值为1；有效值未过去，判断是否超出次数限制，如果超出，则访问失败，没有则+1，并写入 <br>
  
  * 购票接口隐藏 <br>
  如果通过验证码验证，则随机生成一串UUID，同时设置该UUID缓存，并返回 <br>
  前端接受该UUID，在购票接口地址后加上该串，再访问 <br>
  后端控制层从缓存取出对应用户的UUID，验证是否正确，如果错误，说明是恶意访问，直接返回一个error，对于该接口也注解了限制访问，结合之下可以解决用户恶意刷票问题 <br>
  
* 购票接口（/goupiao/UUID/do）
  * 应对高并发情况下的巨大流量，主要是缓解数据库访问的限制 <br>
  这里采用hasNoStockMap本地标识、GoupiaoKey.getStockOnArea redis余票标记预减库存、GoupiaoKey.isDBNoStock redis对于数据库有无票进行标记，三层结构来减少到达数据库的访问 <br>
  同时采用RabbitMQ对访问进行削峰，使巨大流量到达数据库更为均衡（12306的访问也是漫长的等待，那个小圈圈就这么转啊转啊，不知道是不是也是用的消息队列？）
  
  * 第一层屏障**hasNoStockMap**本地标识 <br>
  系统启东时，由于实现InitializingBean接口的afterPropertiesSet()方法，这里直接标识一波hasNoStockMap，没票则为true，用户访问时也是先检查hasNoStockMap，如果为true则直接不往下访问了 <br>
  
  * 第二层屏障**GoupiaoKey.getStockOnArea redis**预减库存 <br>
  同样是系统启东时会初始化所有余票，访问到达这里，会将库存-1（decr），如果-1后< 0，则说明库存一定不足了，此处标识hasNoStockMap为true；否则加入消息队列，削峰后交由数据库处理 <br>
  
  **为什么不在这里解决超卖？** <br>
  确实，可以**利用redis的setNX分布式锁**，在这里解决预减库存减多了问题，即直接解决了超卖问题，但是这样会大大影响了这一层的访问效率（利用jmeter简单测试了下，大概是慢了2.5倍，这还是我的电脑配置差，并发只有4000的情况下，5000就直接oom了，记住一定不要买商务本，板载内存条，想加只能换电脑，心痛！），相对来说这里更应该不采用锁的形式，即使这样，到达数据库的流量至多也就多出2-3倍，这对于数据库来说不是很大的问题，毕竟还有削峰处理 <br>
  
  * 第三层屏障**GoupiaoKey.isDBNoStock redis对于数据库有无票进行标记** <br>
  当消息队列取出消息进行处理，会进入一个循环不断尝试，而循环结束的标志，就是生成车票订单（抢到票）/GoupiaoKey.isDBNoStock标志为false <br>
  而此标志的设置是在生成订单中，如果某个访问的某次迭代中，该标志不为true，则说明暂有余票，那就尝试创建订单 <br>
  
  订单的创建是一个事物（为了方便直接串行化了，这样可以避免幻读（防止别的用户也插入订单，这就造成了超卖了），其实与子查询也是一样的），如果查询余票时直接为0，那么就修改该标志为true，则再一次阻拦其他用户（在迭代中的）创建订单，同时自己也购票失败（因为余票不足） <br>
  
  * 消息队列 <br>
  在第二层后进行削峰 <br>
  
  * 生成订单 <br>
  分成两步：串行化中，只插入与其他表关联id；串行化结束，再择机（看数据库喽）插入具体信息（因为订单的东西包含太多了，都放在一个事物里必定慢死） <br>
  
  * **解决超卖问题** <br>
  相信数据库！交给数据库！事物+唯一索引（防止用户重复购票、不用用户重复购买同一区间同一seat） <br>
  
  * 一个小优化 <br>
  用户在尝试插入订单的时候，seatId是随机的，为什么不能按顺序来？因为相同用户插入同一setatId肯定只能一个成功，其他的都必须重新轮询，这样无疑会造成更多开销，去12306试了一下，他们也是随机的哈哈（至于动车的指定座位功能，有想实现的可以帮忙完善一下） <br>
  
* 查询订单页面（order接口与order.html页面） <br>
  * 根据用户userId查询订单 <br>
  直接查询订单并显示，没啥好说的，根据购票日期排了个序感觉更舒服了 <br>
  
  * 退票 <br>
  只有为出行的可以退票（发现order表的state字段没用上，其实order的state应该在这里标识的，而不是在前端简单判断一下，如果有幸你看到这里并且想完善可以私戳我） <br>
  退票时对于相应的三层标记都要清除哦！先写数据库，再该缓存很重要！（这里其实是有一些小问题的，但概率很小且不咋影响，就没有细处理了） <br>
  
  * 项目都部署好了，又发现一个小问题：  <br>
  其实是大问题！退票不能直接删除订单啊aa!这里真的需要再优化一下了，怎么可以删除订单呢？应该是修改订单状态（比如退票），这样就不会显示在前端，也不会小时这个数据条了，比较任何数据都很重要！ <br>
  
* 登录页面 <br>
  * redis + token + 拦截器 + 参数解析（resolveArgument）解决集群下登录问题 <br>
  对于每个用户，访问时会根据cookie提取token，验证token与redis存储的是否相同，相同则得到user信息，通过实现HandlerMethodArgumentResolver接口的resolveArgument()方法，自动填充控制层接口参数user，方便，还能解决分布式下的session问题，巴适！ <br>
  对于用户密码，老生常谈，网上看的最多的就是MD5两次加密，没啥好说的，不懂得可以直接搜一下，确实有用。 <br>
  
* 注册页面 <br>
  * 注册，参数校验，插入用户信息。。。没啥好说的 <br>

>暂时就这些，如果有好的改良方法或者疑问，可以戳我QQ：2190448693

  
  

  

























