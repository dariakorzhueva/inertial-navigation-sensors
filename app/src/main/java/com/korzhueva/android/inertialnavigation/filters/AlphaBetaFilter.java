package com.korzhueva.android.inertialnavigation.filters;

public class AlphaBetaFilter implements FilterInterface{
    private double dt = 0.5;
    private double ak1 = 0, jk1 = 0, a = 0.85, b = 0.005;
    private double ak, jk, rk;

    public AlphaBetaFilter(double dt, double ak1, double jk1, double a, double b) {
        this.dt = dt;
        this.ak1 = ak1;
        this.jk1 = jk1;
        this.a = a;
        this.b = b;
    }

    public double update(double current) {
        ak = ak1 + (jk1 * dt);
        jk = jk1;

        rk = current - ak;

        ak += a * rk;
        jk += (b * rk) / dt;

        ak1 = ak;
        jk1 = jk;

        return ak1;
    }

    public void reset() {
        dt = 0;
        ak1 = 0;
        jk1 = 0;
        a = 0;
        b = 0;
    }
}
