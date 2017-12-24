package com.tomtom.deliveryroute;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static com.tomtom.deliveryroute.RouteService.ROUTESTOP;


/**
 * StopFragment shows a stop.
 * It contains a "done" checkbox and a Drive button which sets this stop as the current stop
 * and plans a route there.
 * Use the {@link StopFragment#newStopFragment} factory method to
 * create an instance of this fragment.
 */
public class StopFragment extends Fragment implements Button.OnClickListener {
    public static final java.lang.String ROUTESTOP_ID = "ROUTESTOP_ID";
    private DeliveryApplication mApplication;
    private RouteStop mStop;
    private boolean mIsCurrentDestination;
    private int mStopId;

    //private OnFragmentInteractionListener mListener;

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
        mApplication = (DeliveryApplication)getActivity().getApplication();
        if (getArguments() != null) {
            mStopId = getArguments().getInt(ROUTESTOP_ID);
            mStop = mApplication.mRoute.getStop(mStopId);
            mIsCurrentDestination = mApplication.mRoute.getCurrentStopIndex() == mStopId;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stop, container, false);

        // set stop text
        TextView text = (TextView)rootView.findViewById(R.id.text_stop);
        text.setText(mStop.getStopTextUI());

        Button button = (Button)rootView.findViewById(R.id.drive_button);
        button.setOnClickListener(this);
        if (mIsCurrentDestination) {
            button.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    public void onClick(View view) {
        // set the current stop to be this one
        mApplication.mRoute.goToIndex(mStopId);

        // plan the route
        Intent intent = new Intent(getActivity(), RouteService.class);
        intent.putExtra(ROUTESTOP, mStop);
        getActivity().startService(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    //public interface OnFragmentInteractionListener {
    //    // TODO: Update argument type and name
    //    void onFragmentInteraction(Uri uri);
    //}
}
