package com.test.smartbandage.sensors;

import android.util.Log;
import android.widget.TextView;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.test.smartbandage.utils.HexString;

import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class GattUtils {
    final static String TAG = "GattUtils";

    public static Observable<RxBleConnection> prepareConnectionObservable(RxBleDevice bleDevice,PublishSubject<Boolean> disconnectTriggerSubject) {
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(ReplayingShare.instance());
    }
    public static void connectToGatt(UUID characteristicUUID, TextView textView, boolean asString, Observable<RxBleConnection> connectionObservable, CompositeDisposable compositeDisposable, RxBleDevice bleDevice) {
        // connect to service
        final Disposable connectionDisposable = connectionObservable
                    .flatMapSingle(RxBleConnection::discoverServices)
                    .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(characteristicUUID))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            characteristic -> {
                                Log.i(GattUtils.class.getSimpleName(), "Hey, connection has been established!");
                                if(asString) {
                                    readDeviceInfoAsString(characteristicUUID,textView,connectionObservable,compositeDisposable,bleDevice);
                                }else {
                                    readDeviceInfoAsBytes(characteristicUUID,textView,connectionObservable,compositeDisposable,bleDevice);
                                }

                            },
                            GattUtils::onConnectionFailure,
                            GattUtils::onConnectionFinished
                    );
            compositeDisposable.add(connectionDisposable);
    }

    private static void readDeviceInfoAsString(UUID characteristicUUID, TextView textView, Observable<RxBleConnection> connectionObservable, CompositeDisposable compositeDisposable, RxBleDevice bleDevice) {
        // read device info
        if(isConnected(bleDevice)) {
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(characteristicUUID))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        textView.setText(textView.getText() + " " + new String(bytes));
                    }, GattUtils::onConnectionFailure);

            compositeDisposable.add(disposable);

        }else {
            Log.d(TAG, "Device not connected");
        }

    }
    private static void writeDeviceAsBytes(UUID characteristicUUID, TextView textView, Observable<RxBleConnection> connectionObservable, CompositeDisposable compositeDisposable, RxBleDevice bleDevice,byte[] bytesToWrite) {
        // write to device characteristic UUID
        // read device info
        if(isConnected(bleDevice)) {
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(characteristicUUID,bytesToWrite))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {

                    }, GattUtils::onConnectionFailure);

            compositeDisposable.add(disposable);

        }else {
            Log.d(TAG, "Device not connected");
        }
    }

    private static void readDeviceInfoAsBytes(UUID characteristicUUID, TextView textView, Observable<RxBleConnection> connectionObservable, CompositeDisposable compositeDisposable, RxBleDevice bleDevice) {
        // read device info
        if(isConnected(bleDevice)) {
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(characteristicUUID))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        textView.setText(textView.getText() + " " + HexString.bytesToHex(bytes));
                    }, GattUtils::onConnectionFailure);

            compositeDisposable.add(disposable);

        }else {
            Log.d(TAG, "Device not connected");
        }
    }

    public static boolean isConnected(RxBleDevice bleDevice) {
        Log.d(TAG, "Connection state: " +  bleDevice.getConnectionState() );
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }
    public static void triggerDisconnect(PublishSubject<Boolean> disconnectTriggerSubject) {
        Log.d(TAG, "Triggering disconnect");
        disconnectTriggerSubject.onNext(true);
    }

    public static void onConnectionFailure(Throwable throwable) {
        Log.d(TAG, "Connection Failure " + throwable.getLocalizedMessage());

    }
    public static void onConnectionFinished() {
        Log.d(TAG, "Connection finished");
        // disonnect?
    }
}
