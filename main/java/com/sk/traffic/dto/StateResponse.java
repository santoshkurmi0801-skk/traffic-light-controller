package com.sk.traffic.dto;

import com.sk.traffic.domain.model.ControllerStatus;
import com.sk.traffic.domain.model.Direction;
import com.sk.traffic.domain.model.TrafficLightState;

import java.util.Map;

public record StateResponse(String intersectionId, ControllerStatus status,
                            Map<Direction, TrafficLightState> state) {}
