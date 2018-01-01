package com.tomtom.deliveryroute.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private final static String TAG = "ListFragment";

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        ListView list = (ListView) rootView.findViewById(R.id.listView);
        RouteListAdapter adapter = new RouteListAdapter((DeliveryApplication) getActivity().getApplication());
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick(): position=" + position);

        // Start the detail view
        Intent stopActivityIntent = new Intent(getActivity(), StopActivity.class);
        //stopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stopActivityIntent.putExtra(StopFragment.ROUTESTOP_ID, position);

        getActivity().startActivity(stopActivityIntent);
    }
}
