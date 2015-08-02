package com.bmacedo.easylocation.models;

import android.location.Location;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * 
 * This class creates a copy of an {@link Location} object in order to Serialize it inside the
 * SharedPreferences.
 *
 * Only 2 field are ignored: {@link Location#getExtras()} and {@link Location#getElapsedRealtimeNanos()}
 *
 * Created by -Bernardo on 2015-07-28.
 */
@Parcel
public class LocationModel implements Serializable {

    long time;
    double latitude;
    double longitude;
    float accuracy;
    double altitude;
    float bearing;
    String provider;
    float speed;

    public LocationModel() {}

    public LocationModel(Location location) {
        this.time = location.getTime();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.bearing = location.getBearing();
        this.provider = location.getProvider();
        this.speed = location.getSpeed();
    }

    public Location getLocation() {
        Location location = new Location(provider);
        location.setTime(time);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setAltitude(altitude);
        location.setBearing(bearing);
        location.setSpeed(speed);

        return location;
    }

    public long getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public String getProvider() {
        return provider;
    }

    public float getSpeed() {
        return speed;
    }
}
