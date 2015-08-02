package com.bmacedo.easylocation.common.events;


import com.bmacedo.easylocation.models.LocationError;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public class OnLocationErrorEvent {

    private LocationError error;

    public OnLocationErrorEvent(LocationError error) {
        this.error = error;
    }

    public LocationError getError() {
        return error;
    }
}
