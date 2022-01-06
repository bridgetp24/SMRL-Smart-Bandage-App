package com.test.smartbandage.bluetooth.connection;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.RxBleDevice;
import com.test.smartbandage.R;
import com.test.smartbandage.model.RxBleDeviceWrapper;

import java.util.ArrayList;

public class MovesenseAdapter extends RecyclerView.Adapter<com.test.smartbandage.bluetooth.connection.MovesenseAdapter.ViewHolder> {

    private ArrayList<RxBleDeviceWrapper> mMovesenseModelArrayList;
    private View.OnClickListener mOnClickListener;

    public MovesenseAdapter(ArrayList<RxBleDeviceWrapper> movesenseModelArrayList, View.OnClickListener onClickListener) {
        mMovesenseModelArrayList = movesenseModelArrayList;
        mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movesense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RxBleDevice rxBleDevice = mMovesenseModelArrayList.get(position).getRxBleDevice();

        holder.name.setText(rxBleDevice.getName());
        holder.address.setText(rxBleDevice.getMacAddress());
        holder.rssi.setText("Rssi: " + mMovesenseModelArrayList.get(position).getRssi() + " dBm");
        holder.itemView.setTag(rxBleDevice);
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mMovesenseModelArrayList.size();
    }

    public void add(RxBleDeviceWrapper rxBleDeviceWrapper) {
        if (!mMovesenseModelArrayList.contains(rxBleDeviceWrapper)) {
            mMovesenseModelArrayList.add(rxBleDeviceWrapper);

            notifyItemChanged(mMovesenseModelArrayList.size());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView address;
        private TextView rssi;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.movesense_name);
            address = itemView.findViewById(R.id.movesense_address);
            rssi = itemView.findViewById(R.id.movesense_rsid);
        }
    }
}
