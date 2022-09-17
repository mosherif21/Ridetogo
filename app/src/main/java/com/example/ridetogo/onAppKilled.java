package com.example.ridetogo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class onAppKilled extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //this service will used to remove driver as available if he exits or stops app
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance(firebase_google_keys_ids.firebase_database_path).getReference("AvailableDrivers");
        GeoFire geofire = new GeoFire(ref);
        geofire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }
}
