package com.tomtom.deliveryroute.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;

public class StopActivity extends AppCompatActivity {
    private static final String TAG = "StopActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "> onCreate");
        super.onCreate(savedInstanceState);

        createView();
        Log.d(TAG, "< onCreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "> onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);

        createView();
        Log.d(TAG, "< onNewIntent");
    }

    private void createView() {
        Log.d(TAG, "> createView");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        StopFragment fragment = new StopFragment();
        fragment.setArguments(getIntent().getExtras());

        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        setContentView(R.layout.activity_stop);
        Log.d(TAG, "< createView");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions_stop, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                // User chose the "List" item, show the main activity with the list...
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                return true;
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

}
