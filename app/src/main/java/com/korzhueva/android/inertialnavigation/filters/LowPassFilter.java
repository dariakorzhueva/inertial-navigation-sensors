package com.korzhueva.android.inertialnavigation.filters;

public class LowPassFilter {
    private double alpha = 0;
    private double prev = 0;
    private boolean begin = true;

    public LowPassFilter(double coefficient) {
        alpha = coefficient;
    }

    public double update(double current) {
        if(begin) {
            prev = current;
            begin = false;
        }

        double next = prev + alpha * (current - prev);

        prev = next;
        return next;
    }
}
