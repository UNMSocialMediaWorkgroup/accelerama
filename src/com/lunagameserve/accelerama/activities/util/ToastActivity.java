package com.lunagameserve.accelerama.activities.util;

import android.widget.Toast;

/**
 * Created by Ross on 2/27/2015.
 */
public class ToastActivity extends UIActivity {

    protected void toastShort(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void toastLong(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }
}
