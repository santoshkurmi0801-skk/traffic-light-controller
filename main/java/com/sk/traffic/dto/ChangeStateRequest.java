package com.sk.traffic.dto;

import com.sk.traffic.domain.model.Direction;
import com.sk.traffic.domain.model.TrafficLightState;

public record ChangeStateRequest(Direction direction, TrafficLightState state) {}
