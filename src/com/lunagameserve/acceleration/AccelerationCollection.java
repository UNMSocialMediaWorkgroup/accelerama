package com.lunagameserve.acceleration;

import com.lunagameserve.compression.ByteReader;
import com.lunagameserve.compression.ByteWriter;
import com.lunagameserve.nbt.NBTException;
import com.lunagameserve.nbt.NBTSerializableListAdapter;
import com.lunagameserve.nbt.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Ross on 2/27/2015.
 */
public class AccelerationCollection extends NBTSerializableListAdapter {

    private ArrayList<AccelerationPoint> points =
            new ArrayList<AccelerationPoint>();

    public int byteSize() {
        return points.size() * AccelerationPoint.SIZE;
    }

    public void addPoint(AccelerationPoint pt) {
        this.points.add(pt);
    }

    public AccelerationPoint get(int i) {
        return points.get(i);
    }

    public float maxX() {
        float max = points.get(0).getX();
        for (AccelerationPoint p : points) {
            if (p.getX() > max) {
                max = p.getX();
            }
        }
        return max;
    }

    public float minX() {
        float min = points.get(0).getX();
        for (AccelerationPoint p : points) {
            if (p.getX() < min) {
                min = p.getX();
            }
        }
        return min;
    }

    public float maxY() {
        float max = points.get(0).getY();
        for (AccelerationPoint p : points) {
            if (p.getY() > max) {
                max = p.getY();
            }
        }
        return max;
    }

    public float minY() {
        float min = points.get(0).getY();
        for (AccelerationPoint p : points) {
            if (p.getY() < min) {
                min = p.getY();
            }
        }
        return min;
    }

    public float maxZ() {
        float max = points.get(0).getZ();
        for (AccelerationPoint p : points) {
            if (p.getZ() > max) {
                max = p.getZ();
            }
        }
        return max;
    }

    public float minZ() {
        float min = points.get(0).getZ();
        for (AccelerationPoint p : points) {
            if (p.getZ() < min) {
                min = p.getZ();
            }
        }
        return min;
    }

    public int size() {
        return points.size();
    }

    @Override
    protected Tag listItemToTag(int i) {
        try {
            return points.get(i).toCompound();
        } catch (NBTException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void listItemFromTag(int i, Tag tag) {
        AccelerationPoint pt = new AccelerationPoint();
        pt.fromCompound((Tag.Compound)tag);
        points.add(pt);
    }

    @Override
    protected int listItemCount() {
        return points.size();
    }

    @Override
    protected String listName() {
        return "accelerationCollection";
    }

    public void writeAsBytes(OutputStream out) throws IOException {
        ByteWriter writer = new ByteWriter(out);
        writer.writeInt(size());
        for (int i = 0; i < size(); i++) {
            points.get(i).writeAsBytes(out);
        }
    }

    public void readFromBytes(InputStream in) throws IOException {
        ByteReader reader = new ByteReader(in);
        int size = reader.readInt();
        for (int i = 0; i < size; i++) {
            AccelerationPoint pt = new AccelerationPoint();
            pt.readFromBytes(in);
            addPoint(pt);
        }
    }

    public void clear() {
        this.points.clear();
    }
}
