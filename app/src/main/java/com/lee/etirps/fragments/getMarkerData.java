package com.lee.etirps.fragments;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Anchu on 2/10/2018.
 */

public class getMarkerData extends IntentService{

    public static final String LOCATION = "fragments.getmarker.get.LOCATION";
    public static final String MARKER = "fragments.getmarker.get.MARKER";

    public static final String RESPONSE_MESSAGE = "fragments.getmarker.get.RESPONSE_MESSAGE";

    public getMarkerData(){
        super("getMarkerData"); // run IntentService constructor with name e621GetService
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String location = intent.getStringExtra(getMarkerData.LOCATION);
        String marker = intent.getStringExtra(getMarkerData.MARKER);

        Log.d("getMarkerData", location);
        Log.d("getMarkerData", marker);

        HttpURLConnection connection;
        URL url;
        String responseString = "";
        try{
            url = new URL("http://149.125.137.126:5000/get_marker/" + location + "=" + marker);
            try{
                connection = (HttpURLConnection) url.openConnection();

                try {
                    // Try to get data from the connection
                    InputStream in = new BufferedInputStream(connection.getInputStream());

                    // Convert the input stream into a string
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    responseString = result.toString("UTF-8");
                    Log.e("getMarkerData", "err " + connection.getResponseCode());
                }catch (Exception ex) {
                    Log.e("getMarkerData", "err " + connection.getResponseCode());
                }finally {
                    connection.disconnect();
                }
            } catch (Exception ex) {
                Log.e("getMarkerData", ex.getMessage());
            }

        }catch(Exception ex){
            Log.e("getMarkerData", ex.getMessage());
        }

        Intent broadcastIntent = new Intent(MapsActivity.getMarkerReceiver.MESSAGE);
        SharedPreferences.Editor editor = getSharedPreferences(MapsActivity.PREFS_NAME,0).edit();
        editor.putString(getMarkerData.RESPONSE_MESSAGE, responseString);
        editor.commit();
        sendBroadcast(broadcastIntent);
    }
}
