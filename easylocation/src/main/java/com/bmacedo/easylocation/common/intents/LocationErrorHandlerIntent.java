package com.bmacedo.easylocation.common.intents;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.bmacedo.easylocation.models.LocationStrategyError;


/**
 * Created by -Bernardo on 2015-07-24.
 */
public class LocationErrorHandlerIntent extends BaseIntent {

    public static final String HANDLE_ERROR_ACTION = "Location_Error_Handler_Handle_Error";

    private static final String STRATEGY_ERROR_KEY = "Location_Error_Strategy_Error";
    private static final String ERROR_DETAILS_KEY = "Location_Error_Handler_Error_Details";
    private static final String STRATEGY_NAME_KEY = "Location_Error_Strategy_Strategy_Name";

    public LocationErrorHandlerIntent(Context context, Class<?> clazz, String action, LocationStrategyError error) {
        super(context, clazz, action);
        putExtra(STRATEGY_ERROR_KEY, error.getError());
        putExtra(ERROR_DETAILS_KEY, error.getErrorDetails());
        putExtra(STRATEGY_NAME_KEY, error.getStrategy());

    }

    public LocationErrorHandlerIntent(Intent original) {
        super(original);
    }

    @Override
    protected String[] getPossibleActions() {
        return new String[]{HANDLE_ERROR_ACTION};
    }

    public LocationStrategyError getError() {
        LocationStrategyError.StrategyError strategyError = (LocationStrategyError.StrategyError) getSerializableExtra(STRATEGY_ERROR_KEY);
        Parcelable errorDetails = getParcelableExtra(ERROR_DETAILS_KEY);
        String strategyName = getStringExtra(STRATEGY_NAME_KEY);
        return new LocationStrategyError(strategyError, strategyName, errorDetails);
    }
}
