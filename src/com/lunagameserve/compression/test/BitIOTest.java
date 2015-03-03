package com.lunagameserve.compression.test;

import com.lunagameserve.compression.BitReader;
import com.lunagameserve.compression.BitWriter;
import com.lunagameserve.compression.ByteReader;
import com.lunagameserve.compression.ByteWriter;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A small test suite meant to test the correctness of the
 * {@link com.lunagameserve.compression.BitWriter} and
 * {@link com.lunagameserve.compression.BitReader} writing utilities. Due to
 * the complexity of running JUnit tests on Android, this is not a JUnit test.
 * Instead, all tests of this form must be invoked by running
 * {@link com.lunagameserve.compression.test.BitIOTest#main(String[])}.
 *
 * @author Six
 * @since March 2, 2015
 */
public enum BitIOTest {
    /**
     * A basic test which writes and reads a single byte using a
     * {@link com.lunagameserve.compression.BitReader} and a
     * {@link com.lunagameserve.compression.BitWriter}.
     */
    Basic {
        /** {@inheritDoc} */
        @Override
        protected boolean run() throws IOException {
            setup();
            writer.writeBit(1);

            flip();
            return reader.readBit() == 1;
        }
    },
    /**
     * A short test which verifies the writing of four bits and the reading
     * of these bits as a byte once read.
     */
    Nybble {
        /** {@inheritDoc} */
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
    /**
     * A short test which verifies that a
     * {@link com.lunagameserve.compression.BitWriter} may be wrapped by
     * a {@link com.lunagameserve.compression.ByteWriter} in order to write
     * floating point values, and that a
     * {@link com.lunagameserve.compression.BitReader} may be wrapped by
     * a {@link com.lunagameserve.compression.ByteReader} to read floating
     * point values.
     */
    Floats {
        /** {@inheritDoc} */
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
    /**
     * A short test which verifies that a {@link String} value may be
     * written and read.
     */
    String {
        /** {@inheritDoc} */
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

    /**
     * Executes the test represented by this
     * {@link com.lunagameserve.compression.test.BitIOTest}.
     *
     * @return {@code true} if this test passes, else {@code false}.
     *
     * @throws IOException If there is an IO error, which should be treated
     *                     as a test failure.
     */
    protected abstract boolean run() throws IOException;

    /**
     * A {@link com.lunagameserve.compression.BitReader} which is set up by the
     * {@link #setup} method for convenience. This should only be used after a
     * call to {@link #setup} and before a call to {@link #flip}
     */
    protected BitReader reader;

    /**
     * A {@link com.lunagameserve.compression.BitWriter} which is set up by the
     * {@link #flip} method for convenience. This should only be used after a
     * call to {@link #flip}.
     */
    protected BitWriter writer;

    /**
     * A {@link java.io.ByteArrayOutputStream} used as a bridge between
     * {@link #reader} and {@link #writer} for testing purposes.
     */
    private ByteArrayOutputStream byteOut;

    /**
     * Sets up {@link #writer} for general IO testing. This must be called
     * before using {@link #writer}.
     */
    protected void setup() {
        writer = new BitWriter(
                byteOut = new ByteArrayOutputStream());
    }

    /**
     * Sets up {@link #reader} for general IO testing, and closing
     * {@link #writer}. {@link #writer} must not be used after this method is
     * called, and {@link #reader} must only be used after this method is
     * called.
     *
     * @throws IOException If {@link #writer} cannot
     *                   {@link com.lunagameserve.compression.BitWriter#close()}
     *                   for any reason.
     */
    protected void flip() throws IOException {
        writer.close();
        reader = new BitReader(
                new ByteArrayInputStream(byteOut.toByteArray()));
    }

    /**
     * Called once a {@link com.lunagameserve.compression.test.BitIOTest} has
     * been deemed to have failed.
     *
     * @param test The {@link com.lunagameserve.compression.test.BitIOTest}
     *             which is deemed to have failed.
     *
     * @param e An {@link java.lang.Exception}, which should be included if
     *          the running test threw one. If not, may be {@code null}.
     */
    private static void testFail(BitIOTest test, @Nullable Exception e) {
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

    /**
     * a canonical {@code main(String[] args)} method which may be used to
     * run all of the tests contained in this
     * {@link com.lunagameserve.compression.test.BitIOTest} suite.
     *
     * @param args The command line arguments to this process instance. These
     *             are not used.
     */
    public static void main(String[] args) {
        runTests();
    }
}
