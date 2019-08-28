package com.luopo.goupiao.mapper;

import com.luopo.goupiao.pojo.Order;
import com.luopo.goupiao.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("select *\n" +
            "from order_\n" +
            "where user_id = #{userId}\n" +
            "and train_id = #{trainId}\n" +
            "and from_station_id = #{fromStationId}\n" +
            "and to_station_id = #{toStationId}\n" +
            "and date = DATE_FORMAT(#{date},'%Y-%m-%d')")
    public Order getOrder(@Param("userId") int userId,
                          @Param("trainId") int trainId,
                          @Param("fromStationId") int fromStationId,
                          @Param("toStationId") int toStationId,
                          @Param("date") String date);

    @Insert("insert into order_ (user_id, user_name, id_card, telephone, real_name, " +
            "train_id, from_station_id, to_station_id, seat_id, date ) " +
            "values ( #{user.userId}, #{user.userName}, " +
            "#{user.idCard}, #{user.telephone}, #{user.realName}, " +
            "#{trainId}, #{fromStationId}, #{toStationId}, #{seatId}, " +
            " STR_TO_DATE(#{date},'%Y-%m-%d') )  " )
    void addOrder(@Param("user") User user,
            @Param("trainId") int trainId,
            @Param("fromStationId") int fromStationId,
            @Param("toStationId") int toStationId,
            @Param("seatId") int seatId,
            @Param("date") String date);

    @Insert("update order_ " +
            "set train_name = #{order.trainName}, " +
            "   price = #{order.price}, " +
            "   seat_type = #{order.seatType}, " +
            "   seat_location =  #{order.seatLocation}, " +
            "   from_station_name = #{order.fromStationName}, " +
            "   to_station_name = #{order.toStationName} ," +
            "   from_time = #{order.fromTime}, " +
            "   state = #{order.state}, " +
            "   carriage = #{order.carriage} " +
            "where order_id = #{order.orderId} ")
    void updateOrder(@Param("order") Order order);


    @Select("select * " +
            "from order_ " +
            "where order_.user_id = #{userId} " +
            "order by order_.create_date desc ")
    List<Order> getOrderByUser(int userId);

    @Delete("delete " +
            "from order_ " +
            "where order_id = #{orderId} ")
    void deleteOrder(@Param("orderId") int orderId);

    @Select("select * " +
            "from order_ " +
            "where order_.user_id = #{userId} " +
            "and DATE_FORMAT(order_.date,'%Y-%m-%d') = #{date} " +
            "and order_.train_id = #{trainId} ")
    Order getOrderByUserAndDate(@Param("userId") int userId,
                                @Param("date") String date,
                                @Param("trainId") int trainId);
}
