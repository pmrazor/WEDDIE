package com.ssafy.exhi.domain.virginroad.service;


import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadRequest;
import com.ssafy.exhi.domain.virginroad.model.dto.VirginRoadResponse;

public interface VirginRoadService {
    VirginRoadResponse.SimpleResultDTO createVirginRoad(VirginRoadRequest.CreateDTO createDTO);
}
