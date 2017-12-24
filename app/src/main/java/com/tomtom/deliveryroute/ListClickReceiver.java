package com.tomtom.deliveryroute;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receives the clicks from the (Widget) List of route stops.
 */
public class ListClickReceiver extends BroadcastReceiver {
    private final static String TAG = "ListClickReceiver";
    public static final String LISTITEM_CLICK = "LISTITEM_CLICK";
    public static final String EXTRA_ITEMID = "ITEMID";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(EXTRA_ITEMID, -1);
        Log.d(TAG, "id=" + id);

        // Start the detail view
        Intent stopActivityIntent = new Intent(context, StopActivity.class);
        stopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        stopActivityIntent.putExtra(StopFragment.ROUTESTOP_ID, id);

        context.startActivity(stopActivityIntent);
    }
}
