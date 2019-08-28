package com.luopo.goupiao.service;

import com.luopo.goupiao.mapper.StationMapper;
import com.luopo.goupiao.pojo.Station;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    @Autowired
    private StationMapper stationMapper;

    public List<Station> getAllStation() {
        return stationMapper.getAllStation();
    }


    public List<String> getAllCity() {
        return stationMapper.getAllCity();
    }

    public Station getStationByTrainIdAndStationId(int trainId, int fromStationId) {
        return stationMapper.getStationByTrainIdAndStationId(trainId, fromStationId);
    }

    public List<Integer> getStationListByTrainId(int trainId) {
        return stationMapper.getStationListByTrainId(trainId);
    }
}
