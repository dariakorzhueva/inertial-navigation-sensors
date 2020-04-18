package com.korzhueva.android.inertialnavigation.filters;

public class LowPassFilter {
    private double alpha = 0;
    public LowPassFilter(double coefficient){
        alpha = coefficient;
    }

    public double update(double current, double prev){
        double next =  prev+alpha*(current-prev);
        return next;
    }
}
