package com.example.ridetogo;

import android.content.Context;
import android.location.LocationManager;

public class gps_connection {
    //class to check if location services are turned on
    public static boolean locationTurnedOn(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled)
            return true;

        return false;
    }

}
