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
import com.lunagameserve.compression.ByteWriter;
import com.lunagameserve.gl.CubeRenderer;
import com.lunagameserve.gl.geometry.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main activity for Accelerama, which gathers Accelerometer data
 * then packages it as a result and pushes into a
 * {@link com.lunagameserve.accelerama.activities.ResultsActivity}
 * for viewing.
 *
 * @author Six
 * @since March 2, 2015
 */
public class CollectionActivity extends ToastActivity
                                implements SensorEventListener{
    /**
     * The {@link com.lunagameserve.gl.CubeRenderer} used to render the
     * {@link com.lunagameserve.gl.geometry.Cube} onto a
     * {@link android.opengl.GLSurfaceView}.
     */
    private CubeRenderer renderer;

    /**
     * The {@link android.hardware.SensorManager} which is currently running
     * on the target device.
     */
    private SensorManager sensorManager;

    /**
     * The {@link android.hardware.Sensor} representing the device's linear
     * acceleration gatherer.
     */
    private Sensor accelerometer;

    /**
     * The {@link android.hardware.Sensor} representing a hardware sensor which
     * collects orientation information about the device.
     */
    private Sensor rotation;

    private Sensor lightSensor;

    private ArrayList<Float> lightPoints = new ArrayList<Float>();

    /**
     * A thread-safe flag which describes the current status of the
     * {@link android.hardware.Sensor}s in this
     * {@link com.lunagameserve.accelerama.activities.CollectionActivity}.
     */
    private AtomicBoolean collecting = new AtomicBoolean(true);

    /**
     * The device time, in nanoseconds, that this
     * {@link com.lunagameserve.accelerama.activities.CollectionActivity}
     * began collecting information.
     */
    private long startTime = 0L;

    /**
     * The number of seconds which this
     * {@link com.lunagameserve.accelerama.activities.CollectionActivity}
     * will spend collecting data from the device.
     */
    public static final int SECONDS = 5;

    /**
     * A collection of accelerometer points which will be added to while
     * this device is collecting, and pushed to a
     * {@link com.lunagameserve.accelerama.activities.ResultsActivity}
     * when collection is complete
     */
    private AccelerationCollection points = new AccelerationCollection();

    private final boolean COLLECT_ACCELERATION = false;

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sensorManager == null) {
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            rotation =
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            accelerometer =
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            lightSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            GLSurfaceView glView = new GLSurfaceView(this);
            this.renderer = new CubeRenderer();
            glView.setRenderer(this.renderer);
            this.setContentView(glView);
            toastLong("Collecting Accelerometer data. 2 minutes remain.");
            startTime = System.nanoTime();
            sensorManager.registerListener(this,
                             accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,
                                  rotation, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,
                               lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (collecting.get()) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                renderer.getCube().translate(event.values[0] * -0.1f,
                                             event.values[1] * -0.1f,
                                             event.values[2] * -0.1f);

                if (COLLECT_ACCELERATION) {
                    AccelerationPoint pt =
                            new AccelerationPoint(event.values,
                                    System.nanoTime());

                    if (pt.valid()) {
                        points.addPoint(pt);
                    }
                }

                long maxTicks = 1000000000L * SECONDS;
                if ((System.nanoTime() - startTime) > maxTicks) {
                    onCollectionFinished("Collection finished.", true);
                }
            } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

                float[] eulers = Util.quaternionToEuclidean(event.values);

                renderer.getCube().rotate(
                        Util.radToDeg((float)(eulers[0] * Math.PI)),
                        Util.radToDeg((float)(eulers[1] * Math.PI)),
                        Util.radToDeg((float)(eulers[2] * Math.PI)));

                if (!COLLECT_ACCELERATION) {
                    AccelerationPoint pt =
                            new AccelerationPoint(eulers,
                                                  System.nanoTime());

                    if (pt.valid()) {
                        points.addPoint(pt);
                    }
                }
            } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                lightPoints.add(event.values[0]);
            }
        }
    }

    /**
     * To be called when this
     * {@link com.lunagameserve.accelerama.activities.CollectionActivity}
     * is finished collecting data for any reason.
     *
     * @param message The message to {@link android.widget.Toast} to the
     *                user on calling this method.
     *
     * @param pushResults If {@code true}, will create a
     *          {@link com.lunagameserve.accelerama.activities.ResultsActivity},
     *                    and send it the gathered data, and start it.
     */
    private void onCollectionFinished(String message, boolean pushResults) {
        collecting.set(false);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, rotation);
        toastLong(message);
        Bundle bundle = null;
        if (pushResults) {
            try {
                /* Write acceleration points to bundle */
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                points.writeAsBytes(out);

                out.close();
                byte[] accelBytes = out.toByteArray();

                /* Write light points to bundle */
                out = new ByteArrayOutputStream();
                ByteWriter writer = new ByteWriter(out);
                for (Float lightPoint : lightPoints) {
                    writer.writeFloat(lightPoint);
                }
                writer.close();

                bundle = new Bundle();
                bundle.putByteArray("lightPoints", out.toByteArray());
                bundle.putByteArray("points", accelBytes);
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

    /** {@inheritDoc} */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (collecting.get()) {
            onCollectionFinished("Collection cancelled.", false);
        }
    }

    /** {@inheritDoc} */
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
