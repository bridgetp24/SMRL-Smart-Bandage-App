package com.test.smartbandage.sensors;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.jjoe64.graphview.series.DataPoint;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.test.smartbandage.BaseActivity;
import com.test.smartbandage.R;
import com.test.smartbandage.SmartBandageApp;
import com.test.smartbandage.bluetooth.ConnectionLostDialog;
import com.test.smartbandage.bluetooth.RxBle;
import com.test.smartbandage.data_manager.CsvLogger;
import com.test.smartbandage.model.EcgModel;
import com.test.smartbandage.model.HeartRate;
import com.test.smartbandage.utils.HexString;
import com.test.smartbandage.utils.Util;

import com.test.smartbandage.bluetooth.ConnectionLostDialog;
import com.test.smartbandage.data_manager.CsvLogger;
import com.test.smartbandage.model.InfoResponse;
import com.test.smartbandage.model.LinearAcceleration;
import com.test.smartbandage.utils.FormatHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class LinearAccelerationTestActivity extends BaseActivity {

    private final String LOG_TAG = "LinearDebug";
    private final String LINEAR_ACCELERATION_PATH = "Meas/Acc/";
    private final String LINEAR_INFO_PATH = "/Meas/Acc/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final List<String> spinnerOptions = new ArrayList<>();
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String rate;
    UUID accelXUUID = SmartBandageApp.gattServices.get("accelX");
    UUID accelYUUID = SmartBandageApp.gattServices.get("accelY");
    UUID accelZUUID = SmartBandageApp.gattServices.get("accelZ");
    UUID serviceUUID = UUID.fromString("0000181a-0000-1000-8000-00805f9b34fb");
    @BindView(R.id.switchSubscription)
    SwitchCompat switchSubscription;
    @BindView(R.id.spinner) Spinner spinner;
    @BindView(R.id.x_axis_textView) TextView xAxisTextView;
    @BindView(R.id.y_axis_textView) TextView yAxisTextView;
    @BindView(R.id.z_axis_textView) TextView zAxisTextView;
    @BindView(R.id.connected_device_name_textView) TextView mConnectedDeviceNameTextView;
    @BindView(R.id.connected_device_swVersion_textView) TextView mConnectedDeviceSwVersionTextView;
    @BindView(R.id.linearAcc_lineChart) LineChart mChart;

    private AlertDialog alertDialog;
    private CsvLogger mCsvLogger;
    LineData mLineData;
    private boolean isLogSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_acceleration_test);
        ButterKnife.bind(this);
        String macAddress = getIntent().getStringExtra(SensorListActivity.EXTRA_MAC_ADDRESS);
        bleDevice = RxBle.Instance.getClient().getBleDevice(macAddress);
        connectionObservable = GattUtils.prepareConnectionObservable(bleDevice,disconnectTriggerSubject);

        spinnerOptions.add("X");
        spinnerOptions.add("Y");
        spinnerOptions.add("Z");
//        spinnerOptions.add("All");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Linear Acceleration");
        }

        mCsvLogger = new CsvLogger();


        mConnectedDeviceNameTextView.setText("Device name: " + bleDevice.getName());
        xAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        yAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        zAxisTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Init Empty Chart
        mChart.setData(new LineData());
        mChart.getDescription().setText("Linear Acc");
        mChart.setTouchEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.invalidate();


        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, spinnerOptions);

        spinner.setAdapter(spinnerAdapter);
    }

    private void logLinAccelData(byte[] bytes) {
        // record data to csv
        int bytesInt = Util.byteArrayToInteger(bytes);
        Log.d(LOG_TAG, "logging " + bytesInt);
        String timestamp = String.format("%tFT%<tTZ.%<tL", Calendar.getInstance(TimeZone.getTimeZone("Z")));
        mCsvLogger.appendLine(String.format(Locale.getDefault(),
                "%s,%d", timestamp,
                bytesInt));
        // plot in UI
        int[] bytesIntArr = new int[3];
        bytesIntArr[0] = 0;
        bytesIntArr[1] = bytesInt;
        bytesIntArr[2] = 0;
        long timestampNum =  System.currentTimeMillis()/1000;

        // need timestamp, x, y, z
        updateData(timestampNum,bytesIntArr);
    }
    private void updateData(long timestamp, int[] arrayData) {

        if (arrayData != null) {
            xAxisTextView.setText(String.format(Locale.getDefault(),
                    "x: %d", arrayData[0]));
            yAxisTextView.setText(String.format(Locale.getDefault(),
                    "y: %d", arrayData[1]));
            zAxisTextView.setText(String.format(Locale.getDefault(),
                    "z: %d", arrayData[2]));

            //Log.e(LOG_TAG, "onNotification: timestamp: " + timestamp + " x: " + arrayData.x);
            Log.d(LOG_TAG, "onNotification: lineData.getEntryCount(): " + mLineData.getEntryCount());
            mLineData.addEntry(new Entry(mLineData.getEntryCount(), (float) arrayData[0]), 0);
            mLineData.addEntry(new Entry(mLineData.getEntryCount(), (float) arrayData[1]), 1);
            mLineData.addEntry(new Entry(mLineData.getEntryCount(), (float) arrayData[2]), 2);
            mLineData.notifyDataChanged();
            Log.d(LOG_TAG, "notifydatasetchanged");
            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(50);

            // move to the latest entry
            mChart.moveViewToX(timestamp / 100);
        }
    }
    private void onNotificationReceived(byte[] bytes) {
        Log.d(LOG_TAG,"Notification Received");
        Log.d(LOG_TAG,"Byte array length: " + bytes.length);
        logLinAccelData(bytes);
    }
    private void onNotificationReceivedX(byte[] bytes) {
        Log.d(LOG_TAG,"Notification Received from X!!");
        int bytesInt = logCSV(bytes,"X");
        // plot in UI
        int[] bytesIntArr = new int[3];
        bytesIntArr[0] = bytesInt;
        bytesIntArr[1] = 0;
        bytesIntArr[2] = 0;
        long timestampNum =  System.currentTimeMillis()/1000;

        // need timestamp, x, y, z
        displayData(timestampNum,bytesIntArr);

    }

    private void displayData( long timestamp, int[] arrayData){
        if (arrayData != null) {
            xAxisTextView.setText(String.format(Locale.getDefault(),
                    "x: %d", arrayData[0]));
            yAxisTextView.setText(String.format(Locale.getDefault(),
                    "y: %d", arrayData[1]));
            zAxisTextView.setText(String.format(Locale.getDefault(),
                    "z: %d", arrayData[2]));

            //Log.e(LOG_TAG, "onNotification: timestamp: " + timestamp + " x: " + arrayData.x);
            Log.d(LOG_TAG, "onNotification: lineData.getEntryCount(): " + mLineData.getEntryCount());
            mLineData.addEntry(new Entry(mLineData.getEntryCount(), (float) arrayData[0]), 0);
            mLineData.addEntry(new Entry(mLineData.getEntryCount(), (float) arrayData[1]), 1);
            mLineData.addEntry(new Entry(mLineData.getEntryCount(), (float) arrayData[2]), 2);
            mLineData.notifyDataChanged();
            Log.d(LOG_TAG, "notifydatasetchanged");
            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(50);

            // move to the latest entry
            mChart.moveViewToX(timestamp / 100);
        }
    }

    private void onNotificationReceivedY(byte[] bytes) {
        Log.d(LOG_TAG,"Notification Received from Y!!");
        int bytesInt = logCSV(bytes,"Y");

        // plot in UI
        int[] bytesIntArr = new int[3];
        bytesIntArr[0] = 0;
        bytesIntArr[1] = bytesInt;
        bytesIntArr[2] = 0;
        long timestampNum =  System.currentTimeMillis()/1000;

        // need timestamp, x, y, z
        displayData(timestampNum,bytesIntArr);
    }
    private void onNotificationReceivedZ(byte[] bytes) {
        Log.d(LOG_TAG,"Notification Received from Z!!");
        int bytesInt = logCSV(bytes,"Z");

        // plot in UI
        int[] bytesIntArr = new int[3];
        bytesIntArr[0] = 0;
        bytesIntArr[1] = 0;
        bytesIntArr[2] = bytesInt;
        long timestampNum =  System.currentTimeMillis()/1000;

        // need timestamp, x, y, z
        displayData(timestampNum,bytesIntArr);
    }
    private int logCSV(byte[] bytes, String label) {
        int bytesInt = Util.byteArrayToInteger(bytes);
        Log.d(LOG_TAG, "logging " + bytesInt + " from " + label);
        String timestamp = String.format("%tFT%<tTZ.%<tL", Calendar.getInstance(TimeZone.getTimeZone("Z")));
        mCsvLogger.appendLine(String.format(Locale.getDefault(),
                "%s,%s,%d", label,timestamp,
                bytesInt));

        return bytesInt;
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.d(LOG_TAG,"Notification Setup failed: " + throwable);
        Snackbar.make(xAxisTextView, "Notifications error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }
    private void connectX() {
        final Disposable connectionDisposableX = connectionObservable
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(accelXUUID))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            Log.d(LOG_TAG, "Characteristic X " + characteristic);
                            Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established! to x");
                            mCsvLogger.appendHeader("Type,Timestamp,Value");
                            if (GattUtils.isConnected(bleDevice)) {
                                final Disposable disposable = connectionObservable
                                        .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelXUUID))
                                        .flatMap(notificationObservable -> notificationObservable)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::onNotificationReceivedX, this::onNotificationSetupFailure);
                                compositeDisposable.add(disposable);
                            } else {
                                Log.d(LOG_TAG, "Device not connected");
                            }
                        },
                        GattUtils::onConnectionFailure,
                        GattUtils::onConnectionFinished
                );
        compositeDisposable.add(connectionDisposableX);

    }
    private void connectY() {
        final Disposable connectionDisposableY = connectionObservable
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(accelYUUID))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            Log.d(LOG_TAG, "Characteristic Y " + characteristic);
                            Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established! to y");
                            mCsvLogger.appendHeader("Timestamp,Count");
                            if (GattUtils.isConnected(bleDevice)) {
                                final Disposable disposable = connectionObservable
                                        .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelYUUID))
                                        .flatMap(notificationObservable -> notificationObservable)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::onNotificationReceivedY, this::onNotificationSetupFailure);
                                compositeDisposable.add(disposable);
                            } else {
                                Log.d(LOG_TAG, "Device not connected");
                            }
                        },
                        GattUtils::onConnectionFailure,
                        GattUtils::onConnectionFinished
                );
        compositeDisposable.add(connectionDisposableY);
    }
    private void connectZ() {
        final Disposable connectionDisposableZ = connectionObservable
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(accelZUUID))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            Log.d(LOG_TAG, "Characteristic Z " + characteristic);
                            Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established! to z");
                            mCsvLogger.appendHeader("Timestamp,Count");
                            if (GattUtils.isConnected(bleDevice)) {
                                final Disposable disposable = connectionObservable
                                        .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelZUUID))
                                        .flatMap(notificationObservable -> notificationObservable)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(this::onNotificationReceivedZ, this::onNotificationSetupFailure);
                                compositeDisposable.add(disposable);
                            } else {
                                Log.d(LOG_TAG, "Device not connected");
                            }
                        },
                        GattUtils::onConnectionFailure,
                        GattUtils::onConnectionFinished
                );
        compositeDisposable.add(connectionDisposableZ);
    }
    private void connectAll() {
        final Disposable connectionDisposable = connectionObservable
                .flatMap(connection -> connection.discoverServices()
                        .map(RxBleDeviceServices::getBluetoothGattServices)
                        .flatMapObservable(Observable::fromIterable) // map to individual services
                        .map(BluetoothGattService::getCharacteristics) // for each service take all characteristics)
                        .flatMap(Observable::fromIterable) // map to individual characteristic)
                        .filter(characteristic -> (BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) // consider only characteristics that have indicate or notify property

                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            if(characteristic.toString().equals("android.bluetooth.BluetoothGattCharacteristic@8d2b5d7")) {
                                Log.d(LOG_TAG, "Hey X was found");
                                Log.d(LOG_TAG, "Characteristic X " + characteristic);
                                Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established! to x");
                                mCsvLogger.appendHeader("Type,Timestamp,Value");
                                if (GattUtils.isConnected(bleDevice)) {
                                    final Disposable disposable = connectionObservable
                                            .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelXUUID))
                                            .flatMap(notificationObservable -> notificationObservable)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(this::onNotificationReceivedX, this::onNotificationSetupFailure);
                                    compositeDisposable.add(disposable);
                                } else {
                                    Log.d(LOG_TAG, "Device not connected");
                                }
                            }
                            if(characteristic.toString().equals("android.bluetooth.BluetoothGattCharacteristic@557df71")) {
                                Log.d(LOG_TAG, "Hey Y was found");
                                Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established! to y");
                                mCsvLogger.appendHeader("Timestamp,Count");
                                if (GattUtils.isConnected(bleDevice)) {
                                    final Disposable disposable = connectionObservable
                                            .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelYUUID))
                                            .flatMap(notificationObservable -> notificationObservable)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(this::onNotificationReceivedY, this::onNotificationSetupFailure);
                                    compositeDisposable.add(disposable);
                                } else {
                                    Log.d(LOG_TAG, "Device not connected");
                                }
                            }
                            if(characteristic.toString().equals("android.bluetooth.BluetoothGattCharacteristic@3ba01ad")) {
                                Log.d(LOG_TAG, "Characteristic Z " + characteristic);
                                Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established! to z");
                                mCsvLogger.appendHeader("Timestamp,Count");
                                if (GattUtils.isConnected(bleDevice)) {
                                    final Disposable disposable = connectionObservable
                                            .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelZUUID))
                                            .flatMap(notificationObservable -> notificationObservable)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(this::onNotificationReceivedZ, this::onNotificationSetupFailure);
                                    compositeDisposable.add(disposable);
                                }
                            }
                        },
                        GattUtils::onConnectionFailure,
                        GattUtils::onConnectionFinished
                );

        compositeDisposable.add(connectionDisposable);
    }
    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            disableSpinner();
            isLogSaved = false;
            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);
            mLineData = mChart.getData();

            ILineDataSet xSet = mLineData.getDataSetByIndex(0);
            ILineDataSet ySet = mLineData.getDataSetByIndex(1);
            ILineDataSet zSet = mLineData.getDataSetByIndex(2);

            if (xSet == null) {
                xSet = createSet("Data x", getResources().getColor(android.R.color.holo_red_dark));
                ySet = createSet("Data y", getResources().getColor(android.R.color.holo_green_dark));
                zSet = createSet("Data z", getResources().getColor(android.R.color.holo_blue_dark));
                mLineData.addDataSet(xSet);
                mLineData.addDataSet(ySet);
                mLineData.addDataSet(zSet);
            }

            // connect based off of selected
            String selected = (String) spinner.getSelectedItem();
            Log.d(LOG_TAG, "Selected: " + selected);
            if (selected.equals("X")) {
                Log.d(LOG_TAG, "Connect X");
                connectX();
            } else if (selected.equals("Y")) {
                Log.d(LOG_TAG, "Connect Y");
                connectY();
            } else if (selected.equals("Z")) {
                Log.d(LOG_TAG, "Connect Z");
                connectZ();
            }else {
                Log.d(LOG_TAG, "Connect All");
                connectAll();
            }

        } else {
            unSubscribe();
            enableSpinner();
        }
    }


    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rate = spinnerOptions.get(position);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribe();

    }

    private void unSubscribe() {
        // unsubscribe to bluetooth sensor
        GattUtils.triggerDisconnect(disconnectTriggerSubject);
        // save logs and upload
        mCsvLogger.finishSavingLogs(this, "LinearAcceleration",bleDevice.getName());
    }

    private void disableSpinner() {
        spinner.setEnabled(false);
    }

    private void enableSpinner() {
        spinner.setEnabled(true);
    }

    private LineDataSet createSet(String name, int color) {
        LineDataSet set = new LineDataSet(null, name);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(0f);

        return set;
    }
}
