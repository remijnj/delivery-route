package nl.joostremijn.deliveryroute;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tomtom.navapp.NavAppClient;
import com.tomtom.navapp.Routeable;
import com.tomtom.navapp.Trip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joost on 23-7-17.
 */

public class Route {
    private final static String TAG = "Route";
    private List<RouteStop> mRouteStops = null;
    private NavAppClient mClient;
    private RouteStop mCurrentStop = null;
    private int mCurrentStopIdx = -1;
    private Trip mTrip = null;

    public Route(String filename, NavAppClient client) {
        mClient = client;
        mRouteStops = readStopsFromFile(getRouteFileName(filename), mClient);
        mCurrentStopIdx = 0;
    }
/*
    public void showOverlay() {
        // todo : start actity wich shows a small clickable notification in the top left
        //        on top of the Navigation application. This shows the stop summary, clicking
        //        will lead to a full detail screen

        Intent intent = new Intent();

        Intent intent = new Intent(this, OverlayActivity.class);
        startActivity(intent);
    }
    */
    /**
     * Plans route to next stop
     */
    public void planRouteToNextStop() {
        Log.d(TAG, "> planRouteToNextStop");
        if (mRouteStops == null) {
            Log.w(TAG, "planRouteToNextStop(): no route loaded");
            return;
        } else if (mRouteStops.size() == 0) {
            Log.w(TAG, "planRouteToNextStop(): empty route loaded");
            return;
        } else if (mRouteStops.size() < mCurrentStopIdx + 1) {
            Log.w(TAG, "planRouteToNextStop(): already at last stop");
            return;
        }

        mCurrentStop = mRouteStops.get(mCurrentStopIdx);
        Routeable dest = mCurrentStop.getRouteable();
        mCurrentStopIdx++;

        Log.d(TAG, "planning route to [" + dest.getLatitude() + ", " + dest.getLongitude() + "]");
        Log.d(TAG, "\taddress=[" + dest.getAddress() + "]");
        Log.d(TAG, "\tname=[" + mCurrentStop.getName() + "]");

        mClient.getTripManager().planTrip(dest, mPlanListener);

        Log.d(TAG, "< planRouteToNextStop");
    }

    private Trip.PlanListener mPlanListener = new Trip.PlanListener() {
        @Override
        public void onTripPlanResult(Trip trip, Trip.PlanResult result) {
            Log.d(TAG, "onTripPlanResult result[" + result + "]");
            if (Trip.PlanResult.PLAN_OK.equals(result)) {
                mTrip = trip;
            }
        }
    };

    /**
     * Simple method for reading a Route from a text file.
     * Every line of the file must contain a pair of latitude and longitude values expressed in degree
     * and separated by a comma character (CSV). Empty lines are ignored. Everything after lat/long is also ignored (for now)
     *
     * @return The new created Track object or null in case of an error.
     */
    private List<RouteStop> readStopsFromFile(String fileName, NavAppClient client) {
        Log.d(TAG, "> readStopsFromFile");

        List<RouteStop> stops = new ArrayList<>();

        // Check the existence of the file
        final File sourceFile = new File(fileName);
        if (!sourceFile.exists()) {
            Log.e(TAG, "Source file doesn't exist [" + sourceFile.getPath() + "]");
            return null;
        }
        if (!sourceFile.isFile()) {
            Log.e(TAG, "Source file is a directory [" + sourceFile.getPath() + "]");
            return null;
        }
        if (!sourceFile.canRead()) {
            Log.e(TAG, "Source file is not readable [" + sourceFile.getPath() + "]");
            return null;
        }

        // Parse the file
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(sourceFile);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] splitline = line.split(",");
                RouteStop stop = new RouteStop();

                if (splitline.length < 2) {
                    Log.w(TAG, "line found without coordinates (< 2 elements)");
                    Log.w(TAG, "line=[" + line + "]");
                }
                if (splitline.length >= 2) {
                    final double lat = Double.parseDouble(splitline[0]);
                    final double lon = Double.parseDouble(splitline[1]);

                    // Each array contains integer elements representing latitude
                    // and longitude expressed in microdegree
                    Routeable routeable = client.makeRouteable(lat, lon);
                    stop.mStop = routeable;
                }

                if (splitline.length >= 3) {
                    final String name = splitline[2];
                    stop.mName = name;
                }

                stops.add(stop);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading input file [" + sourceFile.getPath() + "]", e);
        } finally {
            // Release of allocated resources
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing BufferedReader", e);
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing FileReader", e);
                }
            }
        }

        Log.d(TAG, "< readStopsFromFile");
        return stops;
    }

    /**
     * Return the predefined path and name for the file containing the sample track data.
     * By default, the file must be placed in the DOWNLOADS folder of the device and must be named TrackData.txt.
     */
    private String getRouteFileName(String filename) {
        final String filePath = Environment.getExternalStorageDirectory().getPath();
        //final String fileName = "route.csv";

        return filePath + File.separator + filename;
    }
}
