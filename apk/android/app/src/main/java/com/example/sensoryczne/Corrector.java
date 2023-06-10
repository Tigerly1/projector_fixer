package com.example.sensoryczne;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Arrays;
import org.opencv.core.Mat;
import org.opencv.core.Core;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import org.opencv.core.MatOfPoint;

import org.opencv.core.MatOfPoint2f;

import org.opencv.core.Point;

import org.opencv.core.Size;

import org.opencv.core.Scalar;

import org.opencv.core.CvType;


import android.util.Log;


public class Corrector {

    private final double resize;
    public Corrector() {
        this(1.0);
    }

    public Corrector(double resize){
        this.resize = resize;
    }
    public Result correct(String path) {
        
        /*Mat src = new Mat();
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
      *//*  PointF topLeft = biggestContour.stream().min(p -> p.x + p.y).get();
        PointF topRight = biggestContour.stream().max(p -> p.x - p.y).get();
        PointF bottomLeft = biggestContour.stream().min(p -> p.x - p.y).get();
        PointF bottomRight = biggestContour.stream().max(p -> p.x + p.y).get();*//*
*/

        Mat image = Imgcodecs.imread(path); // replace with actual image path
        Mat binarized = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // Convert image color
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        // Binarize each channel
        for (int channel = 0; channel < 3; channel++) {
            Mat binChannel = new Mat();
            Core.extractChannel(image, binChannel, channel);
            Imgproc.threshold(binChannel, binChannel, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            if (binarized.empty()) {
                binarized = binChannel;
            } else {
                if (channel == 0) {
                    Core.bitwise_xor(binarized, binChannel, binarized);
                } else {
                    Core.bitwise_or(binarized, binChannel, binarized);
                }
            }
        }

        // Resize and erode
        Imgproc.resize(binarized, binarized, new Size(), 1/resize, 1/resize, Imgproc.INTER_LINEAR);
        Imgproc.erode(binarized, binarized, Mat.ones(3, 3, CvType.CV_8U), new Point(-1, -1), 3, 1, new Scalar(1));

        // Find contours
        Imgproc.findContours(binarized, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find biggest contour
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }

        // Compute points for perspective transformation (TODO: Adapt this part based on your exact use case)
        MatOfPoint biggestContour = contours.get(maxValIdx);
        MatOfPoint2f perspective = new MatOfPoint2f(biggestContour.toArray());
        MatOfPoint2f innerTarget = new MatOfPoint2f(biggestContour.toArray()); // replace with your desired points


        // Get perspective transform
        Mat M = Imgproc.getPerspectiveTransform(innerTarget, perspective);

        double[][] doubleArray = matToDoubleArray(M);

        // Print the double array
        for (int i = 0; i < doubleArray.length; i++) {
            for (int j = 0; j < doubleArray[0].length; j++) {
                Log.d("test", doubleArray[i][j] + " ");
            }
        }
        return null;
    }
    public static double[][] matToDoubleArray(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = mat.get(i, j)[0];
            }
        }

        return result;
    }

    public double[] optimizer(String[] args){

        double[] leftTop = new double[]{1, 2};
        double[] rightTop = new double[]{3, 4};
        double[] rightBottom = new double[]{5, 6};
        double[] leftBottom = new double[]{7, 8};
        double r = 9;

        double[] ab1 = calculateSlopeIntercept(leftTop, rightTop, true);
        double[] gk2 = calculateSlopeIntercept(rightTop, rightBottom, false);
        double[] ab3 = calculateSlopeIntercept(leftBottom, rightBottom, true);
        double[] gk4 = calculateSlopeIntercept(leftTop, leftBottom, false);

        double[][] matrixData = {
                {-ab1[0], 1, 0},
                {-ab1[0], 1, -ab1[0]},
                {ab3[0], -1, r},
                {ab3[0], -1, ab3[0]+r},
                {1, -gk2[0], 1},
                {1, -gk2[0], 1+r*gk2[0]},
                {-1, gk4[0], 0},
                {-1, gk4[0], -r*gk4[0]}
        };

        double[] b = {
                ab1[1],
                ab1[1],
                -ab3[1],
                -ab3[1],
                gk2[1],
                gk2[1],
                -gk4[1],
                -gk4[1]
        };

        double[] c = {0, 0, -1};

        return solveLP(matrixData, b, c);


    }

    private static double[] calculateSlopeIntercept(double[] point1, double[] point2, boolean xToY) {
        double slope;
        double intercept;

        if (xToY) {
            slope = (point2[1] - point1[1]) / (point2[0] - point1[0]);
            intercept = point1[1] - slope * point1[0];
        } else {
            slope = (point2[0] - point1[0]) / (point2[1] - point1[1]);
            intercept = point1[0] - slope * point1[1];
        }

        return new double[]{slope, intercept};
    }

    private static double[] solveLP(double[][] matrixData, double[] b, double[] c) {
        ArrayList<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < matrixData.length; i++) {
            constraints.add(new LinearConstraint(matrixData[i], Relationship.LEQ, b[i]));
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(c, 0);
        LinearOptimizer optimizer = new SimplexSolver();
        PointValuePair solution = optimizer.optimize(new MaxIter(100), f, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        System.out.println(Arrays.toString(solution.getPoint()));
        return solution.getPoint();
    }
}
