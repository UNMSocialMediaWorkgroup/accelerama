package com.lunagameserve.compression;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sixstring982 on 3/1/15.
 */
public class BitReader extends InputStream {

    private int currentByte = 0;
    private int currentBytePos = 0;

    private final InputStream in;

    public BitReader(InputStream in) {
        this.in = in;
    }

    public int readBit() throws IOException {
        if (currentBytePos == 0) {
            currentByte = in.read();
            if (currentByte == -1) {
                return -1;
            }
        }

        int readVal = currentByte & (1 << currentBytePos);
        if (readVal > 0) {
            readVal = 1;
        }
        currentBytePos++;
        currentBytePos %= 8;

        return readVal;
    }

    public int readBits(int bits) throws IOException {
        int readByte = 0;
        for (int i = 0; i < bits; i++) {
            int bit = readBit();

            if (bit == -1) {
                if (currentBytePos == 0) {
                    return -1;
                } else {
                    return readByte;
                }
            }

            readByte |= (bit << i);
        }
        return readByte;
    }

    @Override
    public int read() throws IOException {
        return readBits(8);
    }
}
