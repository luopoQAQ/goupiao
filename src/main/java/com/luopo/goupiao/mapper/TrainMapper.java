package com.luopo.goupiao.mapper;

import com.luopo.goupiao.pojo.Train;
import com.luopo.goupiao.vo.TrainVo;
import com.luopo.goupiao.vo.ZhongzhuanVo;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.List;

@Mapper
public interface TrainMapper {
//    @Select("select train.trainName, s1.stationName, s2.stationName, s1.arriveTime, s2.arriveTime," +
//            "from station s1, station as s2, train \n" +
//            "where s1.cityName = #{fromCity} " +
//                "and s2.city_name = #{toCity} " +
//                "and s1.trainId = s2.trainId " +
//                "and s1.stationId < s2.stationId " +
//                "and s1.trainId = train.trainId " +
//                "and s1.trainId not exists ( \n" +
//                    "select trainId \n" +
//                    "from trainStatus \n" +
//                    "where s1.trainId == train.trainId" +
//                        "and date = #{date} " +
//                        "and status = '停运')\n" )

//    @Results({
//            @Result(property = "trainName", column = "train.train_name"),
//            @Result(property = "fromStationName", column = "s1.station_name"),
//            @Result(property = "toStationName", column = "s2.station_name"),
//            @Result(property = "fromTime", column = "s1.arrive_time"),
//            @Result(property = "toTime", column = "s2.arrive_time")
//    }
//    )

    @Select("select t.train_name as trainName, " +
            "t.train_id as trainId, " +
            "s1.station_name as fromStationName, " +
            "s2.station_name as toStationName, " +
            "s1.station_id as fromStationId, " +
            "s2.station_id as toStationId, " +
            "s1.arrive_time as fromTime, " +
            "s2.arrive_time as toTime " +
            "from station as s1 " +
            "left join station as s2 " +
            "on s1.train_id = s2.train_id and s1.station_id < s2.station_id " +
            "left join train t " +
            "on s1.train_id = t.train_id " +
            "where exists (" +
            "   select train_id " +
            "   from train_state ts " +
            "   where s1.train_id = ts.train_id " +
            "   and ts.date = #{date} and ts.state = '正常' " +
            "   ) " +
            "and s1.city_name = #{fromCity} and s2.city_name = #{toCity} " +
            "order by fromTime ")
    public List<TrainVo> listTrainVo(@Param("fromCity") String fromCity,
                                     @Param("toCity") String toCity,
                                     @Param("date") java.sql.Date date);

    @Select("select * " +
            "from train " +
            "where train_id = #{trainId} ")
    Train getTrainById(@Param("trainId") int trainId);


    @Select("select distinct train_id " +
            "from train")
    List<Integer> getAllTrainId();

    //6表连接station1为出发站，station4为重点站，station2为中转站到达时，station3为中转站再次上车时
    @Select("select station1.train_id as firstTrainId,\n" +
            "\tstation1.station_id as firstFromStationId,\n" +
            "\tstation2.station_id as firstToStationId,\n" +
            "\tstation3.train_id as secondTrainId,\n" +
            "\tstation3.station_id as secondFromStationId,\n" +
            "\tstation4.station_id as secondToStationId\n" +
            "from station as station1\n" +
            "left join station as station2\n" +
            "\ton station1.train_id = station2.train_id and station1.station_id < station2.station_id\n" +
            "left join station as station3\n" +
            "\ton station2.city_name = station3.city_name and station2.train_id <> station3.train_id\n" +
            "\t\tand station2.arrive_time < station3.arrive_time\n" +
            "left join station as station4 \n" +
            "\ton station3.train_id = station4.train_id and station3.station_id < station4.station_id\n" +
            "left join train_state as state1 \n" +
            "\ton station1.train_id = state1.train_id and DATE_FORMAT(state1.date,'%Y-%m-%d') = #{date} and state1.state = '停运'\n" +
            "left join train_state as state4\n" +
            "\ton station4.train_id = state4.train_id and DATE_FORMAT(state4.date,'%Y-%m-%d') = #{date} and state4.state = '停运'\n" +
            "where state1.train_id is null and state4.train_id is null\n" +
            "and station1.city_name = #{fromCity} and station4.city_name = #{toCity}\n")
    List<ZhongzhuanVo> listTrainVoZhongzhuan(@Param("fromCity") String fromCity,
                                             @Param("toCity") String toCity,
                                             @Param("date") String date);


    @Select("select t.train_name as trainName, " +
            "t.train_id as trainId, " +
            "s1.station_name as fromStationName, " +
            "s2.station_name as toStationName, " +
            "s1.station_id as fromStationId, " +
            "s2.station_id as toStationId, " +
            "s1.city_name as fromCity, " +
            "s2.city_name as toCity, " +
            "s1.arrive_time as fromTime, " +
            "s2.arrive_time as toTime " +
            "from train as t, station as s1, station as s2 " +
            "where t.train_id = #{trainId} " +
            "and s1.train_id = #{trainId} and s1.station_id = #{fromStationId} " +
            "and s2.train_id = #{trainId} and s2.station_id = #{toStationId} ")
    TrainVo getTrainZhongzhuan(@Param("trainId") int trainId,
                               @Param("fromStationId") int fromStationId,
                               @Param("toStationId") int toStationId);
}
