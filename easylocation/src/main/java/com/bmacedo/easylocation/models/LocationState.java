package com.bmacedo.easylocation.models;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public enum LocationState {
    IDLE, WAITING_INITIAL_LOCATION, WAITING_UPDATED_LOCATION,
    RECOVERING_FROM_ERROR, ABORTING_LOCATION_UPDATE,
    UNRECOVERABLE_ERROR, DONE
}
