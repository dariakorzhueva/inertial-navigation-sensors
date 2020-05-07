package com.korzhueva.android.inertialnavigation.filters;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverageFilter {
    private final Queue<Double> windowQueue = new LinkedList<Double>();
    private int period;
    private double sum;

    public MovingAverageFilter(int period) {
        this.period = period;
    }

    public double update(double num){
        sum += num;
        windowQueue.add(num);
        if (windowQueue.size() > period) {
            sum -= windowQueue.remove();
        }

        return sum / windowQueue.size();
    }

    public void reset(){
        sum = 0;
        period = 0;
        windowQueue.clear();
    }

}
