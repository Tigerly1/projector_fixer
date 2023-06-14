package com.example.sensoryczne;

import android.graphics.PointF;



public class Result {
    private final double[] distortionMatrix;
//    private final PointF[] points;

    public Result(double[] distortionMatrix){//}, PointF[] points) {
        this.distortionMatrix = distortionMatrix;
//        this.points = points;
    }
    public double[] getDistortionMatrix() {
        return distortionMatrix;
    }
}
