package com.tomtom.deliveryroute;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * ListWidgetService is the service which sets up the list adapter (ListViewFactory) for the
 * widget list.
 * 
 */
public class ListWidgetService extends RemoteViewsService {
    private final static String TAG = "ListWidgetService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d(TAG, "onGetViewFactory appWidgetId=" + appWidgetId);

        return (new ListViewFactory((DeliveryApplication)getApplication(), this.getApplicationContext(), intent));
    }

}

