package com.korzhueva.android.inertialnavigation.filters;

public class AlphaBetaFilter {
    private double dt = 0.5;
    private double xk1 = 0, vk1 = 0, a = 0.85, b = 0.005;
    private double xk, vk, rk;

    public AlphaBetaFilter() {

    }

    public AlphaBetaFilter(double dt, double xk1, double vk1, double a, double b) {
        this.dt = dt;
        this.xk1 = xk1;
        this.vk1 = vk1;
        this.a = a;
        this.b = b;
    }

    public double update(double current) {
        xk = xk1 + (vk1 * dt);
        vk = vk1;

        rk = current - xk;

        xk += a * rk;
        vk += (b * rk) / dt;

        xk1 = xk;
        vk1 = vk;

        return xk1;
    }

    public void reset() {
        dt = 0;
        xk1 = 0;
        vk1 = 0;
        a = 0;
        b = 0;
    }
}
