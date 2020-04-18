package com.korzhueva.android.inertialnavigation.filters;

public class LowPassFilter {
    private double alpha = 0;
    private double prev = 0;

    public LowPassFilter(double coefficient) {
        alpha = coefficient;
    }

    public double update(double current) {
        double next = prev + alpha * (current - prev);

        prev = next;
        return next;
    }
}
