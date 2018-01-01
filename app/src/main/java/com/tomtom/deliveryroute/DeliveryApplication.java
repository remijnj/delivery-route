package com.tomtom.deliveryroute;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import static com.tomtom.deliveryroute.RouteService.ROUTESTOP;

/**
 * Created by joost on 12/09/17.
 */

public class DeliveryApplication extends Application {
    private static final String TAG = "DeliveryApplication";
    private static String ROUTE_FILENAME = "route.csv";
    private static Route mRoute;
    private static RouteLoader mRouteLoader;

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
        //loadRoute();

        mRouteLoader = new RouteLoader(this, mRoute);
    }
}
