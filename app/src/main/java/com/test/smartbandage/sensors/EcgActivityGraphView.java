package com.test.smartbandage.sensors;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.movesense.mds.internal.connectivity.BleManager;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class EcgActivityGraphView extends BaseActivity implements BleManager.IBleConnectionMonitor {

    @BindView(R.id.switchSubscription)
    SwitchCompat mSwitchSubscription;
    @BindView(R.id.ecg_lineChart)
    GraphView mGraphView;
    @BindView(R.id.connected_device_name_textView)
    TextView mConnectedDeviceNameTextView;
    @BindView(R.id.spinner)
    Spinner mSpinner;
    @BindView(R.id.ecg_spinnerText)
    TextView mEcgSpinnerText;
    @BindView(R.id.ecg_switchContainer)
    LinearLayout mEcgSwitchContainer;

    private static final String TAG = "ECGDebug";

    private final int MS_IN_SECOND = 1000;

    @BindView(R.id.heart_rate_textView)
    TextView mHeartRateTextView;
    @BindView(R.id.rr_textView)
    TextView mRrTextView;

    private CsvLogger mCsvLogger;
    private final List<Integer> spinnerRates = new ArrayList<>();
    private AlertDialog alertDialog;
    private LineGraphSeries<DataPoint> mSeriesECG;
    private int mDataPointsAppended;

    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    UUID heartRateUUID = SmartBandageApp.gattServices.get("heartRate");
   // UUID heartRateUUID = SmartBandageApp.gattServices.get("movesenseHRUUID");
    UUID bodySensorLocationUUID = SmartBandageApp.gattServices.get("bodySensorLocation");
    UUID heartRateControlPointUUID = SmartBandageApp.gattServices.get("heartRateControlPoint");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        ButterKnife.bind(this);

        mCsvLogger = new CsvLogger();
        String macAddress = getIntent().getStringExtra(SensorListActivity.EXTRA_MAC_ADDRESS);
        bleDevice = RxBle.Instance.getClient().getBleDevice(macAddress);
        mConnectedDeviceNameTextView.setText("Device name: " + bleDevice.getName());
        connectionObservable = GattUtils.prepareConnectionObservable(bleDevice,disconnectTriggerSubject);
       // GattUtils.connectToGatt(heartRateUUID,mHeartRateTextView,false,connectionObservable,compositeDisposable,bleDevice);

        // Init Empty Chart
        mSeriesECG = new LineGraphSeries<DataPoint>();
        mGraphView.addSeries(mSeriesECG);
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(500);

        mGraphView.getViewport().setYAxisBoundsManual(true);
//        mGraphView.getViewport().setMinY(3000000);
//        mGraphView.getViewport().setMaxY(5000000);
        mGraphView.getViewport().setMinY(0);
        mGraphView.getViewport().setMaxY(5000);
        mGraphView.getViewport().setScrollable(false);
        mGraphView.getViewport().setScrollableY(false);

        mGraphView.setTitleColor(Color.WHITE);
        mGraphView.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        mGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setVerticalLabelsVisible(false);
        mGraphView.getGridLabelRenderer().setHighlightZeroLines(false);
        mSeriesECG.setColor(Color.WHITE);


        final ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_dropdown_item_1line, spinnerRates);

        mSpinner.setAdapter(spinnerAdapter);

    }

    @OnClick(R.id.ecg_changeScreenOrientation)
    public void onScreenOrientationChangeClick() {

        int orientation = getResources().getConfiguration().orientation;

        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                mConnectedDeviceNameTextView.setVisibility(View.GONE);
                mEcgSpinnerText.setVisibility(View.GONE);
                mSpinner.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mEcgSwitchContainer.getLayoutParams();
                p.setMargins(0, 0, 0, 0);
                mEcgSwitchContainer.requestLayout();
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                mConnectedDeviceNameTextView.setVisibility(View.VISIBLE);
                mEcgSpinnerText.setVisibility(View.VISIBLE);
                mSpinner.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams p1 = (ViewGroup.MarginLayoutParams) mEcgSwitchContainer.getLayoutParams();
                p1.setMargins(10, 20, 0, 0); // restore margins from default xml values
                mEcgSwitchContainer.requestLayout();
                break;
        }
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
    }

    public void readGattCharacteristic(UUID characteristicUUID,int numSamples) {
        // connect to service
        final Disposable connectionDisposable = connectionObservable
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(characteristicUUID))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established!");
                            mCsvLogger.appendHeader("Timestamp,Count");
                            // reads 100 samples of the gatt characteristic
                            for(int i = 0; i < numSamples; i++) {
                                if (GattUtils.isConnected(bleDevice)) {
                                    final Disposable disposable = connectionObservable
                                            .firstOrError()
                                            .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(characteristicUUID))
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bytes -> {
                                                logECGData(bytes);
                                            }, GattUtils::onConnectionFailure);


                                    compositeDisposable.add(disposable);
                                } else {
                                    Log.d(TAG, "Device not connected");
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
            mDataPointsAppended = 0;
            mCsvLogger.checkRuntimeWriteExternalStoragePermission(this, this);
            int width = 128 * 3;
            mGraphView.getViewport().setMaxX(width);
            mSeriesECG.resetData(new DataPoint[0]);
            // connect to service
            final Disposable connectionDisposable = connectionObservable
                        .flatMapSingle(RxBleConnection::discoverServices)
                        .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(heartRateUUID))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                characteristic -> {
                                    Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established!");
                                    mCsvLogger.appendHeader("Timestamp,Count");
                                    if (GattUtils.isConnected(bleDevice)) {
                                        final Disposable disposable = connectionObservable
                                                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(heartRateUUID))
                                                .flatMap(notificationObservable -> notificationObservable)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);

                                        compositeDisposable.add(disposable);
                                    } else {
                                        Log.d(TAG, "Device not connected");
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
    private void onNotificationReceived(byte[] bytes) {
        //noinspection ConstantConditions
        logECGData(bytes);
        Snackbar.make(findViewById(R.id.heart_rate_textView), "Change: " + HexString.bytesToHex(bytes), Snackbar.LENGTH_SHORT).show();
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Snackbar.make(findViewById(R.id.heart_rate_textView), "Notifications error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }
    private void logECGData(byte[] bytes) {
        int width = 128 * 3;
        int bytesInt = Util.byteArrayToInteger(bytes);
        Log.d(TAG, "logging " + bytesInt);
        String timestamp = String.format("%tFT%<tTZ.%<tL", Calendar.getInstance(TimeZone.getTimeZone("Z")));
        mCsvLogger.appendLine(String.format(Locale.getDefault(),
                "%s,%d", timestamp,
                bytesInt));
        bytesInt = bytesInt - 4200000;
        mSeriesECG.appendData(new DataPoint(mDataPointsAppended,bytesInt),false, width);
        mDataPointsAppended++;
        if (mDataPointsAppended == 400) {
            mDataPointsAppended = 0;
            mSeriesECG.resetData(new DataPoint[0]);
        }
    }

    private void disableSpinner() {
        mSpinner.setEnabled(false);
    }

    private void enableSpinner() {
        mSpinner.setEnabled(true);
    }

    private void unSubscribe() {
        // unsubscribe to bluetooth sensor
        GattUtils.triggerDisconnect(disconnectTriggerSubject);
        // save logs and upload
        mCsvLogger.finishSavingLogs(this, "ECG",bleDevice.getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(GattUtils.isConnected(bleDevice)) {
            GattUtils.triggerDisconnect(disconnectTriggerSubject);
        }
    }

    @Override
    public void onDisconnect(String s) {
        Log.d(TAG, "onDisconnect: " + s);
        if (!isFinishing()) {
            runOnUiThread(() -> ConnectionLostDialog.INSTANCE.showDialog(com.test.smartbandage.sensors.EcgActivityGraphView.this));
        }
    }

    @Override
    public void onConnect(RxBleDevice rxBleDevice) {
        Log.e(TAG, "onConnect: " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        ConnectionLostDialog.INSTANCE.dismissDialog();
    }
    @Override
    public void onConnectError(String s, Throwable throwable) {
        Toast.makeText(com.test.smartbandage.sensors.EcgActivityGraphView.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }
    public void onHeartRateNotification(String data) {
        Log.e(TAG, "Heart rate onNotification() : " + data);
        //TODO: Figure out how to get data
        HeartRate heartRate = new Gson().fromJson(data, HeartRate.class);

        if (heartRate != null) {

            mHeartRateTextView.setText(String.format(Locale.getDefault(),
                    "Heart rate: %.0f [bpm]", (60.0 / heartRate.body.rrData[0]) * 1000));

            mRrTextView.setText(String.format(Locale.getDefault(),
                    "Beat interval: %d [ms]", heartRate.body.rrData[0]));
        }
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
