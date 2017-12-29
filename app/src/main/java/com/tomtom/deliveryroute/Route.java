package com.tomtom.deliveryroute;

import android.database.DataSetObservable;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Route
 * <p>
 * This class holds the stops on the route and can load the route.csv file
 */

public class Route extends DataSetObservable {
    private final static String TAG = "Route";
    private List<RouteStop> mRouteStops;
    private int mCurrentStopIdx;

    public Route() {
        mRouteStops = new ArrayList<>();
        mCurrentStopIdx = -1;
    }

    public Route(String filename) {
        mRouteStops = readStopsFromFile(getRouteFileName(filename));
        mCurrentStopIdx = -1;
    }

    public Route(String filename, int index) {
        mRouteStops = readStopsFromFile(getRouteFileName(filename));
        mCurrentStopIdx = index;
    }

    public void loadFromFile(String filename) {
        mRouteStops = readStopsFromFile(getRouteFileName(filename));
        mCurrentStopIdx = -1;

        notifyChanged();
    }

    public RouteStop nextStop() {
        Log.d(TAG, "> nextStop");

        if (mRouteStops != null && mRouteStops.size() <= (mCurrentStopIdx + 1)) {
            Log.w(TAG, "planRouteToNextStop(): already at last stop");
            return null;
        }

        mCurrentStopIdx++;
        RouteStop currentStop = getCurrentStop();

        notifyChanged();

        Log.d(TAG, "< nextStop");

        return currentStop;
    }

    public RouteStop prevStop() {
        Log.d(TAG, "> prevStop");

        if ((mCurrentStopIdx - 1) < 0) {
            Log.w(TAG, "planRouteToNextStop(): already at first stop");
            return null;
        }

        mCurrentStopIdx--;
        RouteStop currentStop = getCurrentStop();

        notifyChanged();

        Log.d(TAG, "< prevStop");

        return currentStop;
    }

    /**
     * Gets the current stop information
     *
     * @return : null if no route is loaded or an empty route is loaded.
     */
    public RouteStop getCurrentStop() {
        if (mRouteStops == null) {
            Log.w(TAG, "planRouteToNextStop(): no route loaded");
            return null;
        } else if (mRouteStops.size() == 0) {
            Log.w(TAG, "planRouteToNextStop(): empty route loaded");
            return null;
        }
        return mRouteStops.get(mCurrentStopIdx);
    }

    /**
     * Gets the current stop index, this can be used to restore state (reload + jump to this index)
     */
    public int getCurrentStopIndex() {
        return mCurrentStopIdx;
    }

    public void goToIndex(int index) {
        mCurrentStopIdx = index;

        notifyChanged();
    }

    public int size() {
        return mRouteStops.size();
    }

    public RouteStop getStop(int index) {
        return mRouteStops.get(index);
    }


    /**
     * Sets the done state of the stop and notifies the users of mRoute that
     * the data has changed so views can be updated.
     */
    public void setStopDone(int index, boolean done) {
        mRouteStops.get(index).setDone(done);
        notifyChanged();
    }

    /**
     * Simple method for reading a Route from a text file.
     * Every line of the file must contain a pair of latitude and longitude values expressed in degree
     * and separated by a comma character (CSV). Empty lines are ignored. Everything after lat/long is also ignored (for now)
     *
     * @return The new created Track object or null in case of an error.
     */
    private List<RouteStop> readStopsFromFile(String fileName) {
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
                    continue; // don't add it!!
                }
                if (splitline.length >= 2) {
                    final double lat = Double.parseDouble(splitline[0].trim());
                    final double lon = Double.parseDouble(splitline[1].trim());
                    stop.setLatitude(lat);
                    stop.setLongitude(lon);
                }

                // name
                if (splitline.length >= 3) {
                    final String name = splitline[2].trim();
                    stop.setName(name);
                }

                // street
                if (splitline.length >= 4) {
                    final String street = splitline[3].trim();
                    stop.setStreet(street);
                }

                // housenumber
                if (splitline.length >= 5) {
                    final String housenumber = splitline[4].trim();
                    stop.setHouseNumber(housenumber);
                }

                // placename
                if (splitline.length >= 6) {
                    final String placename = splitline[5].trim();
                    stop.setPlacename(placename);
                }

                // postal code
                if (splitline.length >= 7) {
                    final String postalCode = splitline[6].trim();
                    stop.setPostalCode(postalCode);
                }

                // special marker (? = bad address)
                if (splitline.length >= 8) {
                    final String marker = splitline[7].trim();
                    if ("?".equals(marker)) {
                        stop.setBadAddress(true);
                    }
                }

                // extra info
                if (splitline.length >= 9) {
                    final String extra = splitline[8].trim();
                    stop.setExtra(extra);
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
        return filePath + File.separator + filename;
    }
}
