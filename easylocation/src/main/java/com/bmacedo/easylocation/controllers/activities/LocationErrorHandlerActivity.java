package com.bmacedo.easylocation.controllers.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.bmacedo.easylocation.R;
import com.bmacedo.easylocation.common.events.OnStrategyErrorNotSolved;
import com.bmacedo.easylocation.common.events.OnStrategyErrorSolved;
import com.bmacedo.easylocation.common.events.SingletonBus;
import com.bmacedo.easylocation.common.intents.LocationErrorHandlerIntent;
import com.bmacedo.easylocation.controllers.strategies.FallbackLocationStrategy;
import com.bmacedo.easylocation.controllers.util.LocationUtil;
import com.bmacedo.easylocation.models.LocationStrategyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

/**
 * 
 * The purpose of this Activity is to present a error resolution Dialog when there is any error
 * while trying to obtain the location.
 *
 * This Activity has a transparent layout, so that the user won't notice any change between the
 * previous activity and this one.
 *
 * This Activity should be started only when the dialog should appear, and finished as soon as
 * the dialog disappears. The class LocationService controls the start of this activity, but it
 * finishes itself.
 *
 * The communication between this Activity and the LocationService instance works through EventBus
 * events <b>OnStrategyErrorSolved</b> and <b>OnStrategyErrorNotSolved</b>
 * 
 * Created by -Bernardo on 2015-07-24.
 */
public class LocationErrorHandlerActivity extends FragmentActivity {

    // Request code used for the Services error resolution activity
    private static final int REQUEST_RESOLVE_GOOGLE_SERVICES_ERROR = 1001;
    // Request code used for the device location settings activity
    private static final int REQUEST_RESOLVE_LOCATION_DISABLED = 1002;

    // State variable which avoids that the error resolution is run more than once at the same time
    private static boolean isResolvingError = false;
    // Object that contains the error and some details, if needed
    private LocationStrategyError error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationErrorHandlerIntent intent = new LocationErrorHandlerIntent(getIntent());
        this.error = intent.getError();

        if (error != null) {
            LocationStrategyError.StrategyError strategyError = error.getError();

            if (!isResolvingError) {
                if (strategyError == LocationStrategyError.StrategyError.STRATEGY_CONNECTION_FAILURE) {
                    handleConnectionFailureError(error.getErrorDetails());
                } else if (strategyError == LocationStrategyError.StrategyError.STRATEGY_DISABLED &&
                        FallbackLocationStrategy.STRATEGY_NAME.equals(error.getStrategy())) {
                    handleStrategyDisabledError();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    /**
     * 
     * Shows Dialog which warns the user that the device's location system is disabled.
     * The Dialog presents a button that takes the user to the android's location settings screen.
     *
     * When the user closes the location settings activity, this activity receives the result on
     * onActivityResult
     *
     */
    private void handleStrategyDisabledError() {
        String title = getString(R.string.fallback_error_dialog_title);
        String message = getString(R.string.fallback_error_dialog_message);
        String positiveButtonText = getString(R.string.fallback_error_dialog_positive_button_text);
        String negativeButtonText = getString(R.string.fallback_error_dialog_negative_button_text);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Base_Theme_AppCompat_Dialog);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent settings = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
                        startActivityForResult(settings, REQUEST_RESOLVE_LOCATION_DISABLED);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Error remains. Post event to LocationService.
                        postEventAndFinish(new OnStrategyErrorNotSolved());
                    }
                });
        builder.create().show();

    }

    /**
     * 
     * This method receives the connection error details and tries to execute the error resolution
     * provided by the object itself.
     *
     * The error resolution opens a Dialog that informs the user what to do to resolve the issue. Then
     * the operation result is received on onActivityResult
     * 
     */
    private void handleConnectionFailureError(Parcelable errorDetails) {
        // TODO: change errorDetails to avoid the usage of the instanceof operator. Bad smells.
        if (errorDetails != null) {
            if (errorDetails instanceof ConnectionResult) {
                ConnectionResult result = (ConnectionResult) errorDetails;
                try {
                    result.startResolutionForResult(this, REQUEST_RESOLVE_GOOGLE_SERVICES_ERROR);
                    isResolvingError = true;
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    postEventAndFinish(new OnStrategyErrorNotSolved());
                }
            } else if (errorDetails instanceof Status) {
                Status status = (Status) errorDetails;
                try {
                    status.startResolutionForResult(this, REQUEST_RESOLVE_GOOGLE_SERVICES_ERROR);
                    isResolvingError = true;
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    postEventAndFinish(new OnStrategyErrorNotSolved());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RESOLVE_GOOGLE_SERVICES_ERROR) {
            if (resultCode == RESULT_OK) {
                // Error resolved. Post event to LocationService.
                postEventAndFinish(new OnStrategyErrorSolved());
            } else {
                // Error remains. Post event to LocationService.
                postEventAndFinish(new OnStrategyErrorNotSolved());
            }
            isResolvingError = false;
        } else if (requestCode == REQUEST_RESOLVE_LOCATION_DISABLED) {
            if (LocationUtil.isLocationEnabled(getContentResolver())) {
                postEventAndFinish(new OnStrategyErrorSolved());
            } else {
                postEventAndFinish(new OnStrategyErrorNotSolved());
            }
            isResolvingError = false;
        }
    }

    private void postEventAndFinish(Object event) {
        SingletonBus.getInstance().post(event);
        finish();
    }
}
