package com.tomtom.deliveryroute.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
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
 * RouteListAdapter is the data provider for the main screen's listview.
 *
 */

class RouteListAdapter extends BaseAdapter {
    private final static String TAG = "RouteListAdapter";
    private final DeliveryApplication mApplication;
    private LayoutInflater mInflater;

    RouteListAdapter(DeliveryApplication application) {
        super();
        mApplication = application;
        DeliveryApplication.getRoute().registerObserver(new DataSetObserver() {
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
        Log.d(TAG, "getCount()=" + DeliveryApplication.getRoute().size());
        return DeliveryApplication.getRoute().size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RouteStop stop = DeliveryApplication.getRoute().getStop(position);
        boolean formatUS = mApplication.getFormatUS();
        ViewHolder holder;
        Log.d(TAG, "> getView(" + position + ")");

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_row, parent, false);

            holder = new ViewHolder();
            holder.mName = (TextView) convertView.findViewById(R.id.row_list_name);
            holder.mStreet = (TextView) convertView.findViewById(R.id.row_list_street);
            holder.mPlacename = (TextView) convertView.findViewById(R.id.row_list_placename);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int textColor = Color.LTGRAY;
        if (position == DeliveryApplication.getRoute().getCurrentStopIndex()) {
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
            holder.mName.setText(stop.getName());
            holder.mName.setTextColor(textColor);
            holder.mName.setVisibility(View.VISIBLE);
        } else {
            holder.mName.setVisibility(View.GONE);
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
            holder.mStreet.setText(streetAddr);
            holder.mStreet.setTextColor(textColor);
            holder.mStreet.setVisibility(View.VISIBLE);
        } else {
            holder.mStreet.setText("");
            holder.mStreet.setVisibility(View.INVISIBLE); // make it keep taking up space if not existing
        }

        if (!TextUtils.isEmpty(stop.getPlacename())) {
            holder.mPlacename.setText(stop.getPlacename());
            holder.mPlacename.setVisibility(View.VISIBLE);
            holder.mPlacename.setTextColor(textColor);
        } else {
            holder.mPlacename.setVisibility(View.GONE);
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
        TextView mPlacename;
    }
}
