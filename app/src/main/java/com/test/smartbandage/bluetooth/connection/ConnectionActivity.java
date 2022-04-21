package com.test.smartbandage.bluetooth.connection;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleScanResult;
import com.test.smartbandage.R;
import com.test.smartbandage.bluetooth.ConnectingDialog;
import com.test.smartbandage.bluetooth.RxBle;
import com.test.smartbandage.model.RxBleDeviceWrapper;
import com.test.smartbandage.sensors.SensorListActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ConnectionActivity extends AppCompatActivity implements MovesenseContract.View, View.OnClickListener{

    @BindView(R.id.movesense_recyclerView)
    RecyclerView mMovesenseRecyclerView;
    @BindView(R.id.movesense_infoTv)
    TextView connectionInstructions;
    @BindView(R.id.movesense_progressBar)
    ProgressBar mMovesenseProgressBar;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;

    private MovesenseContract.Presenter mMovesensePresenter;
    private ArrayList<RxBleDeviceWrapper> mMovesenseModels;
    private CompositeDisposable scanningSubscriptions;
    private CompositeDisposable connectedDevicesSubscriptions;

    // BleClient singleton
    private RxBleClient mBleClient;
    private RxBleDevice bleDevice;
    private Disposable connectionDisposable;

    private final String TAG = "ConnectionDebug";
    private MovesenseAdapter mMovesenseAdapter;
    public String mMacAddress = "sample_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Starting MovesenseActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movesense);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bluetooth Connection");
        }

        //   connectionInstructions.setText("@string/connectionInstruction");

        scanningSubscriptions = new CompositeDisposable();
        connectedDevicesSubscriptions = new CompositeDisposable();

        mMovesensePresenter = new MovesensePresenter(this,
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE));

        mMovesensePresenter.onCreate();

        mMovesenseModels = new ArrayList<>();

        mMovesenseAdapter = new MovesenseAdapter(mMovesenseModels, this);
        mMovesenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMovesenseRecyclerView.setAdapter(mMovesenseAdapter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enable so run
            bluetoothAdapter.enable();
        }

        startScanning();

    }

    @Override
    public void displayScanResult(RxBleDevice bluetoothDevice, int rssi) {
        Log.d(TAG, "displayScanResult: " + bluetoothDevice.getName());
        mMovesenseAdapter.add(new RxBleDeviceWrapper(rssi, bluetoothDevice));

        mMovesenseAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerReceiver(BroadcastReceiver broadcastReceiver) {
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public boolean checkLocationPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(ConnectionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Changing location permissions");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ConnectionActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(ConnectionActivity.this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ConnectionActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(ConnectionActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setPresenter(MovesenseContract.Presenter presenter) {
        mMovesensePresenter = presenter;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    startScanning();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMovesensePresenter.onBluetoothResult(requestCode, resultCode, data);
    }

    private void startScanning() {
        // Make sure we have location permission
        if (!checkLocationPermission()) {
            return;
        }

        Log.d(TAG, "START SCANNING !!!");
        // Start scanning
        scanningSubscriptions.add(RxBle.Instance.getClient().scanBleDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RxBleScanResult>() {
                    @Override
                    public void accept(RxBleScanResult rxBleScanResult) {
                        Log.d(TAG, "call: SCANNED: " + rxBleScanResult.getBleDevice().getName() + " " + rxBleScanResult.getBleDevice().getMacAddress()
                                + " rssi: " + rxBleScanResult.getRssi());
                        RxBleDevice rxBleDevice = rxBleScanResult.getBleDevice();

                        if (rxBleDevice.getName() != null && !mMovesenseModels.contains(rxBleDevice)) {

                            Log.d(TAG, "call: Add to list " + rxBleScanResult.getBleDevice().getName());
                            mMovesenseAdapter.add(new RxBleDeviceWrapper(rxBleScanResult.getRssi(), rxBleDevice));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(TAG, "scanBleDevices(): ", throwable);
                    }
                }));
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ConnectionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        final RxBleDevice rxBleDevice = (RxBleDevice) v.getTag();
        Log.d(TAG, "Connecting to : " + rxBleDevice.getName() + " " + rxBleDevice.getMacAddress());
        mMacAddress = rxBleDevice.getMacAddress();
        mMovesenseProgressBar.setVisibility(View.GONE);

        // We are in connecting progress we don't need to scan anymore
        scanningSubscriptions.dispose();
        mMovesensePresenter.stopScanning();
        connectBLEDevice(rxBleDevice);

        ConnectingDialog.INSTANCE.showDialog(this, rxBleDevice.getMacAddress());

    }
    private void connectBLEDevice(RxBleDevice bleDevice) {
        final Activity me = this;
        Intent intent = new Intent(this, SensorListActivity.class);
        Log.d(TAG, "mMacAddress:" + mMacAddress);
        intent.putExtra(SensorListActivity.EXTRA_MAC_ADDRESS, mMacAddress);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMovesensePresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        mMovesensePresenter.onDestroy();
        connectedDevicesSubscriptions.dispose();
        scanningSubscriptions.dispose();
    }
    private void showConnectionError(Throwable throwable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Connection Error:")
                .setMessage(throwable.getMessage());

        builder.create().show();
    }
    @SuppressWarnings("unused")
    private void onConnectionReceived(RxBleConnection connection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Connection received")
                .setMessage("");
        builder.create().show();
        Log.d(TAG, "onConnect:");
        Intent intent = new Intent(this, SensorListActivity.class);
        Log.d(TAG, "mMacAddress:" + mMacAddress);
        intent.putExtra(SensorListActivity.EXTRA_MAC_ADDRESS, mMacAddress);
        startActivity(intent);
    }
    private void dispose() {
        connectionDisposable = null;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, com.test.smartbandage.MainViewActivity.class));
    }
}

