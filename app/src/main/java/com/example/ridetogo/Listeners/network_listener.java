package com.example.ridetogo.Listeners;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.ridetogo.R;
import com.example.ridetogo.internet_connection;

import java.lang.ref.WeakReference;

public class network_listener extends BroadcastReceiver {
    private static WeakReference<Activity> ActivityRef;
    private static int refresh;

    public static void updateActivity(Activity activity, int Refresh) {
        ActivityRef = new WeakReference<Activity>(activity);
        refresh = Refresh;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!internet_connection.ConnectedToInternetCheck(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View no_internet_layout = LayoutInflater.from(context).inflate(R.layout.no_internet_dialogue, null);
            builder.setView(no_internet_layout);
            Button retry = no_internet_layout.findViewById(R.id.btn_retry_conn);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (internet_connection.ConnectedToInternetCheck(context) && refresh == 1) {
                        // ActivityRef.get().finish();
                        //ActivityRef.get().overridePendingTransition(0, 0);
                        // ActivityRef.get().startActivity(ActivityRef.get().getIntent());
                    } else
                        onReceive(context, intent);
                }
            });
        }


    }
}
