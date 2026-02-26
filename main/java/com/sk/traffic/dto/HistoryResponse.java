package com.sk.traffic.dto;

import com.sk.traffic.domain.model.StateChange;

import java.util.List;

public record HistoryResponse(List<StateChange> history) {}
