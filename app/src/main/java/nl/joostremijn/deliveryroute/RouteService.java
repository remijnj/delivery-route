package nl.joostremijn.deliveryroute;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.tomtom.navapp.ErrorCallback;
import com.tomtom.navapp.NavAppClient;
import com.tomtom.navapp.NavAppError;

/**
 * RouteService
 *
 * This service is meant to plan the route to the next stop and watch the ETA.
 *
 * When we get close to the stop we want to show an overlay with some more
 * delivery information.
 *
 */
public class RouteService extends Service {
    private static final String TAG = "RouteService";
    private static final int FOREGROUND_ID = 129;
    private NavAppClient mNavappClient = null;
    private Route mRoute = null;
    private boolean mDebug = true;

    public RouteService() {
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "> onDestroy");

        // Cancel the foreground service / notification.
        stopForeground(true);

        // clear the route
        mRoute.clearRoute();

        // Close the navapp client connection
        mNavappClient.close();
        mNavappClient = null;

        Log.d(TAG, "< onDestroy");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "> onStartCommand");

        // start as a foreground service so this keeps running
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Delivery Route")
                .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                .build();
        startForeground(FOREGROUND_ID, notification);

        // Create the NavAppClient
        createNavAppClient();

        // Load the route
        loadRoute("route.csv");

        // plan the route to the next stop in the list
        mRoute.planRouteToNextStop();

        // TODO: watch for ETA changes (we want to show an overlay when we get close to a stop)


        Log.d(TAG, "< onStartCommand");
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
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

    private final ErrorCallback mErrorCallback = new ErrorCallback() {
        @Override
        public void onError(NavAppError error) {
            Log.e(TAG, "onError(" + error.getErrorMessage() + ")\n" + error.getStackTraceString());
            Toast toast = Toast.makeText(RouteService.this, error.getErrorMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            mNavappClient = null;
        }
    };

    private void loadRoute(String filename) {
        Log.d(TAG, "> loadRoute");
        mRoute = new Route(filename, mNavappClient);
        Log.d(TAG, "< loadRoute");
    }
}