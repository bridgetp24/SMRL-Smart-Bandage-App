package com.test.smartbandage.sensors;


import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
    private final List<String> spinnerRates = new ArrayList<>();
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String rate;
    UUID accelXUUID = SmartBandageApp.gattServices.get("accelX");

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
    private boolean isLogSaved = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_acceleration_test);
        ButterKnife.bind(this);
        String macAddress = getIntent().getStringExtra(SensorListActivity.EXTRA_MAC_ADDRESS);
        bleDevice = RxBle.Instance.getClient().getBleDevice(macAddress);
        connectionObservable = GattUtils.prepareConnectionObservable(bleDevice,disconnectTriggerSubject);



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
                android.R.layout.simple_dropdown_item_1line, spinnerRates);

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
        int[] bytesIntArr = new int[0];
        long timestampNum = 0;
        // need timestamp, x, y, z
        //updateData(timestampNum,bytesIntArr);
    }
    private void updateData(long timestamp, int[] arrayData) {
        LineData mLineData = mChart.getData();
        if (arrayData != null) {
//
//            mCsvLogger.appendHeader("Timestamp (ms),X: (m/s^2),Y: (m/s^2),Z: (m/s^2)");
//
//            mCsvLogger.appendLine(String.format(Locale.getDefault(),
//                    "%d,%.6f,%.6f,%.6f, ", timestamp,
//                    arrayData[0], arrayData[1], arrayData[2]));

            xAxisTextView.setText(String.format(Locale.getDefault(),
                    "x: %.6f", arrayData[0]));
            yAxisTextView.setText(String.format(Locale.getDefault(),
                    "y: %.6f", arrayData[1]));
            zAxisTextView.setText(String.format(Locale.getDefault(),
                    "z: %.6f", arrayData[2]));

            //Log.e(LOG_TAG, "onNotification: timestamp: " + timestamp + " x: " + arrayData.x);
            Log.e(LOG_TAG, "onNotification: lineData.getEntryCount(): " + mLineData.getEntryCount());
            mLineData.addEntry(new Entry(timestamp / 100, (float) arrayData[0]), 0);
            mLineData.addEntry(new Entry(timestamp / 100, (float) arrayData[1]), 1);
            mLineData.addEntry(new Entry(timestamp / 100, (float) arrayData[2]), 2);
            mLineData.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(50);

            // move to the latest entry
            mChart.moveViewToX(timestamp / 100);

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
    }
    }
    private void onNotificationReceived(byte[] bytes) {
        Log.d(LOG_TAG,"Notification Received");
        Log.d(LOG_TAG,"Byte array length: " + bytes.length);
        logLinAccelData(bytes);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.d(LOG_TAG,"Notification Setup failed: " + throwable);
        Snackbar.make(xAxisTextView, "Notifications error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }
    @OnCheckedChanged(R.id.switchSubscription)
    public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            disableSpinner();

            isLogSaved = false;

            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);



           // connect to device
            final Disposable connectionDisposable = connectionObservable
                    .flatMapSingle(RxBleConnection::discoverServices)
                    .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(accelXUUID))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            characteristic -> {
                                Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established!");
                                mCsvLogger.appendHeader("Timestamp,Count");
                                if (GattUtils.isConnected(bleDevice)) {
                                    final Disposable disposable = connectionObservable
                                            .flatMap(rxBleConnection -> rxBleConnection.setupNotification(accelXUUID))
                                            .flatMap(notificationObservable -> notificationObservable)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
                                    compositeDisposable.add(disposable);
                                } else {
                                    Log.d(LOG_TAG, "Device not connected");
                                }
                            },
                            GattUtils::onConnectionFailure,
                            GattUtils::onConnectionFinished
                    );
            compositeDisposable.add(connectionDisposable);
        } else {
            unSubscribe();
            enableSpinner();
        }
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rate = spinnerRates.get(position);
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
