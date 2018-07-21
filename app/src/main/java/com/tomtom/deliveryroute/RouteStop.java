package com.tomtom.deliveryroute;

import android.text.TextUtils;

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
    private boolean mDone;
    private boolean mBadAddress;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getStreet() {
        return mStreet;
    }

    public void setStreet(String street) {
        mStreet = street;
    }

    public String getPostalCode() {
        return mPostalCode;
    }

    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    public String getPlacename() {
        return mPlacename;
    }

    public void setPlacename(String placename) {
        mPlacename = placename;
    }

    public String getHouseNumber() {
        return mHouseNumber;
    }

    public void setHouseNumber(String housenumber) {
        mHouseNumber = housenumber;
    }

    public void setExtra(String name) {
        mExtra = name;
    }

    public String getExtra() {
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

    public boolean isDone() {
        return mDone;
    }

    public void setDone(boolean done) {
        mDone = done;
    }

    public boolean isBadAddress() {
        return mBadAddress;
    }

    public void setBadAddress(boolean badAddress) {
        mBadAddress = badAddress;
    }

    // maybe we should use https://github.com/googlei18n/libaddressinput here
    // See: https://stackoverflow.com/questions/11269172/address-formatting-based-on-locale-in-android
    //
    public String getStopTextUI(boolean formatUS) {
        StringBuilder stopString = new StringBuilder();
        if (!TextUtils.isEmpty(mName)) {
            stopString.append(mName);
            stopString.append("\n");
        }
        if (formatUS) {
            if (!TextUtils.isEmpty(mHouseNumber)) {
                stopString.append(mHouseNumber);
            }
            if (!TextUtils.isEmpty(mStreet)) {
                stopString.append(" ");
                stopString.append(mStreet);
            }
        } else {
            if (!TextUtils.isEmpty(mStreet)) {
                stopString.append(mStreet);
                stopString.append(" ");
            }
            if (!TextUtils.isEmpty(mHouseNumber)) {
                stopString.append(mHouseNumber);
            }
        }
        if (!TextUtils.isEmpty(mHouseNumber) || !TextUtils.isEmpty(mStreet)) {
            stopString.append("\n");
        }
        if (!TextUtils.isEmpty(mPostalCode) || !TextUtils.isEmpty(mPlacename)) {
            if (!TextUtils.isEmpty(mPostalCode)) {
                stopString.append(mPostalCode);
                stopString.append(" ");
            }
            if (!TextUtils.isEmpty(mPlacename)) {
                stopString.append(mPlacename);
            }
            stopString.append("\n");
        }
        if (!TextUtils.isEmpty(mExtra)) {
            stopString.append(mExtra);
            stopString.append("\n");
        }

        return stopString.toString();
    }
}

