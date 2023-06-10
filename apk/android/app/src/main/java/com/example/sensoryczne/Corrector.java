package com.example.sensoryczne;

import org.opencv.core.Mat;
import org.opencv.android.Utils;
import org.opencv.core.Core;

import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import java.util.Comparator;

import org.opencv.core.MatOfPoint;

import org.opencv.core.MatOfPoint2f;

import org.opencv.core.MatOfPoints;

import org.opencv.core.Point;

import org.opencv.core.Size;

import org.opencv.core.Scalar;

import org.opencv.core.CvType;



import android.graphics.Bitmap;
import android.graphics.PointF;


public class Corrector {

    public Corrector() {
    }

    public Result correct(Bitmap bitmap) {
        
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        Mat redChannel = new Mat();
        Mat greenChannel = new Mat();
        Mat blueChannel = new Mat();

        List<Mat> channels = new ArrayList<Mat>();
        Core.split(src, channels);

        redChannel = channels.get(0);
        greenChannel = channels.get(1);
        blueChannel = channels.get(2);

        Mat binarizedRed = new Mat();
        Mat binarizedGreen = new Mat();
        Mat binarizedBlue = new Mat(); 
        
        //binarize with Otsy method
        Imgproc.threshold(redChannel, binarizedRed, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(greenChannel, binarizedGreen, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(blueChannel, binarizedBlue, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        //bitwise or of blue and green
        Mat binarizedBlueGreen = new Mat();
        Core.bitwise_or(binarizedBlue, binarizedGreen, binarizedBlueGreen);

        //bitwise xor of red and bluegreen

        Mat binarizedRedBlueGreen = new Mat();
        Core.bitwise_xor(binarizedRed, binarizedBlueGreen, binarizedRedBlueGreen);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binarizedRedBlueGreen, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //find the biggest contour
        MatOfPoint biggestContour = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            biggestContour = contours.stream().max(Comparator.comparing(Imgproc::contourArea)).get();
        }

        //find boundary points
      /*  PointF topLeft = biggestContour.stream().min(p -> p.x + p.y).get();
        PointF topRight = biggestContour.stream().max(p -> p.x - p.y).get();
        PointF bottomLeft = biggestContour.stream().min(p -> p.x - p.y).get();
        PointF bottomRight = biggestContour.stream().max(p -> p.x + p.y).get();*/
        


    }
    
}
