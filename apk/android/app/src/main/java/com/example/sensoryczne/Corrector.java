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

import com.example.sensoryczne.Result;
import com.example.sensoryczne.Optimizer;

import android.util.Log;


public class Corrector {

    private final double resize;
    private final Optimizer optimizer;
    private final double ratio;
    public Corrector() {
        this(1.0);
    }

    public Corrector(double resize){
        this.resize = resize;
        this.ratio = 3./4.;
        this.optimizer = new Optimizer(ratio);
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
        path = "/sdcard/Download/image.png";
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
        double imageArea = image.rows() * image.cols();
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if ( maxVal < contourArea && contourArea < imageArea * 0.99) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }




        MatOfPoint biggestContour = contours.get(maxValIdx);
        // for points in biggest contour
        // find top left, top right, bottom left, bottom right
        Point topLeft = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        Point topRight = new Point(Double.MIN_VALUE, Double.MAX_VALUE);
        Point bottomLeft = new Point(Double.MAX_VALUE, Double.MIN_VALUE);
        Point bottomRight = new Point(Double.MIN_VALUE, Double.MIN_VALUE);

//        Point topLeft = new Point(214,582);
//        Point topRight = new Point(457,562);
//        Point bottomLeft = new Point(188,283);
//        Point bottomRight = new Point(432,341);

        Point[] test = biggestContour.toArray();

        for (Point point : biggestContour.toArray()) {

            if (point.x + point.y < topLeft.x + topLeft.y) {
                topLeft = point;
            }
            if (point.x - point.y > topRight.x - topRight.y) {
                topRight = point;
            }
            if (point.x - point.y < bottomLeft.x - bottomLeft.y) {
                bottomLeft = point;
            }
            if (point.x + point.y > bottomRight.x + bottomRight.y) {
                bottomRight = point;
            }
        }


        MatOfPoint2f perspective = new MatOfPoint2f(topLeft, topRight, bottomRight, bottomLeft);
        MatOfPoint2f innerTarget = optimizer.solveFittingProblem(perspective.toArray());
        return new Result(serializePerspective(Imgproc.getPerspectiveTransform(innerTarget, perspective)));
    }

    public double[] serializePerspective(Mat mat) {
        double[] result = new double[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i*3+j] = mat.get(i, j)[0];
            }
        }
        return result;
    }
}
