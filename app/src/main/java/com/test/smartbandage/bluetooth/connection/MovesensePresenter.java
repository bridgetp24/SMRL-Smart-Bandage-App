package com.test.smartbandage.bluetooth.connection;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleScanResult;
import com.test.smartbandage.bluetooth.RxBle;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MovesensePresenter implements MovesenseContract.Presenter {

    private RxBleClient rxBleClient;
    private String TAG = "MSPresenter";

    private MovesenseContract.View mView;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<RxBleDevice> mMovesenseModelArrayList;
    private CompositeDisposable mCompositeSubscription;

    public MovesensePresenter(MovesenseContract.View view, BluetoothManager bluetoothManager) {
        mView = view;
        mView.setPresenter(this);

        mBluetoothManager = bluetoothManager;
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mMovesenseModelArrayList = new ArrayList<>();

        mCompositeSubscription = new CompositeDisposable();

        rxBleClient = RxBle.Instance.getClient();
    }

    @Override
    public void startScanning() {
        Log.d(TAG, "startScanning()");

        if (!mView.checkLocationPermissionIsGranted()) {
            mView.displayErrorMessage("Location Permission is required");
            return;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "startScanning: BT not available. Turning ON...");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable so run
                mBluetoothAdapter.enable();
            }
        } else {
            Log.d(TAG, "startScanning() startLeScan");

            mCompositeSubscription.add(rxBleClient.scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<RxBleScanResult>() {
                        @Override
                        public void accept(RxBleScanResult rxBleScanResult) {
                            Log.d(TAG, "call: Scan result() " + rxBleScanResult.getBleDevice().getName());
                            RxBleDevice rxBleDevice = rxBleScanResult.getBleDevice();

                            if (rxBleDevice.getName() != null && rxBleDevice.getName().contains("Movesense")
                                    && !mMovesenseModelArrayList.contains(rxBleDevice)) {

                                Log.d(TAG, "call: Add to list " + rxBleScanResult.getBleDevice().getName());
                                mMovesenseModelArrayList.add(rxBleDevice);
                                mView.displayScanResult(rxBleDevice, rxBleScanResult.getRssi());
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Log.e(TAG, "call: " + throwable);
                        }
                    }));
        }
    }

    @Override
    public void stopScanning() {
        mCompositeSubscription.dispose();
    }

    @Override
    public void onBluetoothResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onBluetoothResult: requestCode: " + requestCode + " resultCode: " + resultCode);
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    // The user bluetooth is ready to use.

                    // start scanning again in case of ready Bluetooth
                    startScanning();
                    return;
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    return;
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    return;
                }

            }
        }
    };

    @Override
    public void onCreate() {
        mView.registerReceiver(btReceiver);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {
        stopScanning();
    }

    @Override
    public void onDestroy() {
        mView.unregisterReceiver(btReceiver);
        mCompositeSubscription.dispose();
    }
}
