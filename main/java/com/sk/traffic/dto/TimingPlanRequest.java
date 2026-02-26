package com.sk.traffic.dto;

import com.sk.traffic.domain.model.TimingPlan;

public record TimingPlanRequest(Long nsGreenMs, Long nsYellowMs, Long ewGreenMs, Long ewYellowMs) {
    public TimingPlan toTimingPlan() {
        long nsG = nsGreenMs == null ? 30000 : nsGreenMs;
        long nsY = nsYellowMs == null ? 4000 : nsYellowMs;
        long ewG = ewGreenMs == null ? 30000 : ewGreenMs;
        long ewY = ewYellowMs == null ? 4000 : ewYellowMs;
        return new TimingPlan(nsG, nsY, ewG, ewY);
    }
}
