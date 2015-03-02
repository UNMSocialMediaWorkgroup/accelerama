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
 * Created by Ross on 2/27/2015.
 */
public class ResultsActivity extends ToastActivity {

    private AccelerationCollection points = new AccelerationCollection();

    private LinearLayout baseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.results_layout);

        this.baseLayout = (LinearLayout)findViewById(R.id.results_layout);

        readPoints();

        baseLayout.post(drawCompressionBox());
    }

    private void queueRedraw() {
        new Thread(makeUIRunnable(drawCompressionBox())).start();
    }

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

    private void readPoints() {
        try {
             ByteArrayInputStream in =
                     new ByteArrayInputStream(
                             getIntent().getByteArrayExtra("points"));

            points.clear();
            points.readFromBytes(in);
            toastLong("Points read!");

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
