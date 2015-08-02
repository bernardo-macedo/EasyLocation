package com.bmacedo.easylocation.common.events;

import android.location.Location;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public class OnUpdatedLocationObtainedEvent {

    private Location location;

    public OnUpdatedLocationObtainedEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
