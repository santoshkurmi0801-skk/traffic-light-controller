package com.sk.traffic.domain.model;

import java.time.Instant;

public record StateChange(String intersectionId, Direction direction, TrafficLightState state, Instant at) {}
