package com.test.smartbandage;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainViewActivity extends AppCompatActivity {

    private final String TAG = "mainMenuDebug";

    @BindView(R.id.mainView_movesense_Ll)
    RelativeLayout mMainViewMovesenseLl;
//    @BindView(R.id.mainView_multiConnection_Ll)
//    RelativeLayout mMainViewMultiConnectionLl;
    @BindView(R.id.mainView_savedData_Ll)
   RelativeLayout mMainViewSavedDataLl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        ButterKnife.bind(this);


    }


    @OnClick({R.id.mainView_movesense_Ll, R.id.mainView_savedData_Ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mainView_movesense_Ll:
                Log.d(TAG, "Starting connection intent");
                startActivity(new Intent(com.test.smartbandage.MainViewActivity.this, com.test.smartbandage.bluetooth.connection.ConnectionActivity.class));

                break;

            case R.id.mainView_savedData_Ll:
                Log.d(TAG, "Starting data manager intent");
                startActivity(new Intent(com.test.smartbandage.MainViewActivity.this, com.test.smartbandage.data_manager.DataManagerActivity.class));
                break;

        }
    }

}
