package com.sk.traffic.domain.service;

import com.sk.traffic.domain.exception.ConflictException;
import com.sk.traffic.domain.exception.InvalidCommandException;
import com.sk.traffic.domain.exception.NotFoundException;
import com.sk.traffic.domain.model.*;
import com.sk.traffic.domain.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class TrafficLightService {

    private final Map<String, Intersection> intersections = new ConcurrentHashMap<>();
    private final Map<String, ScheduledExecutorService> schedulers = new ConcurrentHashMap<>();
    private final Map<String, List<StateChange>> histories = new ConcurrentHashMap<>();

    public synchronized Intersection createIntersection(String id) {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (intersections.containsKey(id)) {
            throw new InvalidCommandException("Intersection already exists: " + id);
        }
        Intersection isect = new Intersection(id);
        intersections.put(id, isect);
        histories.put(id, new CopyOnWriteArrayList<>());
        return isect;
    }

    public List<Intersection> listIntersections() {
        return new ArrayList<>(intersections.values());
    }

    public Intersection get(String id) {
        Intersection isect = intersections.get(id);
        if (isect == null) throw new NotFoundException("Intersection not found: " + id);
        return isect;
    }

    public void delete(String id) {
        pause(id);
        intersections.remove(id);
        histories.remove(id);
        ScheduledExecutorService ex = schedulers.remove(id);
        if (ex != null) ex.shutdownNow();
    }

    public void changeLight(String id, Direction dir, TrafficLightState state) {
        Intersection isect = get(id);
        if (isect.getStatus() != ControllerStatus.PAUSED) {
            throw new InvalidCommandException("Manual changes are allowed only when PAUSED");
        }
        synchronized (isect) {
            validateNoConflict(isect, dir, state);
            isect.getLights().put(dir, state);
            record(id, new StateChange(id, dir, state, Instant.now()));
        }
    }

    public void setPlan(String id, TimingPlan plan) {
        Intersection isect = get(id);
        synchronized (isect) { isect.setPlan(plan); }
        // If running, restart scheduler to apply new plan
        if (isect.getStatus() == ControllerStatus.RUNNING) {
            pause(id);
            resume(id);
        }
    }

    public void resume(String id) {
        Intersection isect = get(id);
        synchronized (isect) {
            if (isect.getStatus() == ControllerStatus.RUNNING) return;
            isect.setStatus(ControllerStatus.RUNNING);
            // initialize to NS GREEN, EW RED to start cycle deterministically
            isect.getLights().put(Direction.NORTH_SOUTH, TrafficLightState.GREEN);
            isect.getLights().put(Direction.EAST_WEST, TrafficLightState.RED);
            record(id, new StateChange(id, Direction.NORTH_SOUTH, TrafficLightState.GREEN, Instant.now()));
            startScheduler(id, isect);
        }
    }

    public void pause(String id) {
        Intersection isect = get(id);
        synchronized (isect) {
            if (isect.getStatus() == ControllerStatus.PAUSED) return;
            isect.setStatus(ControllerStatus.PAUSED);
        }
        ScheduledExecutorService ex = schedulers.remove(id);
        if (ex != null) ex.shutdownNow();
    }

    public Map<Direction, TrafficLightState> getState(String id) {
        return new EnumMap<>(get(id).getLights());
    }

    public List<StateChange> history(String id) {
        return histories.getOrDefault(id, List.of());
    }

    private void startScheduler(String id, Intersection isect) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cycle-" + id);
            t.setDaemon(true);
            return t;
        });
        schedulers.put(id, executor);
        scheduleCycle(id, isect, executor);
    }

    private void scheduleCycle(String id, Intersection isect, ScheduledExecutorService executor) {
        Runnable cycle = new Runnable() {
            @Override public void run() {
                try {
                    if (isect.getStatus() != ControllerStatus.RUNNING) return;
                    TimingPlan p;
                    synchronized (isect) { p = isect.getPlan(); }

                    // Phase 1: NS GREEN, EW RED (already set when resume)
                    sleep(p.getNsGreenMs());
                    // Phase 2: NS YELLOW, EW RED
                    synchronized (isect) { setPair(isect, Direction.NORTH_SOUTH, TrafficLightState.YELLOW, Direction.EAST_WEST, TrafficLightState.RED, id); }
                    sleep(p.getNsYellowMs());
                    // Phase 3: NS RED, EW GREEN
                    synchronized (isect) { setPair(isect, Direction.NORTH_SOUTH, TrafficLightState.RED, Direction.EAST_WEST, TrafficLightState.GREEN, id); }
                    sleep(p.getEwGreenMs());
                    // Phase 4: NS RED, EW YELLOW
                    synchronized (isect) { setPair(isect, Direction.NORTH_SOUTH, TrafficLightState.RED, Direction.EAST_WEST, TrafficLightState.YELLOW, id); }
                    sleep(p.getEwYellowMs());
                    // Loop back to Phase 1
                    synchronized (isect) { setPair(isect, Direction.NORTH_SOUTH, TrafficLightState.GREEN, Direction.EAST_WEST, TrafficLightState.RED, id); }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        // Schedule with fixed delay equals 0; we'll reschedule after each run to maintain sequence durations within run()
        executor.scheduleWithFixedDelay(cycle, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void setPair(Intersection isect, Direction d1, TrafficLightState s1, Direction d2, TrafficLightState s2, String id) {
        validateNoConflict(isect, d1, s1, d2, s2);
        isect.getLights().put(d1, s1);
        isect.getLights().put(d2, s2);
        record(id, new StateChange(id, d1, s1, Instant.now()));
        record(id, new StateChange(id, d2, s2, Instant.now()));
    }

    private void record(String id, StateChange sc) {
        histories.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(sc);
    }

    private void validateNoConflict(Intersection isect, Direction dir, TrafficLightState state) {
        Direction opp = (dir == Direction.NORTH_SOUTH) ? Direction.EAST_WEST : Direction.NORTH_SOUTH;
        TrafficLightState oppState = isect.getLights().get(opp);
        if (state == TrafficLightState.GREEN && oppState == TrafficLightState.GREEN) {
            throw new ConflictException("Conflicting directions cannot be GREEN simultaneously");
        }
    }

    private void validateNoConflict(Intersection isect, Direction d1, TrafficLightState s1, Direction d2, TrafficLightState s2) {
        if (s1 == TrafficLightState.GREEN && s2 == TrafficLightState.GREEN) {
            throw new ConflictException("Conflicting directions cannot be GREEN simultaneously");
        }
    }

    private static void sleep(long ms) throws InterruptedException {
        if (ms > 0) Thread.sleep(ms);
    }
}
