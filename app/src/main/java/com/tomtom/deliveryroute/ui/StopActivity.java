package com.tomtom.deliveryroute.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;

public class StopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        StopFragment fragment = new StopFragment();
        fragment.setArguments(getIntent().getExtras());

        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        setContentView(R.layout.activity_stop);
    }

    @Override
    public void onPause() {
        super.onPause();

        // set activity state in application, we use this again later in the RouteService
        DeliveryApplication.activityPaused();
    }

    @Override
    public void onResume() {
        super.onResume();

        // set activity state in application, we use this again later in the RouteService
        DeliveryApplication.activityResumed();
    }
}
