package com.tomtom.deliveryroute;

import android.app.Application;
import android.util.Log;

/**
 * Created by joost on 12/09/17.
 */

public class DeliveryApplication extends Application {
    private static final String TAG = "DeliveryApplication";
    private static String ROUTE_FILENAME = "route.csv";
    public Route mRoute = null;

    private static boolean mActivityVisible;

    public static boolean isActivityVisible() {
        return mActivityVisible;
    }

    public static void activityResumed() {
        mActivityVisible = true;
    }

    public static void activityPaused() {
        mActivityVisible = false;
    }

    public void loadRoute(String filename) {
        Log.d(TAG, "> loadRoute");
        mRoute = new Route(filename);
        Log.d(TAG, "< loadRoute");
    }

    public void loadRoute() {
        loadRoute(ROUTE_FILENAME);
    }

}
