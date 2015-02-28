package com.lunagameserve.compression;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import com.lunagameserve.acceleration.AccelerationCollection;
import com.lunagameserve.acceleration.AccelerationPoint;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
            out.close();
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
            in.close();
        }
    },
    StandardGZip {
        private ArrayList<AccelerationPoint> list =
                new ArrayList<AccelerationPoint>();

        @Override
        public void write(OutputStream rOut, AccelerationCollection collection)
                throws IOException {
            GZIPOutputStream out = new GZIPOutputStream(rOut);

            ByteWriter writer = new ByteWriter(out);
            for (int i = 0; i < collection.size(); i++) {
                writer.writeFloat(collection.get(i).getX());
                writer.writeFloat(collection.get(i).getY());
                writer.writeFloat(collection.get(i).getZ());
                writer.writeLong(collection.get(i).getTimestamp());
            }
            out.close();
        }

        @Override
        public void read(InputStream rIn) throws IOException {
            super.read(rIn);
            GZIPInputStream in = new GZIPInputStream(rIn);
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
            in.close();
        }
    },
    DoubleGZip {
        private ArrayList<AccelerationPoint> list =
                new ArrayList<AccelerationPoint>();

        @Override
        public void write(OutputStream rOut, AccelerationCollection collection)
                throws IOException {
            GZIPOutputStream g2 = new GZIPOutputStream(rOut);
            GZIPOutputStream out = new GZIPOutputStream(g2);

            ByteWriter writer = new ByteWriter(out);
            for (int i = 0; i < collection.size(); i++) {
                writer.writeFloat(collection.get(i).getX());
                writer.writeFloat(collection.get(i).getY());
                writer.writeFloat(collection.get(i).getZ());
                writer.writeLong(collection.get(i).getTimestamp());
            }
            out.close();
        }

        @Override
        public void read(InputStream rIn) throws IOException {
            super.read(rIn);
            GZIPInputStream g2 = new GZIPInputStream(rIn);
            GZIPInputStream in = new GZIPInputStream(g2);
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
            in.close();
        }
    },
    ByteDownscaling {
        float maxX;
        float minX;
        float maxY;
        float minY;
        float maxZ;
        float minZ;

        @Override
        public void write(OutputStream out, AccelerationCollection collection)
                throws IOException {
            maxX = collection.maxX();
            minX = collection.minX();
            maxY = collection.maxY();
            minY = collection.minY();
            maxZ = collection.maxZ();
            minZ = collection.minZ();

            ByteWriter writer = new ByteWriter(out);
            writer.writeFloat(maxX);
            writer.writeFloat(minX);
            writer.writeFloat(maxY);
            writer.writeFloat(minY);
            writer.writeFloat(maxZ);
            writer.writeFloat(minZ);

            for (int i = 0; i < collection.size(); i++) {
                float x = collection.get(i).getX();
                byte bx;
                if (x > 0) {
                    bx = (byte)((x / maxX) * 127);
                } else {
                    bx = (byte)((x / minX) * 127);
                }
                out.write(bx);

                float y = collection.get(i).getY();
                byte by;
                if (y > 0) {
                    by = (byte)((y / maxY) * 127);
                } else {
                    by = (byte)((y / minY) * 127);
                }
                out.write(by);

                float z = collection.get(i).getZ();
                byte bz;
                if (z > 0) {
                    bz = (byte)((z / maxZ) * 127);
                } else {
                    bz = (byte)((z / minZ) * 127);
                }
                out.write(bz);
            }
            out.close();
        }

        @Override
        public void read(InputStream in) throws IOException {
            super.read(in);
            ByteReader reader = new ByteReader(in);
            maxX = reader.readFloat();
            minX = reader.readFloat();
            maxY = reader.readFloat();
            minY = reader.readFloat();
            maxZ = reader.readFloat();
            minZ = reader.readFloat();

            while (in.available() > 0) {
                byte x = (byte)in.read();
                float fx;
                if (x < 0) {
                    fx = ((float)x) * minX;
                } else {
                    fx = ((float)x) * maxX;
                }
                xPoints.add(fx);

                byte y = (byte)in.read();
                float fy;
                if (y < 0) {
                    fy = ((float)y) * minY;
                } else {
                    fy = ((float)y) * maxY;
                }
                yPoints.add(fy);

                byte z = (byte)in.read();
                float fz;
                if (z < 0) {
                    fz = ((float)z) * minZ;
                } else {
                    fz = ((float)z) * maxZ;
                }
                zPoints.add(fz);
            }
            in.close();
        }
    },
    ByteDownscalingGZip {
        float maxX;
        float minX;
        float maxY;
        float minY;
        float maxZ;
        float minZ;

        @Override
        public void write(OutputStream rOut, AccelerationCollection collection)
                throws IOException {
            GZIPOutputStream out = new GZIPOutputStream(rOut);
            maxX = collection.maxX();
            minX = collection.minX();
            maxY = collection.maxY();
            minY = collection.minY();
            maxZ = collection.maxZ();
            minZ = collection.minZ();

            ByteWriter writer = new ByteWriter(out);
            writer.writeFloat(maxX);
            writer.writeFloat(minX);
            writer.writeFloat(maxY);
            writer.writeFloat(minY);
            writer.writeFloat(maxZ);
            writer.writeFloat(minZ);

            for (int i = 0; i < collection.size(); i++) {
                float x = collection.get(i).getX();
                byte bx;
                if (x > 0) {
                    bx = (byte)((x / maxX) * 127);
                } else {
                    bx = (byte)((x / minX) * 127);
                }
                out.write(bx);

                float y = collection.get(i).getY();
                byte by;
                if (y > 0) {
                    by = (byte)((y / maxY) * 127);
                } else {
                    by = (byte)((y / minY) * 127);
                }
                out.write(by);

                float z = collection.get(i).getZ();
                byte bz;
                if (z > 0) {
                    bz = (byte)((z / maxZ) * 127);
                } else {
                    bz = (byte)((z / minZ) * 127);
                }
                out.write(bz);
            }
            out.close();
        }

        @Override
        public void read(InputStream rIn) throws IOException {
            super.read(rIn);
            GZIPInputStream in = new GZIPInputStream(rIn);
            ByteReader reader = new ByteReader(in);
            maxX = reader.readFloat();
            minX = reader.readFloat();
            maxY = reader.readFloat();
            minY = reader.readFloat();
            maxZ = reader.readFloat();
            minZ = reader.readFloat();

            while (in.available() > 0) {
                byte x = (byte)in.read();
                float fx;
                if (x < 0) {
                    fx = ((float)x) * minX;
                } else {
                    fx = ((float)x) * maxX;
                }
                xPoints.add(fx);

                byte y = (byte)in.read();
                float fy;
                if (y < 0) {
                    fy = ((float)y) * minY;
                } else {
                    fy = ((float)y) * maxY;
                }
                yPoints.add(fy);

                byte z = (byte)in.read();
                float fz;
                if (z < 0) {
                    fz = ((float)z) * minZ;
                } else {
                    fz = ((float)z) * maxZ;
                }
                zPoints.add(fz);
            }
            in.close();
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

    public class StreamStats {
        public final double ratio;
        public final long length;

        public StreamStats(double ratio, long length) {
            this.ratio = ratio;
            this.length = length;
        }
    }

    public StreamStats ratio(AccelerationCollection points) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            write(out, points);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = out.toByteArray();

        return new StreamStats(
                (double)bytes.length / points.byteSize(), bytes.length);
    }

    public void read(InputStream in) throws IOException {
        xPoints.clear();
        yPoints.clear();
        zPoints.clear();
    }

    private void makePointsPositive() {
        for (int i = 0; i < xPoints.size(); i++) {
            xPoints.set(i, Math.abs(xPoints.get(i)));
            yPoints.set(i, Math.abs(yPoints.get(i)));
            zPoints.set(i, Math.abs(zPoints.get(i)));
        }
    }

    private float heightDelta(ArrayList<Float> pts) {
        float max = pts.get(0);
        for (float d : pts) {
            if (max < d) {
                max = d;
            }
        }
        return max;
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
        makePointsPositive();
        switch(pointType) {
            case X_POINTS: pts = xPoints; break;
            case Y_POINTS: pts = yPoints; break;
            case Z_POINTS: pts = zPoints; break;
            default: throw new IllegalArgumentException();
        }

        float dx = ((float)c.getWidth()) / ((float)pts.size());
        float dy = ((float)c.getHeight()) / (heightDelta(pts));

        Paint paint = new Paint();
        paint.setColor(color);

        for (int i = 1; i < pts.size(); i++) {
            float x1 = (i - 1) * dx;
            float x2 = i * dx;
            float y1 = (pts.get(i - 1)) * dy;
            float y2 = (pts.get(i)) * dy;

            c.drawLine(x1, y1, x2, y2, paint);
        }
    }
}
