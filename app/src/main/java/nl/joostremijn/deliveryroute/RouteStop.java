package nl.joostremijn.deliveryroute;

import com.tomtom.navapp.Routeable;

/**
 * Created by joost on 23-7-17.
 */

public class RouteStop {
    private Routeable mStop;
    private String mName;
    private String mAddress;
    private String mExtra;

    public void setName(String name) {
        mName = name;
    }
    public String getName() {
        return mName;
    }

    public void setAddress(String name) {
        mAddress = name;
    }
    public String getAddress() {
        return mAddress;
    }

    public void setExtra(String name) {
        mExtra = name;
    }
    public String getExra() {
        return mExtra;
    }

    public void setRouteable(Routeable stop) {
        mStop = stop;
    }
    public Routeable getRouteable() {
        return mStop;
    }
}

