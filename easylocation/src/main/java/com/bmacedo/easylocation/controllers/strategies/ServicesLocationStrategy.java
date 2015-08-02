package com.bmacedo.easylocation.controllers.strategies;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.bmacedo.easylocation.models.LocationStrategyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

/**
 *
 * This class encapsulates the connection, the request and the retrieval of locations
 * via Google Services. However, it does not recover from errors, it just delegates
 * any erros to the LocationStrategyManager
 *
 * Before trying to obtain the location, this class uses the SettingAPI to find out if
 * the kind of location requested is possible to obtain given the current settings. An
 * error is sent if not possible.
 *
 * The error type
 * {@link com.bmacedo.easylocation.models.LocationStrategyError.StrategyError#STRATEGY_CONNECTION_FAILURE}
 * occurs when:
 * <ol>
 *     <li>It wasn't possible to connect to Google Services, but some user action might fix it</li>
 *     <li>It was possible to connect to Google Services, but the location settings is disabled</li>
 * </ol>
 *
 * The error type
 * {@link com.bmacedo.easylocation.models.LocationStrategyError.StrategyError#UNRECOVERABLE_ERROR}
 * occurs when there is no available solution.
 *
 * The error type
 * {@link com.bmacedo.easylocation.models.LocationStrategyError.StrategyError#STRATEGY_DISABLED}
 * occurs when the device has the location settings enabled, but the user has not allowed Google
 * Services to use his or her location.
 *
 * Created by -Bernardo on 2015-07-22.
 */
public class ServicesLocationStrategy extends LocationStrategy implements com.google.android.gms.location.LocationListener {

    public static final String STRATEGY_NAME = "Google_Services_Strategy";

    // Object that stores the reference to the unique instance of this class
    private static ServicesLocationStrategy instance;
    // This variable indicates when the class is waiting for some result.
    // It avoid that some request is activated more than once while a response is not received.
    private static boolean isWaitingForStatus = false;
    // Object that allows the use of Google Services API
    private GoogleApiClient googleApiClient;
    // Object that stores info about the needed location
    private LocationRequest locationRequest;
    // Object that manages location requests. Needed for the Settings API.
    private LocationSettingsRequest.Builder locationRequestBuilder;
    // Instance of the internal class that manages the connection with Google Services
    private LocationServicesConnectionListener connectionListener;

    private ServicesLocationStrategy(LocationStrategyManager manager, Context context) {
        super(manager, context);

        // Initialize Google Services Connection Listener
        connectionListener = new LocationServicesConnectionListener();

        // Create a GoogleApiClient instance
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionListener)
                .addOnConnectionFailedListener(connectionListener)
                .build();

        // Initialize Location services variables
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequestBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
    }

    public static ServicesLocationStrategy getInstance(LocationStrategyManager manager, Context context) {
        if (instance == null) {
            instance = new ServicesLocationStrategy(manager, context);
        } else {
            instance.setManager(manager);
            instance.setContext(context);
        }
        return instance;
    }

    /**
     * This method is called by {@link LocationStrategyManager} in order to start the process of
     * location requesting.
     */
    @Override
    public void start() {
        if (googleApiClient.isConnected()) {
            checkLocationSettingsAndStartPeriodicUpdates();
        } else if (!googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    /**
     * This method finishes the location requests.
     */
    @Override
    public void stop() {
        stopPeriodicUpdates();
        googleApiClient.disconnect();
    }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    /**
     * This method is called by the Google Services API when a new location is available
     */
    @Override
    public void onLocationChanged(Location location) {
        getManager().onLocationObtained(location);
    }

    /**
     *
     * This method is responsible to use the SettingsAPI to identify the possibility of obtaining
     * the requested location.
     *
     * If successful, then the method {@link #startPeriodicUpdates()} is called
     *
     * If not, the error is delegated to the {@link LocationStrategyManager}.
     *
     * Note that this method should not be called before the Google Services connection is established
     *
     */
    private void checkLocationSettingsAndStartPeriodicUpdates() {
        if (googleApiClient.isConnected()) {
            if (!isWaitingForStatus) {
                isWaitingForStatus = true;
                final PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationRequestBuilder.build());

                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult locationSettingsResult) {
                        final Status status = locationSettingsResult.getStatus();
                        if (status.isSuccess()) {
                            isWaitingForStatus = false;
                            startPeriodicUpdates();
                        } else {
                            handleFailure(status);
                        }
                    }
                });
            }
        } else {
            throw new RuntimeException("O ServicesLocationStrategy nao tem como checar " +
                    "a configuracao da localizacao antes de efetivar a conexao com o GoogleApiClient");
        }
    }

    /**
     * This method start the request for locations using the FusedLocation API. Then it returns the
     * last known location.
     *
     * If an error occurs while requesting the location updates, an error type
     * {@link com.bmacedo.easylocation.models.LocationStrategyError.StrategyError#STRATEGY_CONNECTION_FAILURE}
     * is delegated
     *
     * If an error occurs while trying to obtain the last known location, the user has not allowed
     * Google Services to use his or her location, therefore, an error type
     * {@link com.bmacedo.easylocation.models.LocationStrategyError.StrategyError#STRATEGY_DISABLED}
     * is delegated.
     *
     * Note that this method should not be called before the Google Services connection is established
     *
     */
    private void startPeriodicUpdates() {
        if (googleApiClient.isConnected()) {
            if (!isWaitingForStatus) {
                isWaitingForStatus = true;
                // Inicia request por localizacoes atualizadas
                PendingResult<Status> result = LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this);

                result.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        isWaitingForStatus = false;
                        if (!status.isSuccess()) {
                            handleFailure(status);
                        }
                    }
                });

                // Envia ultima localizacao disponivel
                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    getManager().onLocationObtained(location);
                }
            }
        } else {
            throw new RuntimeException("O ServicesLocationStrategy nao tem como iniciar " +
                    "a obtencao da localizacao antes de efetivar a conexao com o GoogleApiClient");
        }
    }

    private void handleFailure(Status status) {
        isWaitingForStatus = false;
        if (status != null && !status.isSuccess()) {
            if (status.hasResolution()) {
                getManager().onStrategyError(
                        new LocationStrategyError(LocationStrategyError.StrategyError.STRATEGY_CONNECTION_FAILURE, getName(), status));
            } else {
                getManager().onStrategyError(
                        new LocationStrategyError(LocationStrategyError.StrategyError.UNRECOVERABLE_ERROR, getName()));
            }
        }
    }

    private void stopPeriodicUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    /**
     * Internal class that manages the connection with Google Services.
     */
    private class LocationServicesConnectionListener implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnected(Bundle bundle) {
            checkLocationSettingsAndStartPeriodicUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            getManager().onStrategyError(new LocationStrategyError(LocationStrategyError.StrategyError.STRATEGY_CONNECTION_FAILURE, getName()));
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if (connectionResult.hasResolution()) {
                getManager().onStrategyError(
                        new LocationStrategyError(LocationStrategyError.StrategyError.STRATEGY_CONNECTION_FAILURE, getName(), connectionResult));
            } else {
                getManager().onStrategyError(new LocationStrategyError(LocationStrategyError.StrategyError.UNRECOVERABLE_ERROR, getName()));
            }
        }
    }
}
