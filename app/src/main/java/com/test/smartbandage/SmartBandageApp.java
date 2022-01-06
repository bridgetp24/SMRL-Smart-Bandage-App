package com.test.smartbandage;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.polidea.rxandroidble2.RxBleClient;
import com.test.smartbandage.bluetooth.RxBle;
import com.test.smartbandage.data_manager.User;
import com.test.smartbandage.utils.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

public class SmartBandageApp extends Application{
    private RxBleClient rxBleClient;
    //UUIDs
    // Map containing ble services, UUID
    public static HashMap<String, UUID> gattServices = new HashMap<String,UUID>();
    /**
     * User field that stores instance of firebase app as user id
     */
    private static User user;
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize RxBleWrapper
        Log.d("SmartBandageApp", "Running initialize RxBle");
        RxBle.Instance.initialize(this);
        // init gatt UUIDS
        initGattUUIDs();
        // Copy necessary configuration file to proper place
        copyRawResourceToFile(R.raw.kompostisettings, "KompostiSettings.xml");
        FirebaseApp.initializeApp(this);
        if (user == null) {
           // user = new User();
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
    public static RxBleClient getRxBleClient(Context context) {
        SmartBandageApp application = (SmartBandageApp)context.getApplicationContext();
        return application.rxBleClient;
    }
    private void initGattUUIDs() {
        // init map of gatt characteristics
        gattServices.put("deviceInfo",UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"));
        gattServices.put("manufacturerName",UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"));
        gattServices.put("systemID",UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb"));
        gattServices.put("battery",UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));
        gattServices.put("heartRate",UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
        gattServices.put("bodySensorLocation",UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb"));
        gattServices.put("heartRateControlPoint",UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"));
        //gattServices.put("movesenseHRUUID",UUID.fromString("209dB857-c990-7a3b-356b-8aD0eebf8c54"));
        gattServices.put("movesenseHRUUID",UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
    }
    public static User getUser() {
        return user;
    }
    /**
     * Copy raw resource file to file.
     *
     * @param resourceId Resource id.
     * @param fileName   Target file name.
     */
    private void copyRawResourceToFile(int resourceId, String fileName) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = getResources().openRawResource(resourceId);
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not copy configuration file to: " + fileName);
        } finally {
            Util.safeClose(out);
            Util.safeClose(in);
        }
    }

    public void setUser(User user) {
        this.user = user;
    }
}
