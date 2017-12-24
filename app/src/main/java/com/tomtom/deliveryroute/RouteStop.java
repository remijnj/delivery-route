package com.tomtom.deliveryroute;

import java.io.Serializable;

/**
 * RouteStop
 * Contains all info about the stop on the route. This holds lat/long. All other fields are
 * for display purposes only.
 */

public class RouteStop implements Serializable {
    private static final long serialVersionUID = 2L;
    private String mName;
    private String mStreet;
    private String mHouseNumber;
    private String mPostalCode;
    private String mPlacename;
    private String mExtra;
    private double mLatitude;
    private double mLongitude;

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setStreet(String street) {
        mStreet = street;
    }

    public String getStreet() {
        return mStreet;
    }

    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    public String getPostalCode() {
        return mPostalCode;
    }
    public void setPlacename(String placename) {
        mPlacename = placename;
    }

    public String getPlacename() {
        return mPlacename;
    }

    public void setHouseNumber(String housenumber) {
        mHouseNumber = housenumber;
    }

    public String getHouseNumber() {
        return mHouseNumber;
    }

    public void setExtra(String name) {
        mExtra = name;
    }

    public String getExra() {
        return mExtra;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double lat) {
        mLatitude = lat;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double lon) {
        mLongitude = lon;
    }


    // maybe we should use https://github.com/googlei18n/libaddressinput here
    //
    public String getStopTextUI() {
        StringBuilder stopString = new StringBuilder();
        if (mName != null) {
            stopString.append(mName);
        }
        if (mStreet != null) {
            stopString.append("\n");
            stopString.append(mStreet);
        }
        if (mHouseNumber != null) {
            stopString.append(" ");
            stopString.append(mHouseNumber);
        }
        if (mPostalCode != null) {
            stopString.append("\n");
            stopString.append(mPostalCode);
        }
        if (mPlacename != null) {
            stopString.append(" ");
            stopString.append(mPlacename);
        }
        if (mExtra != null) {
            stopString.append("\n");
            stopString.append(mExtra);
        }

        return stopString.toString();
    }
}

