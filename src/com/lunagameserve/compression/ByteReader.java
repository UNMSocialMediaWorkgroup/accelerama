package com.lunagameserve.compression;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Ross on 2/27/2015.
 */
public class ByteReader {
    private InputStream in;

    public ByteReader(InputStream in) {
        this.in = in;
    }

    public float readFloat() throws IOException {
        return ByteBuffer.wrap(readBytes(4)).getFloat();
    }

    public long readLong() throws IOException {
        return ByteBuffer.wrap(readBytes(8)).getLong();
    }

    private byte[] readBytes(int count) throws IOException {
        byte[] bytes = new byte[count];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte)in.read();
        }
        return bytes;
    }
}
