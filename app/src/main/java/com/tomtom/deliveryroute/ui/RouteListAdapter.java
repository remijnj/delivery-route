package com.tomtom.deliveryroute.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;
import com.tomtom.deliveryroute.RouteStop;

/**
 * Created by remijnj on 27-12-17.
 */

class RouteListAdapter extends BaseAdapter {
    private final static String TAG = "RouteListAdapter";
    private final DeliveryApplication mApplication;
    private LayoutInflater mInflater;

    public RouteListAdapter(DeliveryApplication application) {
        super();
        mApplication = application;
        mApplication.mRoute.registerObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Log.d(TAG, "onChanged");

                super.onChanged();
                notifyDataSetChanged();
            }
        });
        mInflater = (LayoutInflater) mApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount()=" + mApplication.mRoute.size());
        return mApplication.mRoute.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RouteStop stop = mApplication.mRoute.getStop(position);
        ViewHolder holder;
        Log.d(TAG, "> getView(" + position + ")");

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_row, parent, false);

            holder = new ViewHolder();
            holder.mName = (TextView) convertView.findViewById(R.id.row_list_name);
            holder.mStreet = (TextView) convertView.findViewById(R.id.row_list_street);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Log.d(TAG, "name=" + stop.getName());
        Log.d(TAG, "street=" + stop.getStreet());
        Log.d(TAG, "housenumber=" + stop.getHouseNumber());
        holder.mName.setText(stop.getName());
        holder.mStreet.setText(stop.getStreet() + " " + stop.getHouseNumber());

        if (position == mApplication.mRoute.getCurrentStopIndex()) {
            // current stop has to be highlighted
            holder.mName.setTextColor(Color.GREEN);
            holder.mName.setTextSize(30);
            holder.mStreet.setTextColor(Color.GREEN);
            holder.mStreet.setTextSize(30);
        }

        Log.d(TAG, "< getView");
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView mName;
        TextView mStreet;
    }
}
