package com.sk.traffic.controller;

import com.sk.traffic.domain.model.*;
import com.sk.traffic.domain.model.Intersection;
import com.sk.traffic.domain.service.TrafficLightService;
import com.sk.traffic.dto.*;
import com.sk.traffic.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/traffic")
@Validated
public class TrafficLightApi {

    private final TrafficLightService service;

    public TrafficLightApi(TrafficLightService service) { this.service = service; }

    @PostMapping("/intersections")
    public ResponseEntity<IntersectionSummary> create(@RequestBody(required = false) CreateIntersectionRequest req) {
        String id = (req == null) ? null : req.id();
        Intersection i = service.createIntersection(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(IntersectionSummary.from(i));
    }

    @GetMapping("/intersections")
    public List<IntersectionSummary> list() {
        return service.listIntersections().stream().map(IntersectionSummary::from).collect(Collectors.toList());
    }

    @DeleteMapping("/intersections/{id}")
    public void delete(@PathVariable String id) { service.delete(id); }

    @GetMapping("/intersections/{id}/state")
    public StateResponse state(@PathVariable String id) {
        Intersection isect = service.get(id);
        return new StateResponse(isect.getId(), isect.getStatus(), isect.getLights());
    }

    @GetMapping("/intersections/{id}/history")
    public HistoryResponse history(@PathVariable String id) {
        return new HistoryResponse(service.history(id));
    }

    @PutMapping("/intersections/{id}/plan")
    public void setPlan(@PathVariable String id, @RequestBody TimingPlanRequest req) {
        service.setPlan(id, req.toTimingPlan());
    }

    @PostMapping("/intersections/{id}/pause")
    public void pause(@PathVariable String id) { service.pause(id); }

    @PostMapping("/intersections/{id}/resume")
    public void resume(@PathVariable String id) { service.resume(id); }

    @PostMapping("/intersections/{id}/change")
    public void change(@PathVariable String id, @RequestBody ChangeStateRequest req) {
        service.changeLight(id, req.direction(), req.state());
    }
}
