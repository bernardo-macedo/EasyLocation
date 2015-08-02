package com.bmacedo.easylocation.api;

import android.location.Location;

/**
 * This interface must be implemented by any class that wants to obtain the user location
 * from the EasyLocationManager.
 *
 * Created by -Bernardo on 2015-08-01.
 */
public interface EasyLocationListener {
    /**
     * This method will be called at most once. It will deliver the last known location
     * even if the location is an old one.
     * @param location the last known location
     */
    public void onInitialLocationObtained(Location location);

    /**
     * This method will be called multiple times, as long as the service is running. It
     * will retrieve an updated location of the user.
     * @param location updated location
     */
    public void onUpdatedLocationObtained(Location location);

    /**
     * This method will be called once the location strategy used fails.
     * When this happens, it means that the user chose to not enable the location
     * system or, if using the Google Services strategy, the connection with the
     * API could not be established.
     */
    public void onLocationError();
}
