package com.korzhueva.android.inertialnavigation.filters;

import java.util.ArrayList;

public class MovingAverageFilter implements FilterInterface{
    private final ArrayList<Double> windowQueue = new ArrayList<Double>();
    private int period = 0;
    private double sum = 0;

    // Инициализация класса
    public MovingAverageFilter(int period) {
        this.period = period;
    }

    // Обновление состояния фильтра
    public double update(double num){
        sum += num;
        windowQueue.add(num);
        if (windowQueue.size() > period) {
            sum -= windowQueue.remove(0);
        }

        return sum / windowQueue.size();
    }

    // Сброс состояния фильтра
    public void reset(){
        sum = 0;
        period = 0;
        windowQueue.clear();
    }
}
