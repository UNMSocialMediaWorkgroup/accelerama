package com.lunagameserve.accelerama.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.lunagameserve.accelerama.R;
import com.lunagameserve.accelerama.activities.util.ToastActivity;
import com.lunagameserve.accelerama.activities.util.UIActivity;
import com.lunagameserve.acceleration.AccelerationCollection;
import com.lunagameserve.compression.Compressor;
import com.lunagameserve.nbt.NBTException;
import com.lunagameserve.nbt.Tag;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Ross on 2/27/2015.
 */
public class ResultsActivity extends ToastActivity {

    private AccelerationCollection points = new AccelerationCollection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.results_layout);

        queueRedraw();
    }

    private void queueRedraw() {
        new Thread(makeUIRunnable(drawCompressionBox())).start();
    }

    private Runnable drawCompressionBox() {
        return new Runnable() {
            @Override
            public void run() {
                ImageView iv = (ImageView)findViewById(R.id.results_imageview);
                if (iv.getHeight() <= 0 || iv.getWidth() <= 0) {
                    Log.d("Results", "Trying to render again...");
                    queueRedraw();
                } else {
                    Log.d("Results", "Making ImageView box");
                    readPoints();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        Compressor.UncompressedBuffer.write(out, points);
                        ByteArrayInputStream in =
                                new ByteArrayInputStream(out.toByteArray());
                        Compressor.UncompressedBuffer.read(in);
                        Bitmap finalBmp = Bitmap.createBitmap(iv.getWidth(), iv
                                .getHeight(), Bitmap.Config.ARGB_8888);
                        Compressor.UncompressedBuffer.render(
                                new Canvas(finalBmp), Compressor.X_POINTS,
                                Color.WHITE);
                        iv.setImageBitmap(finalBmp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void readPoints() {
        try {
            GZIPInputStream in =
                new GZIPInputStream(
                    new FileInputStream(
                        new File(getFilesDir(), "output.nbt")));

            points.clear();
            Tag.Compound root = Tag.readCompound(in);
            points.fromList(root.getList("points"));
            toastLong("Points read!");

            in.close();
        } catch (NBTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
