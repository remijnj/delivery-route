package com.tomtom.deliveryroute.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tomtom.deliveryroute.DeliveryApplication;
import com.tomtom.deliveryroute.R;
import com.tomtom.deliveryroute.RouteService;
import com.tomtom.deliveryroute.RouteStop;

import static com.tomtom.deliveryroute.RouteService.ROUTESTOP;


/**
 * StopFragment shows a stop.
 * It contains a "done" checkbox and a Drive button which sets this stop as the current stop
 * and plans a route there.
 * Use the {@link StopFragment#newStopFragment} factory method to
 * create an instance of this fragment.
 */
public class StopFragment extends Fragment {
    public static final java.lang.String ROUTESTOP_ID = "ROUTESTOP_ID";
    private DeliveryApplication mApplication;
    private RouteStop mStop;
    private boolean mIsCurrentDestination;
    private int mStopId;

    public StopFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param routestop_id The id of the stop.
     * @return A new instance of fragment StopFragment.
     */
    public static StopFragment newStopFragment(int routestop_id) {
        StopFragment fragment = new StopFragment();
        Bundle args = new Bundle();
        args.putSerializable(ROUTESTOP_ID, routestop_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (DeliveryApplication) getActivity().getApplication();
        if (getArguments() != null) {
            mStopId = getArguments().getInt(ROUTESTOP_ID, mApplication.mRoute.getCurrentStopIndex());
        } else {
            mStopId = mApplication.mRoute.getCurrentStopIndex();
        }
        mStop = mApplication.mRoute.getStop(mStopId);
        mIsCurrentDestination = mApplication.mRoute.getCurrentStopIndex() == mStopId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stop, container, false);

        // set stop text
        TextView text = (TextView) rootView.findViewById(R.id.text_stop);
        text.setText(mStop.getStopTextUI());

        Button button = (Button) rootView.findViewById(R.id.drive_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set the current stop to be this one
                mApplication.mRoute.goToIndex(mStopId);

                // plan the route
                Intent intent = new Intent(getActivity(), RouteService.class);
                intent.putExtra(ROUTESTOP, mStop);
                getActivity().startService(intent);
            }
        });

        if (mIsCurrentDestination) {
            button.setVisibility(View.INVISIBLE);
        }

        final CheckBox checkbox = (CheckBox)rootView.findViewById(R.id.done_checkbox);
        checkbox.setChecked(mStop.getDone());
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStop.setDone(checkbox.isChecked());
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
