package com.korzhueva.android.inertialnavigation.filters;

public class MovingAverage {
    private double sum = 0;
    private int counter = 0;

    public MovingAverage(){

    }

    public double update(double current){
        sum += current;
        counter++;

        double average = sum/counter;

        return average;
    }

    public void reset(){
        sum = 0;
        counter = 0;
    }

}
