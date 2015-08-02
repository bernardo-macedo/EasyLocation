package com.bmacedo.easylocation.controllers.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bmacedo.easylocation.common.events.OnInitialLocationObtainedEvent;
import com.bmacedo.easylocation.common.events.OnLocationErrorEvent;
import com.bmacedo.easylocation.common.events.OnStrategyErrorNotSolved;
import com.bmacedo.easylocation.common.events.OnStrategyErrorSolved;
import com.bmacedo.easylocation.common.events.OnUpdatedLocationObtainedEvent;
import com.bmacedo.easylocation.common.events.SingletonBus;
import com.bmacedo.easylocation.common.intents.LocationErrorHandlerIntent;
import com.bmacedo.easylocation.common.intents.LocationServiceIntent;
import com.bmacedo.easylocation.controllers.activities.LocationErrorHandlerActivity;
import com.bmacedo.easylocation.controllers.strategies.FallbackLocationStrategy;
import com.bmacedo.easylocation.controllers.strategies.LocationStrategy;
import com.bmacedo.easylocation.controllers.strategies.LocationStrategyManager;
import com.bmacedo.easylocation.controllers.strategies.ServicesLocationStrategy;
import com.bmacedo.easylocation.controllers.util.PreferenceUtil;
import com.bmacedo.easylocation.models.LocationError;
import com.bmacedo.easylocation.models.LocationModel;
import com.bmacedo.easylocation.models.LocationState;
import com.bmacedo.easylocation.models.LocationStrategyError;
import com.squareup.otto.Subscribe;

/**
 *
 * This class provides the location retrieval service.
 *
 * Any class that starts this service should listen to the events:
 *
 * <ul>
 * <li>OnInitialLocationObtainedEvent</li>
 * <li>OnUpdatedLocationObtainedEvent</li>
 * <li>OnLocationErrorEvent</li>
 * </ul>
 *
 * Depending on the strategy used, it will use Google Services, the device location providers or
 * both in order to obtain the location.
 *
 * The fastest approach is to use the Google Services strategy.
 *
 * Created by -Bernardo on 2015-07-22.
 */
public class LocationService extends Service implements LocationStrategyManager {

    private static final String STATE_PREFERENCE_KEY = "caronaphone_location_state";
    private static final String LOCATION_PREFERENCE_KEY = "caronaphone_location_data";
    private static final long MAX_TIME_LOCATION_INTERVAL = 1 * 60 * 1000;  // 1 minute

    // Object that maintains the state of the strategy management
    private LocationState state;
    // Object that references the current strategy
    private LocationStrategy strategy;
    // Object that stores the last obtained location
    private Location location;
    // Variable that indicates if the service has been requested to stop
    private boolean isStopped;

    private boolean isFallbackEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
        SingletonBus.getInstance().register(this);
        isStopped = true;
        isFallbackEnabled = true;

        // Initialize state
        if (!PreferenceUtil.getPreference(this, STATE_PREFERENCE_KEY, "").isEmpty()) {
            state = LocationState.valueOf(PreferenceUtil.getPreference(this, STATE_PREFERENCE_KEY, ""));
        } else {
            state = LocationState.IDLE;
        }

        // Initialize strategy
        strategy = ServicesLocationStrategy.getInstance(this, this.getApplicationContext());

        // Initialize location. If no location was previously set, getPreference returns null.
        LocationModel locationModel = PreferenceUtil.getPreference(this, LOCATION_PREFERENCE_KEY, LocationModel.class);
        if (locationModel != null) {
            location = locationModel.getLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SingletonBus.getInstance().unregister(this);
        if (state == LocationState.ABORTING_LOCATION_UPDATE) {
            // Change state to DONE in order to avoid that the state gets stuck in ABORTING
            state = LocationState.DONE;
        }
        PreferenceUtil.savePreference(this, STATE_PREFERENCE_KEY, state.name());
        if (location != null) {
            PreferenceUtil.savePreference(this, LOCATION_PREFERENCE_KEY, new LocationModel(location));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent baseIntent, int flags, int startId) {
        handleAction(baseIntent.getAction());
        // The service continues to execute while not called again with action ACTION_STOP
        return START_STICKY;
    }

    private void handleAction(String action) {
        switch (action) {
            case LocationServiceIntent.ACTION_START_STRATEGY_ANY:
                strategy = ServicesLocationStrategy.getInstance(this, this.getApplicationContext());
                isFallbackEnabled = true;
                startStrategy();
                break;
            case LocationServiceIntent.ACTION_START_STRATEGY_SERVICES:
                strategy = ServicesLocationStrategy.getInstance(this, this.getApplicationContext());
                isFallbackEnabled = false;
                startStrategy();
                break;
            case LocationServiceIntent.ACTION_START_STRATEGY_DEVICE:
                strategy = FallbackLocationStrategy.getInstance(this, this.getApplicationContext());
                isFallbackEnabled = true;
                startStrategy();
                break;
            case LocationServiceIntent.ACTION_STOP:
                isStopped = true;
                if (state == LocationState.WAITING_UPDATED_LOCATION) {
                    state = LocationState.ABORTING_LOCATION_UPDATE;
                }
                strategy.stop();
                stopSelf();
                break;
            default:
                throw new UnsupportedOperationException("LocationService deve receber uma Intent do tipo LocationServiceIntent");
        }
    }

    private void startStrategy() {
        isStopped = false;
        if (location != null) {
            // If there is a cached location, sends it even before starting the strategy
            if (System.currentTimeMillis() - location.getTime() > MAX_TIME_LOCATION_INTERVAL) {
                state = LocationState.WAITING_UPDATED_LOCATION;
                SingletonBus.getInstance().post(new OnInitialLocationObtainedEvent(location));
            } else {
                state = LocationState.DONE;
                SingletonBus.getInstance().post(new OnUpdatedLocationObtainedEvent(location));
            }
        }
        strategy.start();
    }


    @Override
    public void onLocationObtained(Location location) {
        if (!isStopped) {
            this.location = location;
            if (state == LocationState.WAITING_INITIAL_LOCATION || state == LocationState.IDLE) {
                // Se recebeu a localizacao quando o estado eh IDLE ou WAITING INITIAL
                state = LocationState.WAITING_UPDATED_LOCATION;
                SingletonBus.getInstance().post(new OnInitialLocationObtainedEvent(location));
            } else {
                // Senao
                if (state != LocationState.ABORTING_LOCATION_UPDATE) {
                    state = LocationState.DONE;
                    SingletonBus.getInstance().post(new OnUpdatedLocationObtainedEvent(location));
                }
            }
        }
    }

    @Override
    public void onStrategyError(LocationStrategyError locationStrategyError) {
        if (!isStopped) {
            switch (locationStrategyError.getError()) {
                case STRATEGY_CONNECTION_FAILURE:
                    if (locationStrategyError.getErrorDetails() != null) {
                        tryToResolveError(locationStrategyError);
                    } else {
                        // If error details were not provided then the error has no resolution
                        handleUnrecoverableError();
                    }
                    break;
                case STRATEGY_DISABLED:
                    strategy.stop();
                    if (isFallbackEnabled && strategy.getName().equals(ServicesLocationStrategy.STRATEGY_NAME)) {
                        strategy = FallbackLocationStrategy.getInstance(this, this.getApplicationContext());
                        strategy.start();
                    } else {
                        tryToResolveError(locationStrategyError);
                    }
                    break;
                case UNRECOVERABLE_ERROR:
                    handleUnrecoverableError();
                    break;
                default:
                    throw new RuntimeException("Sistema de localizacao retornou um erro inesperado: " + locationStrategyError);
            }
        }
    }

    private void handleUnrecoverableError() {
        if (!isStopped) {
            strategy.stop();
            if (isFallbackEnabled && strategy.getName().equals(ServicesLocationStrategy.STRATEGY_NAME)) {
                strategy = FallbackLocationStrategy.getInstance(this, this.getApplicationContext());
                strategy.start();
            } else {
                state = LocationState.UNRECOVERABLE_ERROR;
                SingletonBus.getInstance().post(new OnLocationErrorEvent(LocationError.UNRECOVERABLE_ERROR));
            }
        }
    }

    private void tryToResolveError(LocationStrategyError error) {
        if (!isStopped) {
            LocationErrorHandlerIntent it = new LocationErrorHandlerIntent(this, LocationErrorHandlerActivity.class,
                    LocationErrorHandlerIntent.HANDLE_ERROR_ACTION, error);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        }
    }

    @Subscribe
    public void onStrategyErrorSolved(OnStrategyErrorSolved event) {
        if (!isStopped) {
            strategy.start();
        }
    }

    @Subscribe
    public void onStrategyErrorNotSolved(OnStrategyErrorNotSolved event) {
        if (!isStopped) {
            handleUnrecoverableError();
        }
    }

}
