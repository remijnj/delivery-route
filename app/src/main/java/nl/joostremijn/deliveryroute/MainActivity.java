package nl.joostremijn.deliveryroute;

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
    public static final String EXTRA_STOP = "stop";
    private static final String ROUTE_FILENAME = "ROUTE_FILENAME";
    private static final String ROUTE_CURIDX = "ROUTE_CURIDX";
    private TextView mStopText;
    private Button mPrevStopButton;
    private Button mNextStopButton;
    private Route mRoute = null;
    private String mRouteFilename = "route.csv";
    private int mRouteCurrentIdx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "> onCreate");

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            mRouteFilename = savedInstanceState.getString(ROUTE_FILENAME);
            mRouteCurrentIdx = savedInstanceState.getInt(ROUTE_CURIDX);

            Log.d(TAG, "restored instance state: file=" + mRouteFilename + " idx=" + mRouteCurrentIdx);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // start the route service, this loads the route and plans to the first stop
                loadRoute(mRouteFilename);
                RouteStop stop = mRoute.nextStop();
                setStopText();
                mNextStopButton.setVisibility(View.VISIBLE);
                mPrevStopButton.setVisibility(View.VISIBLE);

                Intent intent = new Intent(MainActivity.this, RouteService.class);
                intent.putExtra(RouteService.ROUTESTOP, stop);
                startService(intent);
                launchNavApp();
            }
        });

        /*
        final Button stopButton = (Button) findViewById(R.id.button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                clearRoute();
            }
        });
        */

        mPrevStopButton = (Button) findViewById(R.id.button_prev_stop);
        mPrevStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                RouteStop stop = mRoute.prevStop();
                if (stop != null) {
                    setStopText();

                    // tell route service to plan route to previous stop
                    Intent intent = new Intent(MainActivity.this, RouteService.class);
                    intent.putExtra(RouteService.ROUTESTOP, stop);
                    startService(intent);
                    launchNavApp();
                }
            }
        });
        mPrevStopButton.setVisibility(View.INVISIBLE);

        mNextStopButton = (Button) findViewById(R.id.button_next_stop);
        mNextStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                RouteStop stop = mRoute.nextStop();
                if (stop != null) {
                    setStopText();

                    // tell route service to plan route to next stop
                    Intent intent = new Intent(MainActivity.this, RouteService.class);
                    intent.putExtra(RouteService.ROUTESTOP, stop);
                    startService(intent);
                    launchNavApp();
                }
            }
        });
        mNextStopButton.setVisibility(View.INVISIBLE);

        mStopText = (TextView) findViewById(R.id.text_stop);

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(myToolbar);

        Log.d(TAG, "< onCreate");
    }

    private void clearRoute() {
        // stop the route service
        Intent intent = new Intent(MainActivity.this, RouteService.class);
        stopService(intent);
        mRoute = null;

        mNextStopButton.setVisibility(View.INVISIBLE);
        mPrevStopButton.setVisibility(View.INVISIBLE);
        setStopText();
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "> onSaveInstanceState");
        // Save the route filename + index
        savedInstanceState.putString(ROUTE_FILENAME, mRouteFilename);
        savedInstanceState.putInt(ROUTE_CURIDX, mRouteCurrentIdx);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "< onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "> onDestroy");
        super.onDestroy();

        //Intent intent = new Intent(MainActivity.this, RouteService.class);
        //stopService(intent);

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

    private void loadRoute(String filename) {
        Log.d(TAG, "> loadRoute");
        mRoute = new Route(filename);
        Log.d(TAG, "< loadRoute");
    }

    private void setStopText() {
        RouteStop stop = null;
        if (mRoute != null) {
            stop = mRoute.getCurrentStop();
        }
        if (stop == null) {
            mStopText.setText("");
            return;
        }

        final String stopName = stop.getName();
        final String stopStreet = stop.getStreet();
        final String stopHouseNumber = stop.getHouseNumber();
        final String stopExtra = stop.getExra();

        String stopString = "Next stop:";
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
}
