package com.example.pdfscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pdfscanner.formdetector.FormDetector;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolygonView extends RelativeLayout {

    protected Context context;
    private Paint paint;
    private ImageView topLeft;
    private ImageView topRight;
    private ImageView bottomLeft;
    private ImageView bottomRight;
    private ImageView midLeft;
    private ImageView midTop;
    private ImageView midBottom;
    private ImageView midRight;
    private PolygonView polygonView = null;
    private int pointWidth;

    public PolygonView(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PolygonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PolygonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        polygonView = this;
        pointWidth = (int) getResources().getDimension(R.dimen.polygonViewCircleWidth);
        topLeft = getImageView(0, 0);
        topRight = getImageView(getWidth(), 0);
        bottomLeft = getImageView(0, getHeight());
        bottomRight = getImageView(getWidth(), getHeight());

        midLeft = getImageView(0, getHeight() / 2);
        midLeft.setOnTouchListener(new MidPointTouchListener(topLeft, bottomLeft));

        midTop = getImageView(getWidth() / 2,0);
        midTop.setOnTouchListener(new MidPointTouchListener(topLeft, topRight));

        midBottom = getImageView(getWidth()/2, getHeight());
        midBottom.setOnTouchListener(new MidPointTouchListener(bottomLeft, bottomRight));

        midRight = getImageView(getWidth(), getHeight() / 2);
        midRight.setOnTouchListener(new MidPointTouchListener(topRight, bottomRight));

        addView(topLeft);
        addView(topRight);
        addView(bottomLeft);
        addView(bottomRight);
        addView(midLeft);
        addView(midRight);
        addView(midTop);
        addView(midBottom);
        initPaint();
    }

    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    private boolean checkPoints(Point[] points) {
        return points != null && points.length == 4 && points[0] != null && points[1] != null && points[2] != null && points[3] != null;
    }

    public void setCropPoints(Point[] cropPoints) {
        if (polygonView == null) {
            return;
        }
        if (!checkPoints(cropPoints)) {
            setFullImgCrop();
            invalidate();
        } else {
            setAutoImgCrop(cropPoints);
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawLine(topLeft.getX() + (topLeft.getWidth() / 2), topLeft.getY() + (topLeft.getHeight() / 2), bottomLeft.getX() + (bottomLeft.getWidth() / 2), bottomLeft.getY() + (bottomLeft.getHeight() / 2), paint);
        canvas.drawLine(topLeft.getX() + (topLeft.getWidth() / 2), topLeft.getY() + (topLeft.getHeight() / 2), topRight.getX() + (topRight.getWidth() / 2), topRight.getY() + (topRight.getHeight() / 2), paint);
        canvas.drawLine(topRight.getX() + (topRight.getWidth() / 2), topRight.getY() + (topRight.getHeight() / 2), bottomRight.getX() + (bottomRight.getWidth() / 2), bottomRight.getY() + (bottomRight.getHeight() / 2), paint);
        canvas.drawLine(bottomLeft.getX() + (bottomLeft.getWidth() / 2), bottomLeft.getY() + (bottomLeft.getHeight() / 2), bottomRight.getX() + (bottomRight.getWidth() / 2), bottomRight.getY() + (bottomRight.getHeight() / 2), paint);
        midLeft.setX(bottomLeft.getX() - ((bottomLeft.getX() - topLeft.getX()) / 2));
        midLeft.setY(bottomLeft.getY() - ((bottomLeft.getY() - topLeft.getY()) / 2));
        midRight.setX(bottomRight.getX() - ((bottomRight.getX() - topRight.getX()) / 2));
        midRight.setY(bottomRight.getY() - ((bottomRight.getY() - topRight.getY()) / 2));
        midBottom.setX(bottomRight.getX() - ((bottomRight.getX() - bottomLeft.getX()) / 2));
        midBottom.setY(bottomRight.getY() - ((bottomRight.getY() - bottomLeft.getY()) / 2));
        midTop.setX(topRight.getX() - ((topRight.getX() - topLeft.getX()) / 2));
        midTop.setY(topRight.getY() - ((topRight.getY() - topLeft.getY()) / 2));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setAutoImgCrop(Point[] cropPoints) {
        topLeft.setX((float) cropPoints[0].x - pointWidth/2);
        topLeft.setY((float) cropPoints[0].y - pointWidth/2);
        topRight.setX((float) cropPoints[1].x - pointWidth/2);
        topRight.setY((float) cropPoints[1].y - pointWidth/2);
        bottomLeft.setX((float) cropPoints[3].x - pointWidth/2);
        bottomLeft.setY((float) cropPoints[3].y -pointWidth/2);
        bottomRight.setX((float) cropPoints[2].x - pointWidth/2);
        bottomRight.setY((float) cropPoints[2].y - pointWidth/2);
        initPaint();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setFullImgCrop() {
        topLeft.setX(0-pointWidth/2);
        topLeft.setY(0-pointWidth/2);
        topRight.setX(getWidth()-pointWidth/2);
        topRight.setY(0-pointWidth/2);
        bottomLeft.setX(0-pointWidth/2);
        bottomLeft.setY(getHeight()-pointWidth/2);
        bottomRight.setX(getWidth()-pointWidth/2);
        bottomRight.setY(getHeight()-pointWidth/2);
        initPaint();
        invalidate();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.point_true));
        paint.setStrokeWidth(getResources().getDimension(R.dimen.polygonViewStrokeWidth));
        paint.setAntiAlias(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private ImageView getImageView(int x, int y){
        ImageView imageView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(pointWidth , pointWidth);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.ic_point_foreground);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setOnTouchListener(new TouchPointListener());
        return imageView;
    }

    public boolean isValidShape(Point[] pointFMap) {
        return pointFMap!= null && pointFMap.length == 4;
    }

    public Point[] getPoints() {

        List<PointF> points = new ArrayList<PointF>();
        points.add(new PointF(topLeft.getX(), topLeft.getY()));
        points.add(new PointF(topRight.getX(), topRight.getY()));
        points.add(new PointF(bottomLeft.getX(), bottomLeft.getY()));
        points.add(new PointF(bottomRight.getX(), bottomRight.getY()));
        try {
            return getOrderedPoints(points);
        }
        catch (Exception e) {
            return null;
        }
    }

    public Point[] getOrderedPoints(List<PointF> points) {

        PointF centerPoint = new PointF();
        int size = points.size();
        for (PointF pointF : points) {
            centerPoint.x += pointF.x / size;
            centerPoint.y += pointF.y / size;
        }
        Map<Integer, PointF> orderedPoints = new HashMap<>();
        for (PointF pointF : points) {
            int index = -1;
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0;
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1;
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2;
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3;
            }
            orderedPoints.put(index, pointF);
        }

        if (orderedPoints.size()==4) {
            Point tL = new Point(orderedPoints.get(0).x + pointWidth/2, orderedPoints.get(0).y + pointWidth/2);
            Point tR = new Point(orderedPoints.get(1).x+pointWidth/2, orderedPoints.get(1).y + pointWidth/2);
            Point bL = new Point(orderedPoints.get(2).x+pointWidth/2, orderedPoints.get(2).y+pointWidth/2);
            Point bR = new Point(orderedPoints.get(3).x+pointWidth/2, orderedPoints.get(3).y+pointWidth/2);
            Point[] cv_points = new Point[]{tL, tR, bR, bL};
            return cv_points;
        }
        return null;
    }

    public void setOrderedPoints() {
        MatOfPoint2f mop = new MatOfPoint2f();
        Point tL = new Point(topLeft.getX(), topLeft.getY());
        Point tR = new Point(topRight.getX(), topRight.getY());
        Point bL = new Point(bottomLeft.getX(), bottomLeft.getY());
        Point bR = new Point(bottomRight.getX(), bottomRight.getY());
        Point[] cropPoints = new Point[]{tL, tR, bR, bL};
        mop.fromArray(cropPoints);
        Point[] x = this.sortPointClockwise(mop).toArray();
        topLeft.setX((float) x[0].x);
        topLeft.setY((float) x[0].y);
        topRight.setX((float) x[1].x);
        topRight.setY((float) x[1].y);
        bottomLeft.setX((float) x[3].x);
        bottomLeft.setY((float) x[3].y);
        bottomRight.setX((float) x[2].x );
        bottomRight.setY((float) x[2].y );
        invalidate();

    }

    private class TouchPointListener implements OnTouchListener{

        PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX()-DownPT.x,event.getY()-DownPT.y);
                    if (((StartPT.x + mv.x + v.getWidth()/2)) < polygonView.getWidth() && (StartPT.y+mv.y+v.getHeight()/2<polygonView.getHeight()) &&(StartPT.y+mv.y+v.getHeight()/2 >0) &&(StartPT.x+ mv.x + v.getWidth()/2 >0)){
                        v.setX((int)(StartPT.x+mv.x));
                        v.setY((int)(StartPT.y+mv.y));
                        StartPT = new PointF(v.getX(),v.getY());
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(),v.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    setOrderedPoints();
                    int color = 0;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.point_true);
                    } else {
                        color = getResources().getColor(R.color.point_false);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }

    private class MidPointTouchListener implements OnTouchListener {

        PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position

        private ImageView mainPointer1;
        private ImageView mainPointer2;

        public MidPointTouchListener(ImageView mainPointer1, ImageView mainPointer2) {
            this.mainPointer1 = mainPointer1;
            this.mainPointer2 = mainPointer2;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);
                    if (Math.abs(mainPointer1.getX() - mainPointer2.getX()) > Math.abs(mainPointer1.getY() - mainPointer2.getY())) {
                        if (((mainPointer2.getY() + mv.y + v.getHeight()/2 < polygonView.getHeight()) && (mainPointer2.getY() + mv.y + v.getHeight()/2 > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setY((int) (mainPointer2.getY() + mv.y));
                        }
                        if (((mainPointer1.getY() + mv.y + v.getHeight()/2 < polygonView.getHeight()) && (mainPointer1.getY() + mv.y + v.getHeight()/2 > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setY((int) (mainPointer1.getY() + mv.y));
                        }
                    } else {
                        if ((mainPointer2.getX() + mv.x + v.getWidth()/2 < polygonView.getWidth()) && (mainPointer2.getX() + mv.x + v.getWidth()/2 > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setX((int) (mainPointer2.getX() + mv.x));
                        }
                        if ((mainPointer1.getX() + mv.x + v.getWidth()/2 < polygonView.getWidth()) && (mainPointer1.getX() + mv.x + v.getWidth()/2  > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setX((int) (mainPointer1.getX() + mv.x));
                        }
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    setOrderedPoints();
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.point_true);
                    } else {
                        color = getResources().getColor(R.color.point_false);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }

    private MatOfPoint2f sortPointClockwise(MatOfPoint2f screenCnt2f) {
        if (screenCnt2f.toArray().length != 4) {
            return screenCnt2f;
        }
        List<Point> points = screenCnt2f.toList();

        Point centerPoint = new Point();

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
        Point p_topLeft = points.get(0);
        Point p_bottomRight = points.get(3);

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
        Point p_topRight = points_1.get(0);
        Point p_bottomLeft = points_1.get(1);
        Point temp;
        if (p_topLeft.y>p_bottomLeft.y) {
            temp = p_topLeft;
            p_topLeft = p_bottomLeft;
            p_bottomLeft = temp;
        }
        if (p_topRight.y>p_bottomRight.y) {
            temp = p_topRight;
            p_topRight = p_bottomRight;
            p_bottomRight = temp;
        }
        if (p_topLeft.x>p_topRight.x) {
            temp = p_topLeft;
            p_topLeft = p_topRight;
            p_topRight = temp;
        }
        if (p_bottomLeft.x>p_bottomRight.x) {
            temp = p_bottomLeft;
            p_bottomLeft = p_bottomRight;
            p_bottomRight = temp;
        }
        Point[] pts = new Point[]{p_topLeft,p_topRight, p_bottomRight, p_bottomLeft};
        screenCnt2f = new MatOfPoint2f(pts);
        return screenCnt2f;
    }

}