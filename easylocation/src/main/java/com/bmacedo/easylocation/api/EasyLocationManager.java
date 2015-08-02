package com.bmacedo.easylocation.api;

import android.content.Context;

import com.bmacedo.easylocation.common.events.OnInitialLocationObtainedEvent;
import com.bmacedo.easylocation.common.events.OnLocationErrorEvent;
import com.bmacedo.easylocation.common.events.OnUpdatedLocationObtainedEvent;
import com.bmacedo.easylocation.common.events.SingletonBus;
import com.bmacedo.easylocation.common.intents.BaseIntent;
import com.bmacedo.easylocation.common.intents.LocationServiceIntent;
import com.bmacedo.easylocation.controllers.services.LocationService;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;

/**
 * 
 * The instances of this class will allow to receive location updates
 * through the EasyLocationListener instance passed in the constructor.
 * 
 * In order to start receiving locations, you must call the method {@link #start()}.
 * Then, after you are done with locations, you should call {@link #stop()}.
 * 
 *
 * Created by -Bernardo on 2015-08-01.
 */
public class EasyLocationManager {

    private WeakReference<Context> context;
    private EasyLocationListener listener;
    private EasyLocationStrategy strategy;

    /**
     * This constructor sets by default the location strategy {@link EasyLocationStrategy#ANY}.
     * @param context the context used to bind the service from
     * @param listener the communication interface that allows you to receive location updates
     */
    public EasyLocationManager(Context context, EasyLocationListener listener) {
        this.context = new WeakReference<>(context);
        this.listener = listener;
        this.strategy = EasyLocationStrategy.ANY;
    }

    /**
     * This constructor lets you choose the location strategy beter suited for your needs.
     * @param context the context used to bind the service from
     * @param strategy the strategy that you want to use
     * @param listener the communication interface that allows you to receive location updates
     */
    public EasyLocationManager(Context context, EasyLocationStrategy strategy, EasyLocationListener listener) {
        this.context = new WeakReference<>(context);
        this.listener = listener;
        this.strategy = strategy;
    }

    /**
     * Call this method to start listening for location updates
     */
    public void start() {
        SingletonBus.getInstance().register(this);
        if (context != null && context.get() != null) {
            String action;
            switch (strategy) {
                case DEVICE_LOCATION:
                    action = LocationServiceIntent.ACTION_START_STRATEGY_DEVICE;
                    break;
                case GOOGLE_SERVICES:
                    action = LocationServiceIntent.ACTION_START_STRATEGY_SERVICES;
                    break;
                default:
                    action = LocationServiceIntent.ACTION_START_STRATEGY_ANY;
                    break;
            }
            BaseIntent it = new LocationServiceIntent(context.get(), LocationService.class, action);
            context.get().startService(it);
        }
    }

    /**
     * Call this method to stop receiving location updates
     */
    public void stop() {
        SingletonBus.getInstance().unregister(this);
        if (context != null && context.get() != null) {
            BaseIntent it = new LocationServiceIntent(context.get(), LocationService.class, LocationServiceIntent.ACTION_STOP);
            context.get().startService(it);
            context.clear();
        }
    }

    /**
     * <b>Do not call this method.</b>
     * This method is used internally to receive locations and forward to the listener.
     */
    @Subscribe
    public void onInitialLocationObtained(OnInitialLocationObtainedEvent event) {
        if (listener != null && context != null && context.get() != null) {
            listener.onInitialLocationObtained(event.getLocation());
        }
    }

    /**
     * <b>Do not call this method.</b>
     * This method is used internally to receive locations and forward to the listener.
     */
    @Subscribe
    public void onUpdatedLocationObtained(OnUpdatedLocationObtainedEvent event) {
        if (listener != null && context != null && context.get() != null) {
            listener.onUpdatedLocationObtained(event.getLocation());
        }
    }

    /**
     * <b>Do not call this method.</b>
     * This method is used internally to receive errors and forward to the listener.
     */
    @Subscribe
    public void onLocationError(OnLocationErrorEvent event) {
        if (listener != null && context != null && context.get() != null) {
            listener.onLocationError();
        }
    }

}
