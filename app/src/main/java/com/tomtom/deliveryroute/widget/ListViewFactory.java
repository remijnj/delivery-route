package com.tomtom.deliveryroute.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;
import com.tomtom.deliveryroute.RouteStop;

/**
 * This is needed to feed data into the listview on the widget side. This acts like an ArrayAdapter
 * for a regular ListView.
 *
 */
public class ListViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private final static String TAG = "ListViewFactory";
    private Context mContext = null;
    private int mAppWidgetId;
    private DeliveryApplication mApplication;

    public ListViewFactory(DeliveryApplication application, final Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        mApplication = application;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount()=" + mApplication.mRoute.size());
        return mApplication.mRoute.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null; // use default loading view
    }

    /*
    * Similar to the getView of ArrayAdapter but instead of View we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                mContext.getPackageName(), R.layout.list_row);
        RouteStop stop = mApplication.mRoute.getStop(position);

        Log.d(TAG, "> getViewAt(" + position + ")");

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(ListClickReceiver.EXTRA_ITEMID, position);
        remoteView.setOnClickFillInIntent(R.id.row_container, fillInIntent);

        Log.d(TAG, "name=" + stop.getName());
        Log.d(TAG, "street=" + stop.getStreet());
        Log.d(TAG, "housenumber=" + stop.getHouseNumber());
        remoteView.setTextViewText(R.id.row_list_name, stop.getName());
        remoteView.setTextViewText(R.id.row_list_street, stop.getStreet() + " " + stop.getHouseNumber());

        if (position ==  mApplication.mRoute.getCurrentStopIndex()) {
            // current stop has to be highlighted
            remoteView.setTextColor(R.id.row_list_name, Color.GREEN);
            //remoteView.setTextViewTextSize(R.id.row_list_name, TypedValue.COMPLEX_UNIT_SP, 30);
            remoteView.setTextColor(R.id.row_list_street, Color.GREEN);
            remoteView.setTextViewTextSize(R.id.row_list_street, TypedValue.COMPLEX_UNIT_SP, 30);
        }

        Log.d(TAG, "< getViewAt");
        return remoteView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onCreate() {
        mApplication.loadRoute();
    }

    @Override
    public void onDataSetChanged() {
    }


    @Override
    public void onDestroy() {
    }
}

