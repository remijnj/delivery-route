package com.tomtom.deliveryroute;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by joost on 12/09/17.
 */

public class DeliveryApplication extends Application {
    private static final String TAG = "DeliveryApplication";
    private static String ROUTE_FILENAME = "route.csv";
    private static Route mRoute;
    private RouteLoader mRouteLoader;

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

    public static Route getRoute() {
        return mRoute;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRoute = new Route();

        mRouteLoader = new RouteLoader(this, mRoute);
    }

    public boolean getFormatUS() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("US_address_format", true);
    }
}
