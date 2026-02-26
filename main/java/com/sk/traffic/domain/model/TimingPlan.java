package com.sk.traffic.domain.model;

public class TimingPlan {
    private long nsGreenMs = 30000;
    private long nsYellowMs = 4000;
    private long ewGreenMs = 30000;
    private long ewYellowMs = 4000;

    public TimingPlan() {}

    public TimingPlan(long nsGreenMs, long nsYellowMs, long ewGreenMs, long ewYellowMs) {
        this.nsGreenMs = nsGreenMs;
        this.nsYellowMs = nsYellowMs;
        this.ewGreenMs = ewGreenMs;
        this.ewYellowMs = ewYellowMs;
    }

    public long getNsGreenMs() { return nsGreenMs; }
    public long getNsYellowMs() { return nsYellowMs; }
    public long getEwGreenMs() { return ewGreenMs; }
    public long getEwYellowMs() { return ewYellowMs; }

    public void setNsGreenMs(long v) { this.nsGreenMs = v; }
    public void setNsYellowMs(long v) { this.nsYellowMs = v; }
    public void setEwGreenMs(long v) { this.ewGreenMs = v; }
    public void setEwYellowMs(long v) { this.ewYellowMs = v; }
}
