package com.lunagameserve.accelerama.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lunagameserve.accelerama.R;
import com.lunagameserve.accelerama.activities.util.ToastActivity;
import com.lunagameserve.acceleration.AccelerationCollection;
import com.lunagameserve.compression.Compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The {@link android.app.Activity} which displays the results
 * gathered from a
 * {@link com.lunagameserve.accelerama.activities.CollectionActivity}.
 *
 * @author Six
 * @since March 2, 2015
 */
public class ResultsActivity extends ToastActivity {

    /**
     * The collection of
     * {@link com.lunagameserve.acceleration.AccelerationPoint}s which are
     * to be analyzed.
     */
    private AccelerationCollection points = new AccelerationCollection();

    /**
     * The base {@link android.text.Layout} of this
     * {@link com.lunagameserve.accelerama.activities.ResultsActivity}
     * which holds all results.
     */
    private LinearLayout baseLayout;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.results_layout);

        this.baseLayout = (LinearLayout)findViewById(R.id.results_layout);

        try {
            readPoints();
        } catch (IOException e) {
            toastLong("Could not read datapoints from previous action. " +
                      "Stopping.");
            e.printStackTrace();
            finish();
        }

        baseLayout.post(drawCompressionBox());
    }

    /**
     * Generates a {@link java.lang.Runnable} which delegates the drawing of
     * all UI elements.
     *
     * @return A {@link java.lang.Runnable} which delegates the drawing of
     *         all UI elements.
     */
    private Runnable drawCompressionBox() {
        return new Runnable() {
            @Override
            public void run() {
                for(Compressor c : Compressor.values()) {
                    addImageView(c);
                }
            }
        };
    }

    /**
     * Generates a {@link java.lang.Runnable} which delegates the setup
     * of an {@link android.widget.ImageView}s involved with using a single
     * specified {@link com.lunagameserve.compression.Compressor} on
     * {@link #points}.
     *
     * @param compressor The {@link com.lunagameserve.compression.Compressor}
     *                   used to generate the interior of a specified
     *                   {@link android.widget.ImageView}.
     *
     * @param iv The {@link android.widget.ImageView} to render the output
     *           of {@code compressor}.
     *
     * @return The {@link java.lang.Runnable}, which will delegate to the UI
     *         {@link java.lang.Thread}, which performs this
     *         {@link android.widget.ImageView} setup.
     */
    private Runnable setupImageRunnable(final Compressor compressor,
                                        final ImageView iv) {
        return makeUIRunnable(new Runnable() {
            @Override
                    public void run() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            compressor.write(out, points);
            ByteArrayInputStream in =
                    new ByteArrayInputStream(out.toByteArray());
            compressor.read(in);
            Bitmap finalBmp = Bitmap.createBitmap(
                    points.size(), 768,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(finalBmp);
            compressor.render(c,
                    Compressor.X_POINTS, Color.RED);
            compressor.render(c,
                    Compressor.Y_POINTS, Color.GREEN);
            compressor.render(c,
                    Compressor.Z_POINTS, Color.BLUE);
            iv.setImageBitmap(finalBmp);

            Log.d("Results", "Completed ViewBox");
        } catch (IOException e) {
            e.printStackTrace();
        }}});
    }

    /**
     * Adds an {@link android.widget.ImageView} to {@link #baseLayout} which
     * will eventually contain the compressed output of a specified compressor.
     * This method delegates the rendering to the new
     * {@link android.widget.ImageView}; no further action is needed for this
     * to happen.
     *
     * @param compressor The {@link com.lunagameserve.compression.Compressor}
     *                   which will be used to compress {@link #points}, then
     *                   used to render to the created
     *                   {@link android.widget.ImageView}.
     */
    private void addImageView(Compressor compressor) {
        Log.d("Results", "Making ImageView box");

        TextView tv = new TextView(getBaseContext());
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        Compressor.StreamStats stats = compressor.ratio(points);
        tv.setTextSize(20f);
        tv.setText(compressor.toString() +
                ": " + stats.ratio + " (" + stats.length + "b, or " +
                ((float)stats.length / CollectionActivity.SECONDS) +
                "b/s)");
        baseLayout.addView(tv);

        ImageView iv = new ImageView(getBaseContext());
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        iv.setLayoutParams(params);

        baseLayout.addView(iv);

        iv.post(setupImageRunnable(compressor, iv));

    }

    /**
     * Reads an {@link com.lunagameserve.acceleration.AccelerationCollection}
     * from the {@link android.content.Intent} {@code byte[]} extra named
     * {@code "points"} and stores it into {@link #points}. This
     * {@link com.lunagameserve.acceleration.AccelerationCollection#clear()}s
     * {@link #points} when called.
     */
    private void readPoints() throws IOException {
        ByteArrayInputStream in =
                new ByteArrayInputStream(
                        getIntent().getByteArrayExtra("points"));

        points.clear();
        points.readFromBytes(in);
        toastLong("Points read!");

        in.close();
    }
}
