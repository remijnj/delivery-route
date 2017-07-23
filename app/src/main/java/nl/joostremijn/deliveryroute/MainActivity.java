package nl.joostremijn.deliveryroute;

import android.content.Intent;
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

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //private final Set<NavAppClientDeadListener> mNavAppClientDeadListeners = new HashSet<NavAppClientDeadListener>();

    private NavAppClient mNavappClient = null;
    private Route mRoute = null;

    private final ErrorCallback mErrorCallback = new ErrorCallback() {
        @Override
        public void onError(NavAppError error) {
            Log.e(TAG, "onError(" + error.getErrorMessage() + ")\n" + error.getStackTraceString());
            Toast toast = Toast.makeText(MainActivity.this, error.getErrorMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            mNavappClient = null;
            //informNavAppDeadListeners();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "> onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView text = (TextView) findViewById(R.id.text_load);
        final Button loadButton = (Button) findViewById(R.id.button_load);
        loadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                loadRoute("route.csv");
                text.setText("Loaded route.csv");
                loadButton.setText("Reload");
            }
        });

        final Button startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                if (mRoute == null) {
                    Toast toast = Toast.makeText(MainActivity.this, "no route loaded", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    mRoute.planRouteToNextStop();
                    launchNavApp();
                }
            }
        });

        // Create the NavAppClient
        createNavAppClient();

        Log.d(TAG, "< onCreate");
    }

    private void loadRoute(String filename) {
        mRoute = new Route(filename, mNavappClient);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "> onDestroy");
        super.onDestroy();
        // Shutdown the NavAppClient
        if (mNavappClient != null) {
            mNavappClient.close();
            mNavappClient = null;
        }
        Log.d(TAG, "< onDestroy");
    }

    public NavAppClient getClient() {
        return mNavappClient;
    }

    public boolean createNavAppClient() {
        Log.d(TAG, "> createNavAppClient");
        if (mNavappClient == null) {
            // Create the NavAppClient
            try {
                mNavappClient = NavAppClient.Factory.make(this, mErrorCallback);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed creating NavAppClient", e);
                return false;
            }
        }
        Log.d(TAG, "< createNavAppClient");
        return true;
    }

    private void launchNavApp() {
        final Intent intent = new Intent(NavAppClient.ACTION_LAUNCH_NAVAPP);
        startActivity(intent);
    }
}
