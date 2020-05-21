package com.korzhueva.android.inertialnavigation.filters;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverageFilter implements FilterInterface{
    private final Queue<Double> windowQueue = new LinkedList<Double>();
    private int period;
    private double sum;

    // Инициализация класса
    public MovingAverageFilter(int period) {
        this.period = period;
    }

    // Обновление состояния фильтра
    public double update(double num){
        sum += num;
        windowQueue.add(num);
        if (windowQueue.size() > period) {
            sum -= windowQueue.remove();
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
