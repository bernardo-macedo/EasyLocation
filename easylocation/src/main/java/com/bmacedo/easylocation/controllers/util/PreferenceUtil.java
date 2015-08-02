package com.bmacedo.easylocation.controllers.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * Created by -Bernardo on 2015-07-22.
 */
public class PreferenceUtil {

    private static Gson gson = new Gson();

    public static void savePreference(Context context, String key, String value) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void savePreference(Context context, String key, Object value) {
        String json = gson.toJson(value);
        savePreference(context, key, json);
    }

    public static String getPreference(Context context, String key, String defaultValue) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String preference = mPrefs.getString(key, defaultValue);
        return preference;
    }

    public static <T> T getPreference(Context context, String key, Class<T> clazz) {
        if (!getPreference(context, key, "").isEmpty()) {
            return gson.fromJson(getPreference(context, key, ""), clazz);
        }
        return null;
    }
}
