package com.tomtom.deliveryroute;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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


}
