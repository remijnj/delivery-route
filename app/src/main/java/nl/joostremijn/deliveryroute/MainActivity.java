package nl.joostremijn.deliveryroute;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tomtom.navapp.ErrorCallback;
import com.tomtom.navapp.NavAppClient;
import com.tomtom.navapp.NavAppError;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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

        Log.d(TAG, "< onCreate");
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "> onDestroy");

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
}
