package com.korzhueva.android.inertialnavigation.filters;

public interface FilterInterface {
    double update(double current);
    void reset();
}
