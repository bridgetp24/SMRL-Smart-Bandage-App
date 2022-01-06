package com.test.smartbandage.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.reactivex.functions.Consumer;

/**
 * Helper class to allow showing Rx exceptions as Toasts
 */
public class ThrowableToastingAction implements Consumer<Throwable> {

    private static final String TAG = ThrowableToastingAction.class.getSimpleName();

    private final Context context;

    public ThrowableToastingAction(Context context) {
        this.context = context;
    }

    @Override
    public void accept(Throwable throwable) {
        Log.e(TAG, "RxError: ", throwable);
        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
    }
}
