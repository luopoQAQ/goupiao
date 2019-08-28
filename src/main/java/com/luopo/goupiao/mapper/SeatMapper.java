package com.luopo.goupiao.mapper;

import com.luopo.goupiao.pojo.Seat;
import com.luopo.goupiao.vo.SeatStockVo;
import com.luopo.goupiao.vo.SeatTypeAndPriceVo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SeatMapper {
    //查询余票数量1(date是sqlDate)
    @Select("select s.seat_type as seat_type, " +
            "count(s.seat_type) as stock " +
            "from seat as s " +
            "where s.train_id = #{trainId} " +
            "and not exists ( " +
            "   select * " +
            "   from order_ " +
            "   where s.seat_id = order_.seat_id" +  //内外查询关联
            "   and order_.train_id = #{trainId} " +
            "   and date = #{date} " +
            "   and ((order_.from_station_id >= #{fromStationId} and order_.from_station_id < #{toStationId}) " +
            "       or (order_.to_station_id > #{fromStationId} and order_.to_station_id <= #{toStationId})) " +
            ") " +
            "group by s.seat_type ")
    List<SeatStockVo> getStock(@Param("trainId") int trainId,
                                      @Param("date") Date date,
                                      @Param("fromStationId") int fromStationId,
                                      @Param("toStationId") int toStationId );
    //查询余票数量2（date是String）
    @Select("select s.seat_type as seat_type, " +
            "count(s.seat_type) as stock " +
            "from seat as s " +
            "where s.train_id = #{trainId} " +
            "and not exists ( " +
            "   select * " +
            "   from order_ " +
            "   where s.seat_id = order_.seat_id" +  //内外查询关联
            "   and order_.train_id = #{trainId} " +
            "   and DATE_FORMAT(order_.date,'%Y-%m-%d') = #{date} " +
            "   and ((order_.from_station_id >= #{fromStationId} and order_.from_station_id < #{toStationId}) " +
            "       or (order_.to_station_id > #{fromStationId} and order_.to_station_id <= #{toStationId})) " +
            ") " +
            "group by s.seat_type ")
    List<SeatStockVo> getStockDateString(@Param("trainId") int trainId,
                                      @Param("fromStationId") int fromStationId,
                                      @Param("toStationId") int toStationId,
                                      @Param("date") String date);

    @Select("select distinct seat_type, price\n" +
            "from seat\n" +
            "where train_id = #{trainId}")
    List<SeatTypeAndPriceVo> getSeatType(@Param("trainId") int trainId);

    @Select("select distinct seat_type\n" +
            "from seat\n" +
            "where train_id = #{trainId}")
    List<String> getSeatTypeByTrainId(@Param("trainId") int trainId);

    @Select("select * " +
            "from seat as s " +
            "where s.train_id = #{trainId} " +
            "and s.seat_type = #{seatType} " +
            "and not exists ( " +
            "   select * " +
            "   from order_ " +
            "   where s.seat_id = order_.seat_id" +  //内外查询关联
            "   and order_.train_id = s.train_id " +
            "   and #{date} = date_format(order_.date,'%Y-%m-%d')" +
            "   and ((order_.from_station_id >= #{fromStationId} and order_.from_station_id < #{toStationId}) " +
            "       or (order_.to_station_id > #{fromStationId} and order_.to_station_id <= #{toStationId})) " +
            ") ")
    List<Seat> getSeatList(
            @Param("trainId") int trainId,
            @Param("fromStationId") int fromStationId,
            @Param("toStationId") int toStationId,
            @Param("seatType") String seatType,
            @Param("date") String date
    );

    @Select("select * " +
            "from seat " +
            "where train_id = #{trainId} and seat_id = #{seatId} ")
    Seat getSeatById(@Param("trainId") int trainId,
            @Param("seatId") int seatId);
}
