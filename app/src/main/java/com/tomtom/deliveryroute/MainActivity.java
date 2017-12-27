package com.tomtom.deliveryroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tomtom.navapp.NavAppClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView mStopText;
    private Button mPrevStopButton;
    private Button mStartButton;
    private Button mNextStopButton;
    private String mRouteFilename = "route.csv";
    DeliveryApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "> onCreate");

        mApplication = (DeliveryApplication) getApplication();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartButton = (Button) findViewById(R.id.button_start);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // start the route service, this loads the route and plans to the first stop
                mApplication.loadRoute(mRouteFilename);
                RouteStop stop = mApplication.mRoute.nextStop();
                updateUI();

                Intent intent = new Intent(MainActivity.this, RouteService.class);
                intent.putExtra(RouteService.ROUTESTOP, stop);
                startService(intent);
                launchNavApp();
            }
        });

        mPrevStopButton = (Button) findViewById(R.id.button_prev_stop);
        mPrevStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                RouteStop stop = mApplication.mRoute.prevStop();
                if (stop != null) {
                    updateUI();

                    // tell route service to plan route to previous stop
                    Intent intent = new Intent(MainActivity.this, RouteService.class);
                    intent.putExtra(RouteService.ROUTESTOP, stop);
                    startService(intent);
                    launchNavApp();
                }
            }
        });

        mNextStopButton = (Button) findViewById(R.id.button_next_stop);
        mNextStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                RouteStop stop = mApplication.mRoute.nextStop();
                if (stop != null) {
                    updateUI();

                    // tell route service to plan route to next stop
                    Intent intent = new Intent(MainActivity.this, RouteService.class);
                    intent.putExtra(RouteService.ROUTESTOP, stop);
                    startService(intent);
                    launchNavApp();
                }
            }
        });

        mStopText = (TextView) findViewById(R.id.text_stop);

        updateUI();

        Log.d(TAG, "< onCreate");
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
    protected void onDestroy() {
        Log.d(TAG, "> onDestroy");
        super.onDestroy();

        Log.d(TAG, "< onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_cancel:
                // User chose the "Clear route" action.
                clearRoute();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchNavApp() {
        Log.d(TAG, "> launchNavApp");
        final Intent intent = new Intent(NavAppClient.ACTION_LAUNCH_NAVAPP);
        startActivity(intent);
        Log.d(TAG, "< launchNavApp");
    }

    private void clearRoute() {
        // stop the route service
        Intent intent = new Intent(MainActivity.this, RouteService.class);
        stopService(intent);
        mApplication.mRoute = null;

        updateUI();
    }

    private void setStopText() {
        RouteStop stop = null;
        if (mApplication.mRoute != null) {
            stop = mApplication.mRoute.getCurrentStop();
        }
        if (stop == null) {
            mStopText.setText("");
            return;
        }

        final String stopName = stop.getName();
        final String stopStreet = stop.getStreet();
        final String stopHouseNumber = stop.getHouseNumber();
        final String stopExtra = stop.getExra();

        String stopString = getString(R.string.current_stop);
        if (stopName != null) {
            stopString += "\n" + stopName;
        }
        if (stopStreet != null) {
            stopString += "\n" + stopStreet;
        }
        if (stopHouseNumber != null) {
            stopString += " " + stopHouseNumber;
        }
        if (stopExtra != null) {
            stopString += "\n" + stopExtra;
        }
        mStopText.setText(stopString);
    }

    private void updateUI() {
        setStopText();

        if (mApplication.mRoute == null) {
            mNextStopButton.setVisibility(View.INVISIBLE);
            mPrevStopButton.setVisibility(View.INVISIBLE);
            mStartButton.setVisibility(View.VISIBLE);
        } else {
            mNextStopButton.setVisibility(View.VISIBLE);
            mPrevStopButton.setVisibility(View.VISIBLE);
            mStartButton.setVisibility(View.INVISIBLE);
        }
    }
}
