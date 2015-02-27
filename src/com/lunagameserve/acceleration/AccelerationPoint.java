package com.lunagameserve.acceleration;

import com.lunagameserve.nbt.NBTException;
import com.lunagameserve.nbt.NBTSerializableObject;
import com.lunagameserve.nbt.Tag;

/**
 * Created by Ross on 2/27/2015.
 */
public class AccelerationPoint implements NBTSerializableObject {
    private float[] values;
    private long timestamp;

    public AccelerationPoint() { }

    public AccelerationPoint(float[] values, long timestamp) {
        this.timestamp = timestamp;
        this.values = new float[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public float getX() {
        return values[0];
    }

    public float getY() {
        return values[1];
    }

    public float getZ() {
        return values[2];
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Tag.Compound toCompound() throws NBTException {
        return new Tag.Compound.Builder()
                .addFloat("x", values[0])
                .addFloat("y", values[1])
                .addFloat("z", values[2])
                .addLong("t", timestamp)
                .toCompound("accelerationPoint");
    }

    @Override
    public void fromCompound(Tag.Compound compound) {
        this.values[0] = compound.getFloat("x");
        this.values[1] = compound.getFloat("y");
        this.values[2] = compound.getFloat("z");
        this.timestamp = compound.getLong("t");
    }
}
