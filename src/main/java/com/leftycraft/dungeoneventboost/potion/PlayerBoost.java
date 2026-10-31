package com.leftycraft.dungeoneventboost.potion;

import com.leftycraft.dungeoneventboost.event.EventType;

public class PlayerBoost {

    private final EventType type;
    private long endTime;
    private long totalDurationSeconds;
    private double customMultiplier;

    public PlayerBoost(EventType type, long endTime, long totalDurationSeconds, double customMultiplier) {
        this.type = type;
        this.endTime = endTime;
        this.totalDurationSeconds = totalDurationSeconds;
        this.customMultiplier = customMultiplier;
    }

    public EventType getType() {
        return type;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public double getCustomMultiplier() {
        return customMultiplier;
    }

    public void setCustomMultiplier(double customMultiplier) {
        this.customMultiplier = customMultiplier;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }

    public long getRemainingSeconds() {
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000L);
    }

    public void extendDuration(long additionalSeconds) {
        long baseTime = Math.max(System.currentTimeMillis(), this.endTime);
        this.endTime = baseTime + (additionalSeconds * 1000L);
        this.totalDurationSeconds += additionalSeconds;
    }

    public void replaceDuration(long newDurationSeconds) {
        this.endTime = System.currentTimeMillis() + (newDurationSeconds * 1000L);
        this.totalDurationSeconds = newDurationSeconds;
    }
}
