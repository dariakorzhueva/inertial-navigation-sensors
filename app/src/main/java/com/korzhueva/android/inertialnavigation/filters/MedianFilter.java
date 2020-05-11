package com.korzhueva.android.inertialnavigation.filters;

public class MedianFilter implements FilterInterface{
    private int window;
    private double[] values;
    private int count = 0;

    public MedianFilter(int window) {
        this.window = window;
        this.values = new double[window];
    }

    public double update(double current) {
        if (count >= window)
            count = 0;

        values[count] = current;
        count++;

        if (count == window)
            return getMiddle(values);
        else
            return current;
    }

    private double getMiddle(double[] values) {
        double middle;

        if ((values[0] <= values[1]) && (values[0] <= values[2])) {
            middle = Math.min(values[1], values[2]);
        } else {
            if ((values[1] <= values[0]) && (values[1] <= values[2])) {
                middle = Math.min(values[0], values[2]);
            } else {
                middle = Math.min(values[0], values[1]);
            }
        }
        return middle;
    }

    public void reset() {
        window = 0;
        values = new double[3];
        count = 0;
    }
}