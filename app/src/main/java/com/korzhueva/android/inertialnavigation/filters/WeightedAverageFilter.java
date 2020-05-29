package com.korzhueva.android.inertialnavigation.filters;

import java.util.ArrayList;

public class WeightedAverageFilter implements FilterInterface {
    private final ArrayList<Double> windowQueue = new ArrayList<Double>();
    private int period = 0;
    private double sum = 0;

    // Инициализация класса
    public WeightedAverageFilter(int period) {
        this.period = period;
    }

    // Обновление состояния фильтра
    public double update(double num) {
        windowQueue.add(num);

        if (windowQueue.size() < period)
            return num;
        else {
            sum = 0;

            for (int i = 0; i < period; i++)
                sum += (period - i) * windowQueue.get(i);

            windowQueue.remove(0);

            return  (2.0 / (period * (period + 1))) * sum;
        }
    }

    // Сброс состояния фильтра
    public void reset() {
        sum = 0;
        period = 0;
        windowQueue.clear();
    }
}
