package nl.joostremijn.deliveryroute;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.tomtom.navapp.NavAppClient;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String EXTRA_STOP = "stop";
    private TextView mStopText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "> onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView text = (TextView) findViewById(R.id.text_load);
        final Button startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                // start the route service, this loads the route and plans to the first stop
                Intent intent = new Intent(MainActivity.this, RouteService.class);
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
            }
        });

        mStopText = (TextView) findViewById(R.id.text_stop);

        Intent intent = getIntent();
        handleStop(intent);

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
    public void onNewIntent(Intent intent) {
        handleStop(intent);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "> onDestroy");
        super.onDestroy();

        Intent intent = new Intent(MainActivity.this, RouteService.class);
        stopService(intent);

        Log.d(TAG, "< onDestroy");
    }

    private void handleStop(Intent intent) {
        String stop = intent.getStringExtra(EXTRA_STOP);
        if (stop != null) {
            mStopText.setText(stop);
        }
    }

    private void launchNavApp() {
        Log.d(TAG, "> launchNavApp");
        final Intent intent = new Intent(NavAppClient.ACTION_LAUNCH_NAVAPP);
        startActivity(intent);
        Log.d(TAG, "< launchNavApp");
    }
}
