package com.test.smartbandage.bluetooth.connection;


import android.content.BroadcastReceiver;
import android.content.Intent;

import com.polidea.rxandroidble2.RxBleDevice;
import com.test.smartbandage.BasePresenter;
import com.test.smartbandage.BaseView;

public interface MovesenseContract {

    interface Presenter extends BasePresenter {
        void startScanning();

        void stopScanning();

        void onBluetoothResult(int requestCode, int resultCode, Intent data);
    }

    interface View extends BaseView<Presenter> {
        void displayScanResult(RxBleDevice bluetoothDevice, int rssi);

        void displayErrorMessage(String message);

        void registerReceiver(BroadcastReceiver broadcastReceiver);

        void unregisterReceiver(BroadcastReceiver broadcastReceiver);

        boolean checkLocationPermissionIsGranted();
    }
}
