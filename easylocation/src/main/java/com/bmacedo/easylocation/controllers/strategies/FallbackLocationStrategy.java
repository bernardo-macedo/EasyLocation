package com.bmacedo.easylocation.controllers.strategies;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.bmacedo.easylocation.controllers.util.LocationUtil;
import com.bmacedo.easylocation.models.LocationStrategyError;


/**
 * 
 * This class represents the strategy used to obtain the location when Google Services
 * is not available.
 *
 * It uses the old location API provided directly by the Android system in order to
 * communicate directly with the location providers (GPS, WIFI and NETWORK).
 *
 * Using this strategy is much slower than the Google Services approach, but it can be
 * useful for some cases.
 *
 * Created by -Bernardo on 2015-07-22.
 */
public class FallbackLocationStrategy extends LocationStrategy implements LocationListener {

    public static final String STRATEGY_NAME = "Fallback_Strategy";

    // Object that stores the unique instance of this class
    private static FallbackLocationStrategy instance;
    // The object that provides the location API
    private LocationManager androidLocationManager;
    // Indicator of the best location provider (GPS, WIFI or NETWORK)
    private String bestProvider;
    // Object that stores the criteria of the location to be requested
    private Criteria criteria;
    // Variable that identifies when requested to stop
    private boolean isStopped = false;

    private FallbackLocationStrategy(LocationStrategyManager manager, Context context) {
        super(manager, context);

        androidLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
    }

    public static FallbackLocationStrategy getInstance(LocationStrategyManager manager, Context context) {
        if (instance == null) {
            instance = new FallbackLocationStrategy(manager, context);
        } else {
            instance.setManager(manager);
            instance.setContext(context);
        }
        return instance;
    }

    @Override
    public void start() {
        isStopped = false;
        if (findBestProvider()) {
            Location location = androidLocationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                getManager().onLocationObtained(location);
            }
            androidLocationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
        }
    }

    @Override
    public void stop() {
        isStopped = true;
        androidLocationManager.removeUpdates(this);
    }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public void onLocationChanged(Location location) {
        getManager().onLocationObtained(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        restartLocationUpdates();
    }

    @Override
    public void onProviderEnabled(String provider) {
        restartLocationUpdates();
    }

    @Override
    public void onProviderDisabled(String provider) {
        restartLocationUpdates();
    }

    private void restartLocationUpdates() {
        if (!isStopped && findBestProvider()) {
            androidLocationManager.removeUpdates(this);
            androidLocationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
        }
    }

    private boolean findBestProvider() {
        if (LocationUtil.isLocationEnabled(getContext().getContentResolver())) {
            bestProvider = androidLocationManager.getBestProvider(criteria, true);
            if (bestProvider != null && !bestProvider.isEmpty()) {
                return true;
            }
        }
        // Gets here only if !isLocationEnabled or bestProvider == null
        getManager().onStrategyError(new LocationStrategyError(LocationStrategyError.StrategyError.STRATEGY_DISABLED, getName()));
        return false;
    }
}
