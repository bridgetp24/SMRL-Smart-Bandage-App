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

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class BatteryActivity extends BaseActivity {

    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.value_textView) TextView mBattery;

    private static final String TAG = BatteryActivity.class.getSimpleName();

    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    UUID batteryUUID = SmartBandageApp.gattServices.get("battery");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        ButterKnife.bind(this);
        //get macAddress and bleDevice
        String macAddress = getIntent().getStringExtra(SensorListActivity.EXTRA_MAC_ADDRESS);
        bleDevice = RxBle.Instance.getClient().getBleDevice(macAddress);
        mConnectedDeviceNameTextView.setText("Device name: " + bleDevice.getName());
        connectionObservable = GattUtils.prepareConnectionObservable(bleDevice,disconnectTriggerSubject);
        // connect to battery Gatt and set text field to value
        GattUtils.connectToGatt(batteryUUID,mBattery,false,connectionObservable,compositeDisposable,bleDevice);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(GattUtils.isConnected(bleDevice)) {
            GattUtils.triggerDisconnect(disconnectTriggerSubject);
        }
    }
}
