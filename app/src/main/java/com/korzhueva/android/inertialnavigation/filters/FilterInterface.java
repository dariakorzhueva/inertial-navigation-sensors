package com.korzhueva.android.inertialnavigation.filters;

public interface FilterInterface {
    // Обновление состояния фильтра
    double update(double current);

    // Сброс состояния фильтра
    void reset();
}
