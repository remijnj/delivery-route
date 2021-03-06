package com.tomtom.deliveryroute;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
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

    public static final String ROUTESTOP = "ROUTESTOP";
    public static final int NOTIFY_NEVER = 0;
    public static final int NOTIFY_ALWAYS = -1;
    private static final int FOREGROUND_ID = 129;

    private NavAppClient mNavappClient = null;
    private Trip mTrip;
    private RouteStop mStop;
    private WindowManager mWindowManager;
    private View mFloatDialogView;
    private boolean mOverlayShowing;
    private boolean mPlanningRoute;

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

    private Trip.ProgressListener mProgressListener = new Trip.ProgressListener() {
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
    private Trip.PlanListener mPlanListener = new Trip.PlanListener() {
        @Override
        public void onTripPlanResult(Trip trip, Trip.PlanResult result) {
            Log.d(TAG, "onTripPlanResult result[" + result + "]");

            // successfully planned
            if (Trip.PlanResult.PLAN_OK.equals(result)) {
                mTrip = trip;

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RouteService.this);
                String notificationDistanceMeters = preferences.getString("notification_distance", "500");
                int notificationDistanceMetersInt = Integer.parseInt(notificationDistanceMeters);

                if (notificationDistanceMetersInt != NOTIFY_NEVER) {
                    // Watch for ETA changes (we want to show an overlay when we get close to a stop)
                    mNavappClient.getTripManager().registerTripProgressListener(mProgressListener);
                }
            }

            // successfully cancelled
            if (Trip.PlanResult.TRIP_CANCELLED.equals(result)) {
                mTrip = null;
            }

            mPlanningRoute = false;
        }
    };

    private Trip.Listener mTripListener = new Trip.Listener() {
        @Override
        public void onTripActive(Trip trip) {
            Log.d(TAG, "> mTripListener.onTripActive()");

            Log.d(TAG, "trip destination=" + (trip == null ? "<none>" : trip.getDestination().getAddress()));
            if (mPlanningRoute) {
                Log.d(TAG, "this is OK, we called planTrip");
            } else {
                Log.d(TAG, "this is NOT OK. Not expecting a new route.");
                // now we need to cancel the route and stuff and unset current destination, tricky..

                // dismiss the overlay and remove the progress listener
                Log.d(TAG, "dismiss the overlay and remove trip progress listeners");
                dismissOverlay();
                mNavappClient.getTripManager().unregisterTripProgressListener(mProgressListener);

                // TODO: Perhaps also unset the current destination on the Route but this is too invasive for now
            }
            Log.d(TAG, "< mTripListener.onTripActive()");
        }
    };

    public RouteService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "> onCreate");

        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // start as a foreground service so this keeps running
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Delivery Route")
                .setSmallIcon(R.drawable.ic_local_shipping_white_24dp)
                .setContentIntent(mainPendingIntent)
                .setShowWhen(false)
                .build();
        startForeground(FOREGROUND_ID, notification);

        // Create the NavAppClient
        createNavAppClient();

        // set a listener for trips
        if (mNavappClient != null) {
            mNavappClient.getTripManager().registerTripListener(mTripListener);
        }

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
        if (mNavappClient != null) {
            mNavappClient.getTripManager().cancelTrip(mTrip, mPlanListener);

            // Close the navapp client connection
            mNavappClient.close();
            mNavappClient = null;
        }

        Log.d(TAG, "< onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "> onStartCommand");

        if (intent != null) {
            mStop = (RouteStop) intent.getSerializableExtra(ROUTESTOP);
        }

        if (mStop != null) {
            // dismiss overlay (because it is for the current route and we are now planning a new one)
            dismissOverlay();

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
        Log.d(TAG, "> planRouteToStop");

        double lat = stop.getLatitude();
        double lon = stop.getLongitude();

        if (mNavappClient == null) {
            Log.e(TAG, "planRouteToStop() called while navapp client is not available");
            return;
        }
        Routeable dest = mNavappClient.makeRouteable(lat, lon);

        Log.d(TAG, "planning route to [" + dest.getLatitude() + ", " + dest.getLongitude() + "]");
        Log.d(TAG, "\taddress=[" + dest.getAddress() + "]");
        Log.d(TAG, "\tname=[" + stop.getName() + "]");

        mPlanningRoute = true;
        mNavappClient.getTripManager().planTrip(dest, planListener);

        String destname = stop.getName();
        if (destname == null) {
            destname = lat + "," + lon;
        }
        String toastText = getString(R.string.planning_route) + " " + destname;
        Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
        toast.show();

        // now jump to Home when this setting is enabled
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RouteService.this);
        Boolean homeOnPlan = preferences.getBoolean("home_on_plan", true);
        if (homeOnPlan) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        Log.d(TAG, "< planRouteToStop");
    }

    // this function can later be used to go to full-screen navapp on route planning
    private void launchNavApp() {
        Log.d(TAG, "> launchNavApp");
        final Intent intent = new Intent(NavAppClient.ACTION_LAUNCH_NAVAPP);
        startActivity(intent);
        Log.d(TAG, "< launchNavApp");
    }

    private void createNavAppClient() {
        Log.d(TAG, "> createNavAppClient");
        if (mNavappClient == null) {
            // Create the NavAppClient
            try {
                mNavappClient = NavAppClient.Factory.make(this, mErrorCallback);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed creating NavAppClient", e);
            }
        }
        Log.d(TAG, "< createNavAppClient");
    }

    @SuppressLint("InflateParams")
    private void showOverlay() {
        Log.d(TAG, "> showOverlay");

        if (mWindowManager == null) {
            Log.e(TAG, "error showing Overlay (wm==null)");
            return;
        }

        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.gravity = Gravity.START | Gravity.TOP;

        LayoutInflater mInflater = LayoutInflater.from(this);
        mFloatDialogView = mInflater.inflate(R.layout.arrival_dialog, null);

        final String stopName = mStop.getName();
        final String stopStreet = mStop.getStreet();
        final String stopHouseNumber = mStop.getHouseNumber();
        String arrivalString = "";

        TextView arrival_text = (TextView) mFloatDialogView.findViewById(R.id.arrival_text);
        if (stopName != null && !stopName.isEmpty()) {
            arrivalString = stopName + "\n";
        }
        if (stopStreet != null && !stopStreet.isEmpty()) {
            arrivalString += stopStreet;
        }
        arrival_text.setText(arrivalString);

        TextView arrival_text_housenumber = (TextView) mFloatDialogView.findViewById(R.id.arrival_text_housenumber);
        if (stopHouseNumber != null) {
            arrival_text_housenumber.setText(stopHouseNumber);
        }

        mFloatDialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "> onClick");

                // Dismiss this view
                dismissOverlay();

                // Start the detail view
                Intent intent = new Intent(RouteService.this, StopActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                Log.d(TAG, "< onClick");
            }
        });

        mWindowManager.addView(mFloatDialogView, wmParams);
        mOverlayShowing = true;

        Log.d(TAG, "< showOverlay");
    }

    private void dismissOverlay() {
        Log.d(TAG, "> dismissOverlay");

        if (!mOverlayShowing) {
            return;
        }

        if (mWindowManager != null && mFloatDialogView != null) {
            mWindowManager.removeView(mFloatDialogView);
            mOverlayShowing = false;
            mFloatDialogView = null;
        }

        Log.d(TAG, "< dismissOverlay");
    }
}