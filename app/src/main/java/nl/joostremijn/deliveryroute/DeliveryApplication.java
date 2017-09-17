package nl.joostremijn.deliveryroute;

import android.app.Application;

/**
 * Created by joost on 12/09/17.
 */

public class DeliveryApplication extends Application {
    public Route mRoute = null;
    public int mRouteCurrentIdx = 0;

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

}
