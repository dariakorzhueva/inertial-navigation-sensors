package com.korzhueva.android.inertialnavigation.filters;

public class AlphaBetaFilter {
    private double dt = 0.5;
    private double xk_1 = 0, vk_1 = 0, a = 0.85, b = 0.005;
    private double xk, vk, rk;

    public AlphaBetaFilter() {

    }

    public AlphaBetaFilter(double dt, double xk_1, double vk_1, double a, double b) {
        this.dt = dt;
        this.xk_1 = xk_1;
        this.vk_1 = vk_1;
        this.a = a;
        this.b = b;
    }

    public double update(double current) {
        xk = xk_1 + (vk_1 * dt);
        vk = vk_1;

        rk = current - xk;

        xk += a * rk;
        vk += (b * rk) / dt;

        xk_1 = xk;
        vk_1 = vk;

        return xk_1;
    }

    public void reset() {
        dt = 0;
        xk_1 = 0;
        vk_1 = 0;
        a = 0;
        b = 0;
    }
}
