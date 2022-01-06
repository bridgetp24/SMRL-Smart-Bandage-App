package com.test.smartbandage.sensors;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.test.smartbandage.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EcgPortraitFragment extends Fragment {


    public EcgPortraitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ecg_portrait, container, false);
    }

}
