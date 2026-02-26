package com.sk.traffic.dto;

import com.sk.traffic.domain.model.ControllerStatus;
import com.sk.traffic.domain.model.Direction;
import com.sk.traffic.domain.model.Intersection;
import com.sk.traffic.domain.model.TrafficLightState;

import java.util.Map;

public record IntersectionSummary(String id, ControllerStatus status,
                                  Map<Direction, TrafficLightState> state) {
    public static IntersectionSummary from(Intersection i) {
        return new IntersectionSummary(i.getId(), i.getStatus(), i.getLights());
    }
}
