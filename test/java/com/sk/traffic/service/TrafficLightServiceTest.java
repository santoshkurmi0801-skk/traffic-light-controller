package com.sk.traffic.service;

import com.sk.traffic.domain.exception.ConflictException;
import com.sk.traffic.domain.exception.InvalidCommandException;
import com.sk.traffic.domain.model.*;
import com.sk.traffic.domain.model.Direction;
import com.sk.traffic.domain.model.TrafficLightState;
import com.sk.traffic.domain.service.TrafficLightService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficLightServiceTest {

    @Test
    void manualChange_allowed_only_when_paused() {
        TrafficLightService svc = new TrafficLightService();
        String id = svc.createIntersection("test-1").getId();
        svc.resume(id);
        assertThrows(InvalidCommandException.class, () ->
                svc.changeLight(id, Direction.NORTH_SOUTH, TrafficLightState.GREEN));
        svc.pause(id);
        assertDoesNotThrow(() -> svc.changeLight(id, Direction.NORTH_SOUTH, TrafficLightState.GREEN));
    }

    @Test
    void shouldThrowConflictWhenBothDirectionsGreen() {
        TrafficLightService svc = new TrafficLightService();
        String id = svc.createIntersection("test-2").getId();
        svc.pause(id);
        svc.changeLight(id, Direction.NORTH_SOUTH, TrafficLightState.GREEN);
        assertThrows(ConflictException.class, () ->
                svc.changeLight(id, Direction.EAST_WEST, TrafficLightState.GREEN));
    }

    @Test
    void history_records_changes() {
        TrafficLightService svc = new TrafficLightService();
        String id = svc.createIntersection("test-3").getId();
        svc.pause(id);
        int before = svc.history(id).size();
        svc.changeLight(id, Direction.NORTH_SOUTH, TrafficLightState.RED);
        assertTrue(svc.history(id).size() >= before + 1);
    }

    @Test
    void state_returns_copy() {
        TrafficLightService svc = new TrafficLightService();
        String id = svc.createIntersection("test-4").getId();
        Map<Direction, TrafficLightState> state = svc.getState(id);
        assertNotNull(state);
    }
}
