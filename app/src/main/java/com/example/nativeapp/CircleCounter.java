package com.example.nativeapp;

import org.opencv.core.Mat;

class CircleCounter {
    public int cnt;
    public double x;
    public double y;
    public double r;

    public CircleCounter(){
        this.cnt = 0;
        this.x = 0d;
        this.y = 0d;
        this.r = 0d;
    }

    public int intersects(Mat circles){
        for (int i = 0; i < circles.cols(); i++) {
            double newX = circles.get(0, i)[0];
            double newY = circles.get(0, i)[1];

            if ((Math.abs(this.x - newX) < 13) && (Math.abs(this.y - newY) < 13)){
                this.cnt++;
                if (this.cnt == 5) return 10;           // sufficient stability to send blob to identification
                else return i;
            }
        }
        return -1;
    }

    public void updateCircle(double x, double y, double r){
        this.x = x;
        this.y = y;
        this.r = r;
        this.cnt = 0;
    }
}