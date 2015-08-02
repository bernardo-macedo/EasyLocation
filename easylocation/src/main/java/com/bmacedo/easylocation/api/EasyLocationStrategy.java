package com.bmacedo.easylocation.api;

/**
 * This is a selector for the strategy that will be used in order to obtain the location.
 * Read the javadoc of each strategy for more information.
 *
 * Created by -Bernardo on 2015-08-01.
 */
public enum EasyLocationStrategy {
    /**
     * By choosing this strategy, the location obtained will come from the Google Services Location API.
     * This is the fastest option to obtain location updates, however, if, for any reason, this API
     * is unavailable, it wont be possible to obtain locations.
     */
    GOOGLE_SERVICES,
    /**
     * By choosing this strategy, the location will be obtained by using the old Location API,
     * interaction directly with the device location providers. This option is a lot slower than
     * obtained the location using the Google Services approach, but it does not depend on any
     * third-party service or network connection.
     */
    DEVICE_LOCATION,
    /**
     * By choosing this strategy, first we will try to obtain the location using the Google Services
     * Location API. If for some reason, this API is not available, than we will try to obtain the
     * location directly from the device location providers.
     * Note that this approach might be as slow as the DEVICE_LOCATION strategy.
     */
    ANY
}
