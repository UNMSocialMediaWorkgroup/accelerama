package com.lunagameserve.compression;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.lunagameserve.acceleration.AccelerationCollection;
import com.lunagameserve.acceleration.AccelerationPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Ross on 2/27/2015.
 */
public enum Compressor {
    UncompressedBuffer {

        private ArrayList<AccelerationPoint> list =
                new ArrayList<AccelerationPoint>();

        @Override
        public void write(OutputStream out, AccelerationCollection collection)
                throws IOException {
            ByteWriter writer = new ByteWriter(out);
            for (int i = 0; i < collection.size(); i++) {
                writer.writeFloat(collection.get(i).getX());
                writer.writeFloat(collection.get(i).getY());
                writer.writeFloat(collection.get(i).getZ());
                writer.writeLong(collection.get(i).getTimestamp());
            }
        }

        @Override
        public void read(InputStream in) throws IOException {
            super.read(in);
            list.clear();
            float[] floats = new float[3];
            long timestamp;
            ByteReader reader = new ByteReader(in);
            while(in.available() > 0) {
                for (int i = 0; i < 3; i++) {
                    floats[i] = reader.readFloat();
                }
                timestamp = reader.readLong();
                list.add(new AccelerationPoint(floats, timestamp));
            }

            for (AccelerationPoint p : list) {
                xPoints.add(p.getX());
                yPoints.add(p.getY());
                zPoints.add(p.getZ());
            }
        }
    },
    ByteDownsample {
        private double amplitude;

        @Override
        public void read(InputStream in) {

        }

        @Override
        public void write(OutputStream out, AccelerationCollection collection)
                throws IOException{
            for (int i = 0; i < collection.size(); i++) {

            }
        }
    };

    protected ArrayList<Float> xPoints = new ArrayList<Float>();
    protected ArrayList<Float> yPoints = new ArrayList<Float>();
    protected ArrayList<Float> zPoints = new ArrayList<Float>();

    public static final int X_POINTS = 1;
    public static final int Y_POINTS = 2;
    public static final int Z_POINTS = 3;

    public abstract void write(OutputStream out,
                               AccelerationCollection collection)
                               throws IOException ;

    public void read(InputStream in) throws IOException {
        xPoints.clear();
        yPoints.clear();
        zPoints.clear();
    }

    private float heightDelta(ArrayList<Float> pts) {
        float max = pts.get(0);
        float min = pts.get(0);
        for (float d : pts) {
            if (max < d) {
                max = d;
            }
            if (min > d) {
                min = d;
            }
        }
        return max - min;
    }

    private float minPoint(ArrayList<Float> pts) {
        float min = pts.get(0);
        for (float d : pts) {
            if (min > d) {
                min = d;
            }
        }
        return min;
    }

    public void render(Canvas c, int pointType, int color) {
        ArrayList<Float> pts;
        switch(pointType) {
            case X_POINTS: pts = xPoints; break;
            case Y_POINTS: pts = yPoints; break;
            case Z_POINTS: pts = zPoints; break;
            default: throw new IllegalArgumentException();
        }

        float dx = (float)c.getWidth() / pts.size();
        float dy = (float)c.getHeight() / heightDelta(pts);
        float sy = minPoint(pts);

        Paint paint = new Paint();
        paint.setColor(color);

        for (int i = 1; i < pts.size(); i++) {
            float x1 = (i - 1) * dx;
            float x2 = i * dx;
            float y1 = (pts.get(i - 1) - sy) * dy;
            float y2 = (pts.get(i) - sy) * dy;

            c.drawLine(x1, y1, x2, y2, paint);
        }
    }
}
