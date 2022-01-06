package com.test.smartbandage.model;


import com.polidea.rxandroidble2.RxBleDevice;

public class RxBleDeviceWrapper {

    private int rssi;
    private RxBleDevice mRxBleDevice;

    public RxBleDeviceWrapper(int rssi, RxBleDevice rxBleDevice) {
        this.rssi = rssi;
        mRxBleDevice = rxBleDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public RxBleDevice getRxBleDevice() {
        return mRxBleDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        com.test.smartbandage.model.RxBleDeviceWrapper that = (com.test.smartbandage.model.RxBleDeviceWrapper) o;

        return mRxBleDevice != null ? mRxBleDevice.getMacAddress().equals(that.mRxBleDevice.getMacAddress()) : that.mRxBleDevice.getMacAddress() == null;
    }

    @Override
    public int hashCode() {
        int result = mRxBleDevice.hashCode();
        result = 31 * result + (mRxBleDevice != null ? mRxBleDevice.hashCode() : 0);
        return result;
    }
}
