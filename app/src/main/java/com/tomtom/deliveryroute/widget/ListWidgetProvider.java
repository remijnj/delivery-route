package com.tomtom.deliveryroute.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;

/**
 * This is the ListWidget provider. This sets up the widget and updates the view.
 */

public class ListWidgetProvider extends AppWidgetProvider {
    private final static String TAG = "ListWidgetProvider";
    private int X = 0;

    private static void updateAll(Context context) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        final ComponentName cn = new ComponentName(context, ListWidgetProvider.class);

        Intent intent = new Intent(context, ListWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetManager.getAppWidgetIds(cn));
        context.sendBroadcast(intent);
    }

    @Override
    public void onEnabled(final Context context) {
        Log.d(TAG, "> onEnabled");
        DeliveryApplication.getRoute().registerObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Log.d(TAG, "> onChanged");

                super.onChanged();
                final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
                final ComponentName cn = new ComponentName(context, ListWidgetProvider.class);

                // THIS DOES NOT WORK - TODO: find out how to make the scrolling work
                /*
                // scroll to right place
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.listwidget);
                remoteViews.setScrollPosition(R.id.listView, DeliveryApplication.mRoute.getCurrentStopIndex());

                // notify widget of changes in the listview (scroll)
                widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(cn), remoteViews);
                */

                // notify widget of changes in the listview (content)
                widgetManager.notifyAppWidgetViewDataChanged(widgetManager.getAppWidgetIds(cn), R.id.listView);

                updateAll(context);

                Log.d(TAG, "< onChanged");
            }
        });
        Log.d(TAG, "< onEnabled");
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "> onUpdate");
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "onUpdate() pkgname=[" + context.getPackageName() + "]");
            // Debug text in the textview
            final RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.listwidget);

            // Now comes the actual list
            // RemoteViews Service needed to provide adapter for ListView
            Intent serviceIntent = new Intent(context, ListWidgetService.class);
            // passing app widget id to that RemoteViews Service
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // setting a unique Uri to the intent, why?
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            //setting adapter to listview of the widget
            widget.setRemoteAdapter(R.id.listView, serviceIntent);

            // now setup the click listener
            Intent clickIntent = new Intent(context, ListClickReceiver.class);
            clickIntent.setAction(ListClickReceiver.LISTITEM_CLICK);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(R.id.listView, clickPendingIntent);

            // set the scroll position
            final int scrollPos = DeliveryApplication.getRoute().getCurrentStopIndex();
            Log.d(TAG, "setting scroll position to " + scrollPos);
            widget.setScrollPosition(R.id.listView, scrollPos);
/*
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "delayed: setting scroll position to " + scrollPos);

                    widget.setScrollPosition(R.id.listView, scrollPos);
                }
            }, 1000);
*/

            //setting an empty view in case of no data
            //views.setEmptyView(R.id.list_view, R.id.empty_view);

            //widget.setTextViewText(R.id.textbox, "List setup done");

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }

        Log.d(TAG, "< onUpdate");
    }
}
