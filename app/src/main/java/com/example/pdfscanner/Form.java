package com.example.pdfscanner;

import android.graphics.Bitmap;

import org.opencv.core.Point;

public class Form {
    private Bitmap originalBitmap;
    private Point[] points;
    private Bitmap cropBitmap;

    public Form() {
        this.points = null;
        this.originalBitmap = null;
        this.cropBitmap = null;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public void setOriginalBitmap(Bitmap original) {
        this.originalBitmap = original.copy(original.getConfig(), true);
    }

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public Bitmap getCropBitmap() {
        return cropBitmap;
    }

    public void setCropBitmap(Bitmap cropBitmap) {
        this.cropBitmap = cropBitmap.copy(cropBitmap.getConfig(), true);;
    }

    public void Recycle() {
        if (this.originalBitmap!=null) {
            this.originalBitmap.recycle();
            this.originalBitmap = null;
        }
        if (this.cropBitmap!=null) {
            this.cropBitmap.recycle();
            this.cropBitmap = null;
        }
        points = null;
    }
}
