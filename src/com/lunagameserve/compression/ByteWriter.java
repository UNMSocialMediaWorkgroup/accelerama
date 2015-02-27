package com.lunagameserve.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Ross on 2/27/2015.
 */
public class ByteWriter {
    private OutputStream out;

    public ByteWriter(OutputStream out) {
        this.out = out;
    }

    public void writeDouble(double d) throws IOException {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(d);
        out.write(bytes);
    }

    public void writeFloat(float f) throws IOException {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putFloat(f);
        out.write(bytes);
    }

    public void writeLong(long l) throws IOException {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(l);
        out.write(bytes);
    }
}
