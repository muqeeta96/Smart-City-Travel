package com.example.smartcitytravel.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;

import khttp.KHttp;
import khttp.responses.Response;

public class Connection {
    public Connection() {

    }

    public boolean isConnectionSourceAndInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        } else {
            return isInternetAvailable();
        }

    }

    public boolean isInternetAvailable() {
        try {
            Response response = KHttp.get("https://www.google.com/",
                    new HashMap<String, String>(), new HashMap<String, String>(),
                    null, null, null, null, 2);

            return response.getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnectionSourceAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

}
