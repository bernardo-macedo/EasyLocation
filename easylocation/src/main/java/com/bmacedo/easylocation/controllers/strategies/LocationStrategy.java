package com.bmacedo.easylocation.controllers.strategies;

import android.content.Context;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public abstract class LocationStrategy {

    // Reference to the communication interface with the manager
    private LocationStrategyManager manager;
    // App context used to start the location requests
    private Context context;

    public LocationStrategy(LocationStrategyManager manager, Context context) {
        this.manager = manager;
        this.context = context;
    }

    protected void setManager(LocationStrategyManager manager) {
        this.manager = manager;
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    protected LocationStrategyManager getManager() {
        return manager;
    }

    protected Context getContext() {
        return context;
    }

    /**
     * Method that starts the location request process
     */
    public abstract void start();

    /**
     * Method that ends the location request process
     */
    public abstract void stop();

    /**
     * Method that identifies the strategy by name
     */
    public abstract String getName();

}
