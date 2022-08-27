package com.example.ridetogo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class internet_connection {
    public static boolean ConnectedToInternetCheck(Context context) {
        ConnectivityManager connman = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connman != null) {
            NetworkInfo[] info = connman.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
