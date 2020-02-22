package com.korzhueva.android.inertialnavigation.filters;

public class LowPassFilter {
    private float alpha = 0;
    public LowPassFilter(float coefficient){
        alpha = coefficient;
    }

    public double update(double current, double prev){
        double next =  prev+alpha*(current-prev);
        return next;
    }
}
