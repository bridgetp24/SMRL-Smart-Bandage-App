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
            user = new User();
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
        // notable
        // 7B6A092E-5FC8-BCBB-C58B-03BE82A8DD16
        // init map of gatt characteristics
        gattServices.put("deviceInfo",UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"));
        gattServices.put("manufacturerName",UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"));
        gattServices.put("systemID",UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb"));
        gattServices.put("battery",UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));
        gattServices.put("ECG",UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
        //
        // these are in smart-bandage prototype
        gattServices.put("bodySensorLocation",UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb"));
        gattServices.put("heartRateControlPoint",UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"));

        // still need to be implemented currently using ECG UUID as placeholder
        gattServices.put("heartRate",UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));
        //wind speed
        gattServices.put("accelX",UUID.fromString("00002a72-0000-1000-8000-00805f9b34fb"));
        // dew point
        gattServices.put("accelY",UUID.fromString("00002a7b-0000-1000-8000-00805f9b34fb"));
        //elevation
        gattServices.put("accelZ",UUID.fromString("00002a6c-0000-1000-8000-00805f9b34fb"));
        // time formated year, month, day hour, minute, second
        gattServices.put("time",UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"));

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


