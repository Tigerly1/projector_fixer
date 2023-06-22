package com.example.sensoryczne;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.LinearOptimizer;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;


class Optimizer {
    private final double r;

    public Optimizer(double ratio) {
        this.r = ratio;
    }

    public MatOfPoint2f solveFittingProblem(Point[] points) {

        double[] leftBottom = new double[]{points[0].x, points[0].y};
        double[] rightBottom = new double[]{points[1].x, points[1].y};
        double[] rightTop = new double[]{points[2].x, points[2].y};
        double[] leftTop = new double[]{points[3].x, points[3].y};

        double[] ab1 = calculateSlopeIntercept(leftTop, rightTop, true);
        double[] gk2 = calculateSlopeIntercept(rightTop, rightBottom, false);
        double[] ab3 = calculateSlopeIntercept(leftBottom, rightBottom, true);
        double[] gk4 = calculateSlopeIntercept(leftTop, leftBottom, false);

        double[][] A = {
                {-ab1[0], 1, 0},
                {-ab1[0], 1, -ab1[0]},
                {ab3[0], -1, r},
                {ab3[0], -1, ab3[0] + r},
                {1, -gk2[0], 1},
                {1, -gk2[0], 1 + r * gk2[0]},
                {-1, gk4[0], 0},
                {-1, gk4[0], -r * gk4[0]}
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

        double[] result = solveLP(A, b, c);

        double l = result[0];
        double t = result[1];
        double w = result[2];

        Log.d("test", String.valueOf(l));
        Log.d("test", String.valueOf(t));

        Log.d("test", String.valueOf(w));

        return new MatOfPoint2f(
                new Point(l, t),
                new Point(l + w, t),
                new Point(l + w, t - r * w),
                new Point(l, t - r * w)
        );
    }

    private double[] calculateSlopeIntercept(double[] point1, double[] point2, boolean xToY) {
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

    private double[] solveLP(double[][] A, double[] b, double[] c) {
        ArrayList<LinearConstraint> constraints = new ArrayList<>();
        //Log.d()
        for (int i = 0; i < A.length; i++) {
            constraints.add(new LinearConstraint(A[i], Relationship.LEQ, b[i]));
        }

        LinearObjectiveFunction f = new LinearObjectiveFunction(c, 0);
        LinearOptimizer optimizer = new SimplexSolver();
        PointValuePair solution = optimizer.optimize(new MaxIter(10000), f, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        return solution.getPoint();
    }

}