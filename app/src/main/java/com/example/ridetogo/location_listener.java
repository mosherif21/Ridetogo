package com.example.ridetogo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;

public class location_listener extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if(!gps_connection.locationTurnedOn(context)){
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            View no_internet_layout= LayoutInflater.from(context).inflate(R.layout.no_location_dialogue,null);
            builder.setView(no_internet_layout);
            Button turn_location=no_internet_layout.findViewById(R.id.btn_turn_location);
            AlertDialog dialog=builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
            turn_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                 //   if(gps_connection.locationTurnedOn(context)){
                   //     ActivityRef.get().finish();
                     //   ActivityRef.get().startActivity(ActivityRef.get().getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                 //   }
                 //   else
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
        }
    }

}
