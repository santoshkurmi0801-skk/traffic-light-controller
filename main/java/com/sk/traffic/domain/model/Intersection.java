package com.sk.traffic.domain.model;

import java.util.EnumMap;
import java.util.Map;

public class Intersection {
    private final String id;
    private final Map<Direction, TrafficLightState> lights = new EnumMap<>(Direction.class);
    private ControllerStatus status = ControllerStatus.PAUSED; // start paused until explicitly resumed
    private TimingPlan plan = new TimingPlan();

    public Intersection(String id) {
        this.id = id;
        lights.put(Direction.NORTH_SOUTH, TrafficLightState.RED);
        lights.put(Direction.EAST_WEST, TrafficLightState.RED);
    }

    public String getId() { return id; }
    public Map<Direction, TrafficLightState> getLights() { return lights; }
    public ControllerStatus getStatus() { return status; }
    public void setStatus(ControllerStatus status) { this.status = status; }
    public TimingPlan getPlan() { return plan; }
    public void setPlan(TimingPlan plan) { this.plan = plan; }
}
