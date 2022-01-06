package com.test.smartbandage.sensors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movesense.mds.internal.connectivity.BleManager;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.polidea.rxandroidble2.RxBleDevice;
import com.test.smartbandage.BaseActivity;
import com.test.smartbandage.MainViewActivity;
import com.test.smartbandage.R;
import com.test.smartbandage.bluetooth.RxBle;


import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class SensorListActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.sensorList_recyclerView) RecyclerView mSensorListRecyclerView;
    @BindView(R.id.sensorList_deviceInfo_title_tv) TextView mSensorListDeviceInfoTitleTv;


    private CompositeDisposable subscriptions;

    public static final String EXTRA_MAC_ADDRESS = "extra_mac_address";
    private String macAddress;
    private RxBleDevice bleDevice;
    private final String TAG = SensorListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);
        ButterKnife.bind(this);
        macAddress = getIntent().getStringExtra(EXTRA_MAC_ADDRESS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sensors List");
        }
        bleDevice = RxBle.Instance.getClient().getBleDevice(macAddress);
        mSensorListDeviceInfoTitleTv.setText(mSensorListDeviceInfoTitleTv.getText() + ": " + bleDevice.getName());
        subscriptions = new CompositeDisposable();

        ArrayList<SensorListItemModel> sensorListItemModels = new ArrayList<>();

        sensorListItemModels.add(new SensorListItemModel(getString(R.string.battery_energy)));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.ecg)));
        sensorListItemModels.add(new SensorListItemModel(getString(R.string.deviceInfo)));


        SensorsListAdapter sensorsListAdapter = new SensorsListAdapter(sensorListItemModels, this);
        mSensorListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSensorListRecyclerView.setAdapter(sensorsListAdapter);

        sensorsListAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_device_settings:
                //startActivity(new Intent(SensorListActivity.this, DeviceSettingsActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        subscriptions.clear();
    }

    @Override
    public void onClick(View v) {
        String sensorName = (String) v.getTag();

        subscriptions.clear();

        if (getString(R.string.battery_energy).equals(sensorName)) {
            final Intent intent = new Intent(this, BatteryActivity.class);
            intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
            startActivity(intent);
        } else if (getString(R.string.ecg).equals(sensorName)) {
            final Intent intent = new Intent(SensorListActivity.this, EcgActivityGraphView.class);
            intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
            startActivity(intent);
        } else if (getString(R.string.deviceInfo).equals(sensorName)) {
            final Intent intent = new Intent(this, DeviceInformationActivity.class);
            intent.putExtra(EXTRA_MAC_ADDRESS, macAddress);
            startActivity(intent);
        }
}

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, com.test.smartbandage.data_manager.DataManagerActivity.class));
    }
}