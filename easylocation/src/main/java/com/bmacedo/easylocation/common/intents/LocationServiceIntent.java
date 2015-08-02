package com.bmacedo.easylocation.common.intents;

import android.content.Context;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public class LocationServiceIntent extends BaseIntent {

    public static final String ACTION_START_STRATEGY_ANY = "LocationService_Start_Any";
    public static final String ACTION_START_STRATEGY_SERVICES = "LocationService_Start_Services";
    public static final String ACTION_START_STRATEGY_DEVICE = "LocationService_Start_Device";
    public static final String ACTION_STOP = "LocationService_Stop";

    public LocationServiceIntent(Context context, Class<?> clazz, String action) {
        super(context, clazz, action);
    }

    @Override
    protected String[] getPossibleActions() {
        return new String[] {
                ACTION_START_STRATEGY_ANY,
                ACTION_START_STRATEGY_SERVICES,
                ACTION_START_STRATEGY_DEVICE,
                ACTION_STOP
        };
    }
}
