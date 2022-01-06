package com.test.smartbandage;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.test.smartbandage.bluetooth.BluetoothStatusMonitor;
import com.test.smartbandage.bluetooth.RxBle;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = "BaseActivityDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        BluetoothStatusMonitor.INSTANCE.bluetoothStatusSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == BluetoothAdapter.STATE_ON) {
                        Log.d(TAG, "call: BluetoothAdapter.STATE_ON");

                        RxBle.Instance.getClient().scanBleDevices()
                                .takeUntil(Observable.timer(5, TimeUnit.SECONDS))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(rxBleScanResult -> Log.d(TAG, "scan: "),
                                        throwable -> Log.e(TAG, "scanBleDevices() error ", throwable));
                    } else if (integer == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "call: BluetoothAdapter.STATE_OFF");
                    }
                }, throwable -> Log.e(TAG, "call bluetoothStatusSubject: ", throwable));


    }
}
