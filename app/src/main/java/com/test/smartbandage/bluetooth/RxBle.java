package com.test.smartbandage.bluetooth;

import android.content.Context;
import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;

/**
 * Singleton wrapper for RxBleClient
 */
public enum RxBle {
    Instance;

    private RxBleClient client;

    public void initialize(Context context) {
        Log.d("BLEDebug", "client initialized");
        client = RxBleClient.create(context);
    }

    public RxBleClient getClient()
    {
        Log.d("BLEDebug" ,"getClient" + client);
        return client;
    }
}
