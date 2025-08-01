package org.example.phmonitor.model;

public class PhModel {
    private double value;

    // No-arg constructor for JSON deserialization
    public PhModel() {}

    public PhModel(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
