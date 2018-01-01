package com.tomtom.deliveryroute.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tomtom.deliveryroute.ui.StopActivity;
import com.tomtom.deliveryroute.ui.StopFragment;

/**
 * This receives the clicks from the (Widget) List of route stops.
 */
public class ListClickReceiver extends BroadcastReceiver {
    public static final String LISTITEM_CLICK = "LISTITEM_CLICK";
    public static final String EXTRA_ITEMID = "ITEMID";
    private final static String TAG = "ListClickReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(EXTRA_ITEMID, -1);
        Log.d(TAG, "id=" + id);

        // Start the detail view
        Intent stopActivityIntent = new Intent(context, StopActivity.class);
        stopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopActivityIntent.putExtra(StopFragment.ROUTESTOP_ID, id);

        context.startActivity(stopActivityIntent);
    }
}
