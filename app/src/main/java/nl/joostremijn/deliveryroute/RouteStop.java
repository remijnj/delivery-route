package nl.joostremijn.deliveryroute;

import com.tomtom.navapp.Routeable;

/**
 * Created by joost on 23-7-17.
 */

public class RouteStop {
    public Routeable mStop;
    public String mName;

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setRouteable(Routeable stop) {
        mStop = stop;
    }

    public Routeable getRouteable() {
        return mStop;
    }
}

