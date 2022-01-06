package com.test.smartbandage.sensors;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.test.smartbandage.BaseActivity;
import com.test.smartbandage.R;
import com.test.smartbandage.SmartBandageApp;
import com.test.smartbandage.bluetooth.RxBle;
import com.test.smartbandage.utils.HexString;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class DeviceInformationActivity extends BaseActivity {
    @BindView(R.id.connected_device_name_textView) TextView connectedDeviceName;
    @BindView(R.id.deviceInfo_manufacturerName) TextView manufacturerName;
    @BindView(R.id.deviceInfo_systemID) TextView systemID;
    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    UUID manufacturerNameUUID = SmartBandageApp.gattServices.get("manufacturerName");
    UUID systemIDUUID = SmartBandageApp.gattServices.get("systemID");
    private String TAG = "DeviceInformationDebug";
    private static String TAGStatic = "DeviceInformationDebug";
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    static ArrayList<byte[]> values = new ArrayList<byte[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        ButterKnife.bind(this);
        connectedDeviceName.setText("Device name:");
        manufacturerName.setText("Manufacturer name:");
        systemID.setText("System ID:");
        String macAddress = getIntent().getStringExtra(SensorListActivity.EXTRA_MAC_ADDRESS);
        Log.d(TAG, "macAddress:" + macAddress);
        bleDevice = RxBle.Instance.getClient().getBleDevice(macAddress);
        connectedDeviceName.setText("Device name: " + bleDevice.getName());
        Log.d(TAG, "bleDevice" + bleDevice.toString());
        connectionObservable = GattUtils.prepareConnectionObservable(bleDevice,disconnectTriggerSubject);
        // get GATT fields and set text
        GattUtils.connectToGatt(manufacturerNameUUID,manufacturerName,true,connectionObservable,compositeDisposable,bleDevice);
        GattUtils.connectToGatt(systemIDUUID,systemID,false,connectionObservable,compositeDisposable,bleDevice);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(GattUtils.isConnected(bleDevice)) {
            GattUtils.triggerDisconnect(disconnectTriggerSubject);
        }
    }
}
