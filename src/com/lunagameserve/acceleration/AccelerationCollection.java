package com.lunagameserve.acceleration;

import com.lunagameserve.nbt.NBTException;
import com.lunagameserve.nbt.NBTSerializableList;
import com.lunagameserve.nbt.NBTSerializableListAdapter;
import com.lunagameserve.nbt.Tag;

import java.util.ArrayList;

/**
 * Created by Ross on 2/27/2015.
 */
public class AccelerationCollection extends NBTSerializableListAdapter {

    private ArrayList<AccelerationPoint> points =
            new ArrayList<AccelerationPoint>();

    public void addPoint(AccelerationPoint pt) {
        this.points.add(pt);
    }

    public AccelerationPoint get(int i) {
        return points.get(i);
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

    public void clear() {
        this.points.clear();
    }
}
