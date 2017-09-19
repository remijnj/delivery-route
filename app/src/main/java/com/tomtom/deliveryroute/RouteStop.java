package com.tomtom.deliveryroute;

import java.io.Serializable;

/**
 * Created by joost on 23-7-17.
 */

public class RouteStop implements Serializable {
    private static final long serialVersionUID = 123822938793792L;
    private String mName;
    private String mStreet;
    private String mHouseNumber;
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
}

