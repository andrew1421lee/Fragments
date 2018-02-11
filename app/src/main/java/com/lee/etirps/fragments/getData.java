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

public class getData extends IntentService{

    public static final String TOP_RIGHT = "fragments.getdata.get.TOP_RIGHT_BOUND";
    public static final String BOTTOM_LET = "fragments.getdata.get.BOTTOM_LET_BOUND";

    public static final String RESPONSE_MESSAGE = "fragments.getdata.get.RESPONSE_MESSAGE";

    public getData(){
        super("getData"); // run IntentService constructor with name e621GetService
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String topRight = intent.getStringExtra(getData.TOP_RIGHT);
        String bottomLeft = intent.getStringExtra(getData.BOTTOM_LET);

        Log.d("getData", topRight);
        Log.d("getData", bottomLeft);

        HttpURLConnection connection;
        URL url;
        String responseString = "";
        try{
            url = new URL("http://149.125.137.126:5000/get_points/" + topRight + "=" + bottomLeft);
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
                }catch (Exception ex) {
                    Log.e("getData", "err " + connection.getResponseCode());
                }finally {
                    connection.disconnect();
                }
            } catch (Exception ex) {
                Log.e("getData", ex.getMessage());
            }

        }catch(Exception ex){
            Log.e("getData", ex.getMessage());
        }

        Intent broadcastIntent = new Intent(MapsActivity.getDataReceiver.MESSAGE);
        SharedPreferences.Editor editor = getSharedPreferences(MapsActivity.PREFS_NAME,0).edit();
        editor.putString(getData.RESPONSE_MESSAGE, responseString);
        editor.commit();
        sendBroadcast(broadcastIntent);
    }
}
