package com.bmacedo.easylocation.models;

import android.os.Parcelable;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public class LocationStrategyError {

    public enum StrategyError {STRATEGY_DISABLED, STRATEGY_CONNECTION_FAILURE, UNRECOVERABLE_ERROR};

    private StrategyError error;
    private String strategy;
    private Parcelable errorDetails;

    public LocationStrategyError(StrategyError error, String strategy) {
        this.error = error;
        this.strategy = strategy;
    }

    public LocationStrategyError(StrategyError error, String strategy, Parcelable errorDetails) {
        this.error = error;
        this.strategy = strategy;
        this.errorDetails = errorDetails;
    }

    public StrategyError getError() {
        return error;
    }

    public Parcelable getErrorDetails() {
        return errorDetails;
    }

    public String getStrategy() {
        return strategy;
    }
}
