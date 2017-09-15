package nl.joostremijn.deliveryroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.tomtom.navapp.NavAppClient;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String EXTRA_STOP = "stop";
    private TextView mStopText;
    private Route mRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "> onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // start the route service, this loads the route and plans to the first stop
                loadRoute("route.csv");
                RouteStop stop = mRoute.nextStop();
                setStopText();

                Intent intent = new Intent(MainActivity.this, RouteService.class);
                intent.putExtra(RouteService.ROUTESTOP, stop);
                startService(intent);
                launchNavApp();
            }
        });

        final Button stopButton = (Button) findViewById(R.id.button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // stop the route service
                Intent intent = new Intent(MainActivity.this, RouteService.class);
                stopService(intent);
                mRoute = null;
                setStopText();
            }
        });

        final Button nextStopButton = (Button) findViewById(R.id.button_next_stop);
        nextStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                RouteStop stop = mRoute.nextStop();
                setStopText();

                // tell route service to plan route to next stop
                Intent intent = new Intent(MainActivity.this, RouteService.class);
                intent.putExtra(RouteService.ROUTESTOP, stop);
                startService(intent);
                launchNavApp();
            }
        });
        mStopText = (TextView) findViewById(R.id.text_stop);

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

        Intent intent = new Intent(MainActivity.this, RouteService.class);
        stopService(intent);

        Log.d(TAG, "< onDestroy");
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
