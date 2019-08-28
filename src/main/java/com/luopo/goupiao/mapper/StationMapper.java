package com.luopo.goupiao.mapper;

import com.luopo.goupiao.pojo.Station;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StationMapper {


    @Select("SELECT DISTINCT city_name\n" +
            "FROM station;")
    List<String> getAllCity();

    @Select("select * " +
            "from station " +
            "where train_id = #{trainId} " +
            "and station_id = #{fromStationId} ")
    Station getStationByTrainIdAndStationId(@Param("trainId") int trainId,
                                            @Param("fromStationId") int fromStationId);

    @Select("SELECT * " +
            "FROM station ")
    List<Station> getAllStation();

    @Select("select station_id " +
            "from station " +
            "where train_id = #{trainId} ")
    List<Integer> getStationListByTrainId(@Param("trainId") int trainId);
}
