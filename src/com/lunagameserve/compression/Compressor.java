package com.lunagameserve.compression;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.lunagameserve.acceleration.AccelerationCollection;
import com.lunagameserve.acceleration.AccelerationPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * An enumeration of both lossless and lossy floating point stream compression
 * algorithms.
 *
 * @author Six
 * @since March 2, 2015
 */
public enum Compressor {
    /**
     * A {@link com.lunagameserve.compression.Compressor} which performs no
     * compression. Used as a benchmark.
     */
    UncompressedBuffer {

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        @Override
        public void read(InputStream in) throws IOException {
            super.read(in);
            ArrayList<AccelerationPoint> list =
                    new ArrayList<AccelerationPoint>();
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
    /**
     * A {@link com.lunagameserve.compression.Compressor} which simply runs
     * the uncompressed data through a {@link java.util.zip.GZIPOutputStream}.
     */
    StandardGZip {
        private ArrayList<AccelerationPoint> list =
                new ArrayList<AccelerationPoint>();

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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
    /**
     * A {@link com.lunagameserve.compression.Compressor} which downscales each
     * floating point value to a byte value.
     */
    ByteDownscaling {
        float maxX;
        float minX;
        float maxY;
        float minY;
        float maxZ;
        float minZ;

        /** {@inheritDoc} */
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
                out.write(generatePoint(collection.get(i).getX(),
                                        minX, maxX - minX));

                out.write(generatePoint(collection.get(i).getY(),
                        minY, maxY - minY));

                out.write(generatePoint(collection.get(i).getZ(),
                        minZ, maxZ - minZ));

            }
            out.close();
        }

        private byte generatePoint(float realP, float min, float maxDelta) {
            float delta = realP - min;
            float ratio = delta / maxDelta;
            return (byte) (ratio * 127);
        }

        private float fromPoint(byte pt, float min, float maxDelta) {
            return (pt / 127f) * maxDelta + min;
        }

        /** {@inheritDoc} */
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
                xPoints.add(fromPoint((byte)in.read(), minX, maxX - minX));
                yPoints.add(fromPoint((byte)in.read(), minY, maxY - minY));
                zPoints.add(fromPoint((byte)in.read(), minZ, maxZ - minZ));
            }
            in.close();
        }
    },
    /**
     * A {@link com.lunagameserve.compression.Compressor} which downscales each
     * floating point value to a byte value, then runs all bytes through a
     * {@link java.util.zip.GZIPOutputStream}.
     */
    ByteDownscalingGZip {
        float maxX;
        float minX;
        float maxY;
        float minY;
        float maxZ;
        float minZ;

        /** {@inheritDoc} */
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
                out.write(generatePoint(collection.get(i).getX(),
                        minX, maxX - minX));

                out.write(generatePoint(collection.get(i).getY(),
                        minY, maxY - minY));

                out.write(generatePoint(collection.get(i).getZ(),
                        minZ, maxZ - minZ));

            }
            out.close();
        }

        private byte generatePoint(float realP, float min, float maxDelta) {
            float delta = realP - min;
            float ratio = delta / maxDelta;
            return (byte) (ratio * 127);
        }

        private float fromPoint(byte pt, float min, float maxDelta) {
            return (pt / 127f) * maxDelta + min;
        }

        /** {@inheritDoc} */
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
                xPoints.add(fromPoint((byte)in.read(), minX, maxX - minX));
                yPoints.add(fromPoint((byte)in.read(), minY, maxY - minY));
                zPoints.add(fromPoint((byte)in.read(), minZ, maxZ - minZ));
            }
            in.close();
        }
    },
    /**
     * A {@link com.lunagameserve.compression.Compressor} which downscales each
     * floating point value to a nybble value.
     */
    NybbleDownsampling {
        float maxX;
        float minX;
        float maxY;
        float minY;
        float maxZ;
        float minZ;

        /** {@inheritDoc} */
        @Override
        public void write(OutputStream out, AccelerationCollection collection)
                throws IOException {
            maxX = collection.maxX();
            minX = collection.minX();
            maxY = collection.maxY();
            minY = collection.minY();
            maxZ = collection.maxZ();
            minZ = collection.minZ();

            BitWriter bwrite = new BitWriter(out);
            ByteWriter writer = new ByteWriter(bwrite);
            writer.writeFloat(maxX);
            writer.writeFloat(minX);
            writer.writeFloat(maxY);
            writer.writeFloat(minY);
            writer.writeFloat(maxZ);
            writer.writeFloat(minZ);

            for (int i = 0; i < collection.size(); i++) {
                byte nyb = (byte)(
                        (generatePoint(collection.get(i).getX(),
                                minX, maxX - minX) >> NYBBLE_LEFT));
                bwrite.writeBits(nyb, NYBBLE_SIZE);

                bwrite.writeBits(
                        generatePoint(
                                collection.get(i).getY(),
                                minY, maxY - minY) >> NYBBLE_LEFT, NYBBLE_SIZE);

                bwrite.writeBits(
                        generatePoint(
                                collection.get(i).getZ(),
                                minZ, maxZ - minZ) >> NYBBLE_LEFT, NYBBLE_SIZE);
            }
            writer.close();
        }

        /**
         * Converts a specified floating point value to a {@code byte} value
         * which is used during output.
         *
         * @param realP The floating point value to convert.
         *
         * @param min The minimum floating point value in the data set.
         *
         * @param maxDelta The range of the floating point values in the
         *                 data set.
         *
         * @return The {@code byte} value representing the scaled value which
         *         should be used for output.
         */
        private byte generatePoint(float realP, float min, float maxDelta) {
            float delta = realP - min;
            float ratio = delta / maxDelta;
            return (byte) (ratio * 127);
        }

        /**
         * Converts a specified {@code byte} value to a {@code float} which
         * is used during decompression.
         *
         * @param pt The {@code byte} value which is read duing decompression.
         *
         * @param min The minimum floating point value in the data set.
         *
         * @param maxDelta The range of the floating point values in the
         *                 data set.
         *
         * @return The {@code float} value representing the decompressed
         *         value.
         */
        private float fromPoint(byte pt, float min, float maxDelta) {
            return (pt / 127f) * maxDelta + min;
        }

        /** {@inheritDoc} */
        @Override
        public void read(InputStream in) throws IOException {
            super.read(in);
            BitReader bread = new BitReader(in);
            ByteReader reader = new ByteReader(bread);
            maxX = reader.readFloat();
            minX = reader.readFloat();
            maxY = reader.readFloat();
            minY = reader.readFloat();
            maxZ = reader.readFloat();
            minZ = reader.readFloat();

            while (in.available() > 0) {
                xPoints.add(fromPoint((byte)(bread.readBits(NYBBLE_SIZE) <<
                                NYBBLE_LEFT),
                        minX, maxX - minX));
                yPoints.add(fromPoint((byte)(bread.readBits(NYBBLE_SIZE) <<
                                NYBBLE_LEFT),
                        minY, maxY - minY));
                zPoints.add(fromPoint((byte)(bread.readBits(NYBBLE_SIZE) <<
                                NYBBLE_LEFT),
                        minZ, maxZ - minZ));
            }
            reader.close();
        }
    },
    /**
     * A {@link com.lunagameserve.compression.Compressor} which downscales each
     * floating point value to a nybble value, then runs all bytes through a
     * {@link java.util.zip.GZIPOutputStream}.
     */
    NybbleDownsamplingGZip {
        float maxX;
        float minX;
        float maxY;
        float minY;
        float maxZ;
        float minZ;

        /** {@inheritDoc} */
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

            BitWriter bwrite = new BitWriter(out);
            ByteWriter writer = new ByteWriter(bwrite);
            writer.writeFloat(maxX);
            writer.writeFloat(minX);
            writer.writeFloat(maxY);
            writer.writeFloat(minY);
            writer.writeFloat(maxZ);
            writer.writeFloat(minZ);

            for (int i = 0; i < collection.size(); i++) {
                byte nyb = (byte)(
                        (generatePoint(collection.get(i).getX(),
                                minX, maxX - minX) >> NYBBLE_LEFT));
                bwrite.writeBits(nyb, NYBBLE_SIZE);

                bwrite.writeBits(
                        generatePoint(
                                collection.get(i).getY(),
                                minY, maxY - minY) >> NYBBLE_LEFT, NYBBLE_SIZE);

                bwrite.writeBits(
                        generatePoint(
                                collection.get(i).getZ(),
                                minZ, maxZ - minZ) >> NYBBLE_LEFT, NYBBLE_SIZE);
            }
            writer.close();
        }

        /**
         * Converts a specified floating point value to a {@code byte} value
         * which is used during output.
         *
         * @param realP The floating point value to convert.
         *
         * @param min The minimum floating point value in the data set.
         *
         * @param maxDelta The range of the floating point values in the
         *                 data set.
         *
         * @return The {@code byte} value representing the scaled value which
         *         should be used for output.
         */
        private byte generatePoint(float realP, float min, float maxDelta) {
            float delta = realP - min;
            float ratio = delta / maxDelta;
            return (byte) (ratio * 127);
        }

        /**
         * Converts a specified {@code byte} value to a {@code float} which
         * is used during decompression.
         *
         * @param pt The {@code byte} value which is read duing decompression.
         *
         * @param min The minimum floating point value in the data set.
         *
         * @param maxDelta The range of the floating point values in the
         *                 data set.
         *
         * @return The {@code float} value representing the decompressed
         *         value.
         */
        private float fromPoint(byte pt, float min, float maxDelta) {
            return (pt / 127f) * maxDelta + min;
        }

        /** {@inheritDoc} */
        @Override
        public void read(InputStream rIn) throws IOException {
            super.read(rIn);
            GZIPInputStream in = new GZIPInputStream(rIn);
            BitReader bread = new BitReader(in);
            ByteReader reader = new ByteReader(bread);
            maxX = reader.readFloat();
            minX = reader.readFloat();
            maxY = reader.readFloat();
            minY = reader.readFloat();
            maxZ = reader.readFloat();
            minZ = reader.readFloat();

            while (in.available() > 0) {
                xPoints.add(fromPoint((byte)(bread.readBits(NYBBLE_SIZE) <<
                                NYBBLE_LEFT),
                        minX, maxX - minX));
                yPoints.add(fromPoint((byte)(bread.readBits(NYBBLE_SIZE) <<
                                NYBBLE_LEFT),
                        minY, maxY - minY));
                zPoints.add(fromPoint((byte)(bread.readBits(NYBBLE_SIZE) <<
                                NYBBLE_LEFT),
                        minZ, maxZ - minZ));
            }
            reader.close();
        }
    };

    /**
     * The size of the "nybble" written by the two nybble compression
     * algorithms.
     */
    protected static final int NYBBLE_SIZE = 4;

    /**
     * The remainder of a byte if {@link #NYBBLE_SIZE} bits are used.
     */
    protected static final int NYBBLE_LEFT = 8 - NYBBLE_SIZE;

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

    private float maxPoint(ArrayList<Float> pts) {
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

    private float heightDelta(ArrayList<Float> pts) {
        return maxPoint(pts) - minPoint(pts);
    }

    public void render(Canvas c, int pointType, int color) {
        ArrayList<Float> pts;
        switch(pointType) {
            case X_POINTS: pts = xPoints; break;
            case Y_POINTS: pts = yPoints; break;
            case Z_POINTS: pts = zPoints; break;
            default: throw new IllegalArgumentException();
        }

        float dx = ((float)c.getWidth()) / ((float)pts.size());
        float dy = ((float) c.getHeight()) / (heightDelta(pts));
        float sy = minPoint(pts);

        Paint paint = new Paint();
        paint.setColor(color);

        Paint axisPaint = new Paint();
        axisPaint.setColor(Color.GRAY);
        c.drawLine(0, (0f - sy) * dy, c.getWidth(), (0f - sy) * dy, axisPaint);

        for (int i = 1; i < pts.size(); i++) {
            float x1 = (i - 1) * dx;
            float x2 = i * dx;
            float y1 = (pts.get(i - 1) - sy) * dy;
            float y2 = (pts.get(i) - sy) * dy;

            c.drawLine(x1, y1, x2, y2, paint);
        }
    }
}
