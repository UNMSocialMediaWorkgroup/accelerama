package com.lunagameserve.accelerama.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import com.lunagameserve.accelerama.activities.util.ToastActivity;
import com.lunagameserve.acceleration.AccelerationCollection;
import com.lunagameserve.acceleration.AccelerationPoint;
import com.lunagameserve.gl.CubeRenderer;
import com.lunagameserve.gl.geometry.Util;
import com.lunagameserve.nbt.NBTException;
import com.lunagameserve.nbt.Tag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Ross on 2/27/2015.
 */
public class CollectionActivity extends ToastActivity
                                implements SensorEventListener{

    private GLSurfaceView glView;

    private CubeRenderer renderer;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotation;

    private AtomicBoolean collecting = new AtomicBoolean(true);
    private long startTime = 0L;
    private long maxTicks = 1000000000L * 5; /* * 60 * 2;  Two minutes */

    private AccelerationCollection points = new AccelerationCollection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sensorManager == null) {
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            rotation =
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            accelerometer =
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            this.glView = new GLSurfaceView(this);
            this.renderer = new CubeRenderer(getBaseContext());
            glView.setRenderer(this.renderer);
            this.setContentView(glView);
            toastLong("Collecting Accelerometer data. 2 minutes remain.");
            startTime = System.nanoTime();
            sensorManager.registerListener(this,
                             accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,
                                  rotation, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (collecting.get()) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                AccelerationPoint pt =
                        new AccelerationPoint(event.values, System.nanoTime());

                if (pt.valid()) {
                    points.addPoint(pt);
                }


                renderer.getCube().translate(event.values[0] * -0.1f,
                                             event.values[1] * -0.1f,
                                             event.values[2] * -0.1f);

                if ((System.nanoTime() - startTime) > maxTicks) {
                    onCollectionFinished("Collection finished.", true);
                }
            } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                renderer.getCube().rotate(
                        Util.radToDeg((float)(event.values[0] * Math.PI)),
                        Util.radToDeg((float)(event.values[1] * Math.PI)),
                        Util.radToDeg((float)(event.values[2] * Math.PI)));
            }
        }
    }

    private void onCollectionFinished(String message, boolean pushResults) {
        collecting.set(false);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, rotation);
        toastLong(message);
        Bundle bundle = null;
        if (pushResults) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                points.writeAsBytes(out);

                out.close();

                bundle = new Bundle();
                bundle.putByteArray("points", out.toByteArray());

            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (collecting.get()) {
            onCollectionFinished("Collection cancelled.", false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    Log.d("AccuracyChanged", "High Accuracy");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    Log.d("AccuracyChanged", "Medium Accuracy");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    Log.d("AccuracyChanged", "Low Accuracy");
                    break;
            }
        }
    }
}
