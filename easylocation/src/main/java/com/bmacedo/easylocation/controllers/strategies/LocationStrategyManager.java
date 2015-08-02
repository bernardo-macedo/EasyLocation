package com.bmacedo.easylocation.controllers.strategies;

import android.location.Location;

import com.bmacedo.easylocation.models.LocationStrategyError;


/**
 * 
 * Interface that should be implemented by the class responsible for managing the strategies
 * for location requesting
 *
 * Created by -Bernardo on 2015-07-22.
 */
public interface LocationStrategyManager {

    void onLocationObtained(Location location);

    void onStrategyError(LocationStrategyError error);

}
