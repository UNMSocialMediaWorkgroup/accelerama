package com.lunagameserve.accelerama.activities.util;

import android.app.Activity;

/**
 * Created by Ross on 2/27/2015.
 */
public class UIActivity extends Activity {
    protected Runnable makeUIRunnable(final Runnable r) {
        return new Runnable() {
            @Override
            public void run() {
                runOnUiThread(r);
            }
        };
    }
}
