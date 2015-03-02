package com.lunagameserve.compression;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by sixstring982 on 3/1/15.
 */
public class BitWriter extends OutputStream {

    private int currentByte = 0;
    private int currentBytePos = 0;

    private OutputStream out;

    public BitWriter(OutputStream out) {
        this.out = out;
    }

    public void writeBit(int bit) throws IOException {
        bit &= 1;

        currentByte |= (bit << currentBytePos);

        currentBytePos = (currentBytePos + 1) % 8;
        if (currentBytePos == 0) {
            out.write(currentByte);
            currentByte = 0;
        }
    }

    public void writeBits(int word, int bits) throws IOException {
        for (int i = 0; i < bits; i++) {
            writeBit(word >> i);
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        writeBits(oneByte, 8);
    }

    @Override
    public void close() throws IOException {
        super.close();

        while(currentBytePos > 0) {
            writeBit(0);
        }

        out.close();
    }
}
