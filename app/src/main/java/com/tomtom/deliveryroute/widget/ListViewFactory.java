package com.tomtom.deliveryroute.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;
import com.tomtom.deliveryroute.RouteStop;

/**
 * This is needed to feed data into the listview on the widget side. This acts like an ArrayAdapter
 * for a regular ListView.
 */
public class ListViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private final static String TAG = "ListViewFactory";
    private Context mContext = null;
    private int mAppWidgetId;
    private DeliveryApplication mApplication;

    ListViewFactory(DeliveryApplication application, final Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mApplication = application;

        mApplication.getRoute().registerObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Log.d(TAG, "> onChanged");

                super.onChanged();
                final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

                // notify widget of changes in the listview
                widgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.listView);

                Log.d(TAG, "< onChanged");
            }
        });
    }

    @Override
    public int getCount() {
        int size = mApplication.getRoute().size();
        Log.d(TAG, "getCount() => " + size);
        return size;
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
        RouteStop stop = mApplication.getRoute().getStop(position);
        boolean formatUS = mApplication.getFormatUS();
        Log.d(TAG, "> getViewAt(" + position + ")");

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(ListClickReceiver.EXTRA_ITEMID, position);
        remoteView.setOnClickFillInIntent(R.id.row_container, fillInIntent);

        int textColor = Color.LTGRAY;
        if (position == mApplication.getRoute().getCurrentStopIndex()) {
            // current stop has to be highlighted
            textColor = Color.GREEN;
        } else if (stop.isDone()) {
            textColor = 0xFF666666;
        } else if (stop.isBadAddress()) {
            textColor = Color.RED;
        }

        Log.d(TAG, "name=" + stop.getName());
        Log.d(TAG, "street=" + stop.getStreet());
        Log.d(TAG, "housenumber=" + stop.getHouseNumber());
        Log.d(TAG, "placename=" + stop.getPlacename());
        if (!TextUtils.isEmpty(stop.getName())) {
            remoteView.setTextViewText(R.id.row_list_name, stop.getName());
            remoteView.setTextColor(R.id.row_list_name, textColor);
            remoteView.setViewVisibility(R.id.row_list_name, View.VISIBLE);
        } else {
            Log.d(TAG, "name is NULL");
            remoteView.setViewVisibility(R.id.row_list_name, View.GONE);
        }
        if (!TextUtils.isEmpty(stop.getStreet())) {
            SpannableString streetAddr;
            if (formatUS) {
                streetAddr = new SpannableString(stop.getHouseNumber() + " " + stop.getStreet());
                streetAddr.setSpan(new RelativeSizeSpan(1.3f), 0, stop.getHouseNumber().length(), 0);
            } else {
                streetAddr = new SpannableString(stop.getStreet() + " " + stop.getHouseNumber());
                streetAddr.setSpan(new RelativeSizeSpan(1.3f), stop.getStreet().length(), streetAddr.length(), 0);
            }
            remoteView.setTextViewText(R.id.row_list_street, streetAddr);
            remoteView.setTextColor(R.id.row_list_street, textColor);
            remoteView.setViewVisibility(R.id.row_list_street, View.VISIBLE);
        } else {
            remoteView.setTextViewText(R.id.row_list_street, "");
            remoteView.setViewVisibility(R.id.row_list_street, View.INVISIBLE); // make it keep taking up space if not existing
        }
        if (!TextUtils.isEmpty(stop.getPlacename())) {
            remoteView.setTextViewText(R.id.row_list_placename, stop.getPlacename());
            remoteView.setTextColor(R.id.row_list_placename, textColor);
            remoteView.setViewVisibility(R.id.row_list_placename, View.VISIBLE);
        } else {
            remoteView.setViewVisibility(R.id.row_list_placename, View.GONE);
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
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }
}

