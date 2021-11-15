package com.example.pdfscanner.formdetector;

import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FormDetector {
    private static ImageSegmentation imageSegmentation = null;
    static int resizeThreshold = 500;
    static float resizeScale = 1.0f;
    static boolean isHisEqual = false;

    public FormDetector(Activity activity) throws IOException {
        try {
            imageSegmentation = new ImageSegmentation(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static Point[] detector(Bitmap srcBmp) {
        isHisEqual = false;
        resizeScale = 1.0f;
        if (srcBmp == null) {
            throw new IllegalArgumentException("srcBmp cannot be null");
        }
        Bitmap outBmp = null;
        if (imageSegmentation != null) {
            Bitmap bitmap = imageSegmentation.detectImage(srcBmp);
            if (bitmap != null) {
                outBmp = Bitmap.createScaledBitmap(bitmap, srcBmp.getWidth(), srcBmp.getHeight(), false);
            }
        }

        Mat inputMat = new Mat(outBmp.getWidth(), outBmp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(outBmp, inputMat);
        MatOfPoint2f points = findLargestRectangle(inputMat);
        return points.toArray();
    }

    private static MatOfPoint2f findLargestRectangle(Mat original_image) {
        Mat image = resizeImage(original_image);
        //Mat untouched = original_image.clone();
        MatOfPoint2f result = new MatOfPoint2f();
        int cannyValue[] = {100, 150, 300};
        int blurValue[] = {3, 7, 11, 15};

        for (int i=0; i<3;i++){
            for (int j=0;j<4;j++) {
                Mat scanImage = preprocessedImage(image, cannyValue[i], blurValue[j]);
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Imgproc.findContours(scanImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);



                if (contours.size() > 0) {
                    try {
                        contours.sort(new Comparator<MatOfPoint>() {
                            public int compare(MatOfPoint c1, MatOfPoint c2) {
                                return (int) (Imgproc.contourArea(c2)-Imgproc.contourArea(c1));
                            }
                        });
                    }
                    catch (Exception e){
                        continue;
                    }

                    MatOfPoint contour = contours.get(0); //the largest is at the index 0 for starting point

                    double arc = arcLength(new MatOfPoint2f(contour.toArray()), true);
                    MatOfPoint2f outDP = new MatOfPoint2f();
                    Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), outDP, 0.01 * arc, true);
                    MatOfPoint2f selectedPoints = selectPoints(outDP);
                    if (selectedPoints.toArray().length != 4) {
                        continue;
                    } else {
                        int widthMin = (int) selectedPoints.toArray()[0].x;
                        int widthMax = (int) selectedPoints.toArray()[0].x;
                        int heightMin = (int) selectedPoints.toArray()[0].y;
                        int heightMax = (int) selectedPoints.toArray()[0].y;
                        for (int k = 1; k < 4; k++) {
                            if (selectedPoints.toArray()[k].x < widthMin) {
                                widthMin = (int) selectedPoints.toArray()[k].x;
                            }
                            if (selectedPoints.toArray()[k].x > widthMax) {
                                widthMax = (int) selectedPoints.toArray()[k].x;
                            }
                            if (selectedPoints.toArray()[k].y < heightMin) {
                                heightMin = (int) selectedPoints.toArray()[k].y;
                            }
                            if (selectedPoints.toArray()[k].y > heightMax) {
                                heightMax = (int) selectedPoints.toArray()[k].y;
                            }
                        }
                        int selectArea = (widthMax - widthMin) * (heightMax - heightMin);
                        int imageArea = scanImage.cols() * scanImage.rows();
                        if (selectArea < (imageArea / 20)) {
                            result = new MatOfPoint2f();
                            continue;
                        } else {
                            result = selectedPoints;
                            return sortPointClockwise(upsize(result));
                        }
                    }
                }
            }
        }

        if (!isHisEqual){
            isHisEqual = true;
            return findLargestRectangle(original_image);
        }
        if (result.toArray().length != 4) {
            ArrayList<Point> temp_points = new ArrayList<Point>();
            result = new MatOfPoint2f();
            temp_points.add(new Point(0,0));
            temp_points.add(new Point(image.cols(),0));
            temp_points.add(new Point(image.cols(),image.rows()));
            temp_points.add(new Point(0,image.rows()));
            result.fromList(temp_points);
        }

        return sortPointClockwise(upsize(result));
    }

    private static MatOfPoint2f upsize(MatOfPoint2f result) {
        ArrayList<Point> temp_points = new ArrayList<Point>();
        MatOfPoint2f points = new MatOfPoint2f();
        for (Point point : result.toArray()) {
            temp_points.add(new Point(point.x*resizeScale,point.y*resizeScale));
        }
        points.fromList(temp_points);
        return points;
    }

    private static Mat resizeImage(Mat srcBitmap) {
        int width = srcBitmap.cols();
        int height = srcBitmap.rows();
        int maxSize = width > height? width : height;
        if (maxSize > resizeThreshold) {
            resizeScale = 1.0f * maxSize / resizeThreshold;
            width = (int) (width / resizeScale);
            height = (int) (height / resizeScale);
            Size size = new Size(width, height);
            Mat resizedBitmap = new Mat(size, CvType.CV_8UC3);
            resize(srcBitmap, resizedBitmap, size);
            return resizedBitmap;
        }
        return srcBitmap;
    }

    private static Mat preprocessedImage(Mat image, int cannyValue, int blurValue) {
        Mat grayMat = new Mat();
        cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY);
        if (isHisEqual){
            Imgproc.equalizeHist(grayMat, grayMat);
        }
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurMat, new Size(blurValue, blurValue), 0);
        Mat cannyMat = new Mat();
        Imgproc.Canny(blurMat, cannyMat, 50, cannyValue, 3);
        Mat thresholdMat = new Mat();
        Imgproc.threshold(cannyMat, thresholdMat, 0, 255, Imgproc.THRESH_OTSU);
        return thresholdMat;
    }

    private static MatOfPoint2f selectPoints(MatOfPoint2f points) {
        if (points.toArray().length > 4) {
            Point p = points.toArray()[0];
            int minX = (int) p.x;
            int maxX = (int) p.x;
            int minY = (int) p.y;
            int maxY = (int) p.y;

            for (int i = 1; i < points.toArray().length; i++) {
                if (points.toArray()[i].x < minX) {
                    minX = (int) points.toArray()[i].x;
                }
                if (points.toArray()[i].x > maxX) {
                    maxX = (int)points.toArray()[i].x;
                }
                if (points.toArray()[i].y < minY) {
                    minY = (int)points.toArray()[i].y;
                }
                if (points.toArray()[i].y > maxY) {
                    maxY = (int)points.toArray()[i].y;
                }
            }

            Point center = new Point((minX + maxX) / 2, (minY + maxY) / 2);
            Point p0 = choosePoint(center, points, 0);
            Point p1 = choosePoint(center, points, 1);
            Point p2 = choosePoint(center, points, 2);
            Point p3 = choosePoint(center, points, 3);

            ArrayList<Point> temp_points = new ArrayList<Point>();

            points = new MatOfPoint2f();
            if (!(p0.x == 0 && p0.y == 0)){
                temp_points.add(p0);
            }
            if (!(p1.x == 0 && p1.y == 0)){
                temp_points.add(p1);
            }
            if (!(p2.x == 0 && p2.y == 0)){
                temp_points.add(p2);
            }
            if (!(p3.x == 0 && p3.y == 0)){
                temp_points.add(p3);
            }
            points.fromList(temp_points);
        }
        return points;
    }

    private static Point choosePoint(Point center, MatOfPoint2f points, int type) {
        int index = -1;
        int minDis = 0;
        if (type == 0) {
            for (int i = 0; i < points.toArray().length; i++) {
                if (points.toArray()[i].x < center.x && points.toArray()[i].y < center.y) {
                    int dis = (int) (sqrt(pow((points.toArray()[i].x - center.x), 2) + pow((points.toArray()[i].y - center.y), 2)));
                    if (dis > minDis){
                        index = i;
                        minDis = dis;
                    }
                }
            }
        } else if (type == 1) {
            for (int i = 0; i < points.toArray().length; i++) {
                if (points.toArray()[i].x < center.x && points.toArray()[i].y > center.y) {
                    int dis = (int) (sqrt(pow((points.toArray()[i].x - center.x), 2) + pow((points.toArray()[i].y - center.y), 2)));
                    if (dis > minDis){
                        index = i;
                        minDis = dis;
                    }
                }
            }
        } else if (type == 2) {
            for (int i = 0; i < points.toArray().length; i++) {
                if (points.toArray()[i].x > center.x && points.toArray()[i].y < center.y) {
                    int dis = (int) (sqrt(pow((points.toArray()[i].x - center.x), 2) + pow((points.toArray()[i].y - center.y), 2)));
                    if (dis > minDis){
                        index = i;
                        minDis = dis;
                    }
                }
            }

        } else if (type == 3) {
            for (int i = 0; i < points.toArray().length; i++) {
                if (points.toArray()[i].x > center.x && points.toArray()[i].y > center.y) {
                    int dis = (int) (sqrt(pow((points.toArray()[i].x - center.x), 2) + pow((points.toArray()[i].y - center.y), 2)));
                    if (dis > minDis){
                        index = i;
                        minDis = dis;
                    }
                }
            }
        }

        if (index != -1){
            return new Point(points.toArray()[index].x, points.toArray()[index].y);
        }
        return new Point(0, 0);
    }

    public static MatOfPoint2f sortPointClockwise(MatOfPoint2f screenCnt2f) {
        if (screenCnt2f.toArray().length != 4) {
            return screenCnt2f;
        }
        List<Point> points = screenCnt2f.toList();
        // # initialize a list of coordinates that will be ordered
        // # such that the first entry in the list is the top-left,
        // # the second entry is the top-right, the third is the
        // # bottom-right, and the fourth is the bottom-left
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                double s1 = p1.x + p1.y;
                double s2 = p2.x + p2.y;
                return Double.compare(s1, s2);
            }
        });
        Point topLeft = points.get(0);
        Point bottomRight = points.get(3);

        List<Point> points_1 = new ArrayList<Point>();
        points_1.add(points.get(1));
        points_1.add(points.get(2));

        // # now, compute the difference between the points, the
        // # top-right point will have the smallest difference,
        // # whereas the bottom-left will have the largest difference
        Collections.sort(points_1, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                double s1 = p1.y - p1.x  ;
                double s2 = p2.y - p2.x;
                return Double.compare(s1, s2);
            }
        });
        Point topRight = points_1.get(0);
        Point bottomLeft = points_1.get(1);
        Point temp;
        if (topLeft.y>bottomLeft.y) {
            temp = topLeft;
            topLeft = bottomLeft;
            bottomLeft = temp;
        }
        if (topRight.y>bottomRight.y) {
            temp = topRight;
            topRight = bottomRight;
            bottomRight = temp;
        }
        if (topLeft.x>topRight.x) {
            temp = topLeft;
            topLeft = topRight;
            topRight = temp;
        }
        if (bottomLeft.x>bottomRight.x) {
            temp = bottomLeft;
            bottomLeft = bottomRight;
            bottomRight = temp;
        }
        Point[] pts = new Point[]{topLeft,topRight, bottomRight, bottomLeft};
        screenCnt2f = new MatOfPoint2f(pts);
        return screenCnt2f;
    }

}
