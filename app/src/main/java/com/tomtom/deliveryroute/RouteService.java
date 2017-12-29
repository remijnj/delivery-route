package com.tomtom.deliveryroute;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.tomtom.deliveryroute.ui.MainActivity;
import com.tomtom.deliveryroute.ui.StopActivity;
import com.tomtom.navapp.ErrorCallback;
import com.tomtom.navapp.NavAppClient;
import com.tomtom.navapp.NavAppError;
import com.tomtom.navapp.Routeable;
import com.tomtom.navapp.Trip;

/**
 * RouteService
 * <p>
 * This service is meant to plan the route to the next stop and watch the ETA.
 * <p>
 * When we get close to the stop we want to show an overlay with some more
 * delivery information.
 */
public class RouteService extends Service {
    private static final String TAG = "RouteService";
    private static final int FOREGROUND_ID = 129;
    private NavAppClient mNavappClient = null;
    private Trip mTrip;
    private boolean mOverlayShowing;
    public static final String ROUTESTOP = "ROUTESTOP";
    private RouteStop mStop;

    public RouteService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "> onCreate");

        // start as a foreground service so this keeps running
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Delivery Route")
                .setSmallIcon(R.drawable.ic_local_shipping_white_24dp)
                .build();
        startForeground(FOREGROUND_ID, notification);

        // Create the NavAppClient
        createNavAppClient();

        Log.d(TAG, "< onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "> onDestroy");

        // Cancel the foreground service / notification.
        stopForeground(true);

        // clear the route
        //
        // TODO: this does not always work, the mNavAppClient.close() call stops the conection before the
        //       route has been cancelled. We need to move the close to the planlistener or even try to
        //       cancel the route before the onDestroy() happens somehow.
        //
        mNavappClient.getTripManager().cancelTrip(mTrip, mPlanListener);

        // Close the navapp client connection
        mNavappClient.close();
        mNavappClient = null;

        Log.d(TAG, "< onDestroy");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "> onStartCommand");

        if (intent != null) {
            mStop = (RouteStop) intent.getSerializableExtra(ROUTESTOP);
        }

        if (mStop != null) {
            // plan the route to the next stop in the list
            planRouteToStop(mStop, mPlanListener);
        }

        Log.d(TAG, "< onStartCommand");

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Plans route to next stop
     */
    private void planRouteToStop(RouteStop stop, Trip.PlanListener planListener) {
        double lat = stop.getLatitude();
        double lon = stop.getLongitude();
        Routeable dest = mNavappClient.makeRouteable(lat, lon);

        Log.d(TAG, "planning route to [" + dest.getLatitude() + ", " + dest.getLongitude() + "]");
        Log.d(TAG, "\taddress=[" + dest.getAddress() + "]");
        Log.d(TAG, "\tname=[" + stop.getName() + "]");

        mNavappClient.getTripManager().planTrip(dest, planListener);

        String destname = stop.getName();
        if (destname == null) {
            destname = lat + "," + lon;
        }
        String toastText = getString(R.string.planning_route) + " " + destname;
        Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
        toast.show();

        Log.d(TAG, "< planRouteToNextStop");
    }

    private boolean createNavAppClient() {
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

    private Trip.PlanListener mPlanListener = new Trip.PlanListener() {
        @Override
        public void onTripPlanResult(Trip trip, Trip.PlanResult result) {
            Log.d(TAG, "onTripPlanResult result[" + result + "]");

            // successfully planned
            if (Trip.PlanResult.PLAN_OK.equals(result)) {
                mTrip = trip;

                // Watch for ETA changes (we want to show an overlay when we get close to a stop)
                mNavappClient.getTripManager().registerTripProgressListener(mProgressListener);
            }

            // successfully cancelled
            if (Trip.PlanResult.TRIP_CANCELLED.equals(result)) {
                mTrip = null;
            }
        }
    };

    private Trip.ProgressListener mProgressListener = new Trip.ProgressListener() {
        public static final int NOTIFY_NEVER = 0;
        public static final int NOTIFY_ALWAYS = -1;

        @Override
        public void onTripArrival(Trip trip) {
            Log.d(TAG, "> onTripArrival");
            Log.d(TAG, "< onTripArrival");
        }

        @Override
        public void onTripProgress(Trip trip, long eta, int distanceRemaining) {
            //Log.d(TAG, "> onTripProgress");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RouteService.this);
            String notificationDistanceMeters = preferences.getString("notification_distance", "500");
            int notificationDistanceMetersInt = Integer.parseInt(notificationDistanceMeters);

            //Log.d(TAG, "eta=" + eta + " distanceRemaining=" + distanceRemaining + " meters" + " notification_distance=" + notificationDistanceMeters);
            if (notificationDistanceMetersInt == NOTIFY_NEVER) {
                return;
            }

            // do not show overlay if overlay is already showing or if MainActivity is in the front
            if (!mOverlayShowing && !DeliveryApplication.isActivityVisible()) {
                if (notificationDistanceMetersInt == NOTIFY_ALWAYS || distanceRemaining < notificationDistanceMetersInt) {
                    showOverlay();
                }
            }

            //Log.d(TAG, "< onTripProgress");
        }
    };

    private void showOverlay() {
        Log.d(TAG, "> showOverlay");

        final WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;

        LayoutInflater mInflater = LayoutInflater.from(this);
        final View floatDialogView = mInflater.inflate(R.layout.arrival_dialog, null);

        final String stopName = mStop.getName();
        final String stopStreet = mStop.getStreet();
        final String stopHouseNumber = mStop.getHouseNumber();
        String arrivalString = "";

        TextView arrival_text = (TextView) floatDialogView.findViewById(R.id.arrival_text);
        if (stopName != null && !stopName.isEmpty()) {
            arrivalString = stopName + "\n";
        }
        if (stopStreet != null && !stopStreet.isEmpty()) {
            arrivalString += stopStreet;
        }
        arrival_text.setText(arrivalString);

        TextView arrival_text_housenumber = (TextView) floatDialogView.findViewById(R.id.arrival_text_housenumber);
        if (stopHouseNumber != null) {
            arrival_text_housenumber.setText(stopHouseNumber);
        }

        floatDialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "> onClick");

                // Dismiss this view
                wm.removeView(floatDialogView);
                mOverlayShowing = false;

                // Start the detail view
                Intent intent = new Intent(RouteService.this, StopActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                Log.d(TAG, "< onClick");
            }
        });

        wm.addView(floatDialogView, wmParams);
        mOverlayShowing = true;

        Log.d(TAG, "< showOverlay");
    }
}