package com.ecosense.app.pojo;

import java.util.List;

public class LegsObject {

    private List<StepsObject> steps;

    private DistanceObject distance;

    private DurationObject duration;

    public LegsObject() {}

    public List<StepsObject> getSteps() {
        return steps;
    }

    public void setSteps(List<StepsObject> steps) {
        this.steps = steps;
    }

    public DistanceObject getDistance() {
        return distance;
    }

    public void setDistance(DistanceObject distance) {
        this.distance = distance;
    }

    public DurationObject getDuration() {
        return duration;
    }

    public void setDuration(DurationObject duration) {
        this.duration = duration;
    }
}