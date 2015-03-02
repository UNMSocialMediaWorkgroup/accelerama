package com.lunagameserve.compression.test;

import com.lunagameserve.compression.BitReader;
import com.lunagameserve.compression.BitWriter;
import com.lunagameserve.compression.ByteReader;
import com.lunagameserve.compression.ByteWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by sixstring982 on 3/1/15.
 */
public enum BitIOTest {
    Basic {
        @Override
        protected boolean run() throws IOException {
            setup();
            writer.writeBit(1);

            flip();
            return reader.readBit() == 1;
        }
    },
    Nybble {
        @Override
        protected boolean run() throws IOException {
            setup();
            writer.writeBit(1);
            writer.writeBit(0);
            writer.writeBit(1);
            writer.writeBit(1);

            flip();
            return reader.read() == 13;
        }
    },
    Floats {
        @Override
        protected boolean run() throws IOException {
            setup();
            ByteWriter w = new ByteWriter(writer);
            w.writeFloat(0.3f);

            flip();
            ByteReader r = new ByteReader(reader);
            float f = r.readFloat();
            return f == 0.3f;
        }
    },
    String {
        @Override
        protected boolean run() throws IOException {
            String s = "This is a test string!";
            byte[] buffer = s.getBytes();

            setup();
            writer.write(buffer);

            flip();

            //noinspection ResultOfMethodCallIgnored
            reader.read(buffer);
            return new String(buffer).equals(s);
        }
    };

    protected abstract boolean run() throws IOException;

    protected BitReader reader;
    protected BitWriter writer;

    private ByteArrayOutputStream byteOut;

    protected void setup() {
        writer = new BitWriter(
                byteOut = new ByteArrayOutputStream());
    }

    protected void flip() throws IOException {
        writer.close();
        reader = new BitReader(
                new ByteArrayInputStream(byteOut.toByteArray()));
    }

    private static void testFail(BitIOTest test, Exception e) {
        String message = "";
        if (e != null) {
            message = e.toString() + ": " + e.getMessage();
        }
        System.err.println("Test Failed (" + test.toString() + "): " +
                message);
    }

    /**
     * Runs all {@link BitIOTest}s inside this test suite. Logs to
     * {@link System#err} if a test fails, and logs to
     * {@link System#out} if a test passes.
     *
     * @return {@code true} if all tests pass, else {@code false}.
     */
    public static boolean runTests() {
        boolean allPassed = true;
        for (BitIOTest t : values()) {
            try {
                if (!t.run()) {
                    testFail(t, null);
                    allPassed = false;
                }
                System.out.println("Test Passed: " + t.toString());
            } catch (Exception e) {
                testFail(t, e);
                allPassed = false;
            }
        }
        return allPassed;
    }
}
