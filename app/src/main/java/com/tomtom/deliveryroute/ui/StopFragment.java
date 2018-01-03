package com.tomtom.deliveryroute.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
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
    private static final String TAG = "StopFragment";
    private DeliveryApplication mApplication;
    private RouteStop mStop;
    private boolean mIsCurrentDestination;
    private int mStopId;
    private View mRootView;

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
            mStopId = getArguments().getInt(ROUTESTOP_ID, mApplication.getRoute().getCurrentStopIndex());
        } else {
            mStopId = mApplication.getRoute().getCurrentStopIndex();
        }
        mStop = mApplication.getRoute().getStop(mStopId);
        mIsCurrentDestination = mApplication.getRoute().getCurrentStopIndex() == mStopId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_stop, container, false);

        Button driveButton = (Button) mRootView.findViewById(R.id.drive_button);
        driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set the current stop to be this one
                mApplication.getRoute().goToIndex(mStopId);
                mIsCurrentDestination = true;

                // plan the route
                Intent intent = new Intent(getActivity(), RouteService.class);
                intent.putExtra(ROUTESTOP, mStop);
                getActivity().startService(intent);

                updateUI();
            }
        });

        AppCompatImageButton prevButton = (AppCompatImageButton) mRootView.findViewById(R.id.previous_button);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "pressed prev");

                // Go to previous stop
                if (mStopId > 0) {
                    mStopId--;
                    mStop = mApplication.getRoute().getStop(mStopId);
                    mIsCurrentDestination = (mApplication.getRoute().getCurrentStopIndex() == mStopId);
                } else {
                    Log.w(TAG, "not going to previous stop, we are already on first");
                }

                // Update the UI
                updateUI();
            }
        });

        AppCompatImageButton nextButton = (AppCompatImageButton) mRootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "pressed next");

                // Go to next stop
                if (mStopId + 1 < mApplication.getRoute().size()) {
                    mStopId++;
                    mStop = mApplication.getRoute().getStop(mStopId);
                    mIsCurrentDestination = (mApplication.getRoute().getCurrentStopIndex() == mStopId);
                } else {
                    Log.w(TAG, "not going to next stop, we are already on last");
                }

                // Update the UI
                updateUI();
            }
        });

        final CheckBox checkbox = (CheckBox) mRootView.findViewById(R.id.done_checkbox);
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApplication.getRoute().setStopDone(mStopId, checkbox.isChecked());
            }
        });

        updateUI();

        return mRootView;
    }

    private void updateUI() {
        // set stop text
        TextView text = (TextView) mRootView.findViewById(R.id.text_stop);
        text.setText(mStop.getStopTextUI(mApplication.getFormatUS()));

        // only show the Drive button when this is not yet the current destination
        Button driveButton = (Button) mRootView.findViewById(R.id.drive_button);
        if (mIsCurrentDestination || mStop.isBadAddress()) {
            driveButton.setVisibility(View.INVISIBLE);
        } else {
            driveButton.setVisibility(View.VISIBLE);
        }

        // show special text for current destination and bad address
        TextView bottomText = (TextView) mRootView.findViewById(R.id.bottom_text);
        if (mIsCurrentDestination) {
            bottomText.setText(R.string.current_stop);
            bottomText.setTextColor(Color.GREEN);
            bottomText.setVisibility(View.VISIBLE);
        } else if (mStop.isBadAddress()) {
            bottomText.setText(R.string.bad_address);
            bottomText.setTextColor(Color.RED);
            bottomText.setVisibility(View.VISIBLE);
        } else {
            bottomText.setVisibility(View.INVISIBLE);
        }


        // only show the Prev button if we are not already on the first stop
        AppCompatImageButton prevButton = (AppCompatImageButton) mRootView.findViewById(R.id.previous_button);
        if (mStopId <= 0) {
            prevButton.setVisibility(View.INVISIBLE);
        } else {
            prevButton.setVisibility(View.VISIBLE);
        }

        // only show the Next button when this is not the last stop
        AppCompatImageButton nextButton = (AppCompatImageButton) mRootView.findViewById(R.id.next_button);
        if (mStopId == (mApplication.getRoute().size() - 1)) {
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }

        // Set the checkbox correctly
        final CheckBox checkbox = (CheckBox) mRootView.findViewById(R.id.done_checkbox);
        checkbox.setChecked(mStop.isDone());
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
