package com.lee.etirps.fragments;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Anchu on 2/11/2018.
 */

public class sendData extends IntentService {

    public static final String ALL = "fragments.senddata.post.TOP_RIGHT_BOUND";

    public static final String RESPONSE_MESSAGE = "fragments.senddata.post.RESPONSE_MESSAGE";


    public sendData(){
        super("sendData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String payload = intent.getStringExtra(ALL);

        HttpURLConnection connection;
        URL url;
        String responseString = "";
        try{
            url = new URL("http://149.125.137.126:5000/new_point");
            try{
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-type", "application/text");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                try {
                    // Try to get data from the connection
                    OutputStream stream = connection.getOutputStream();
                    DataOutputStream writer = new DataOutputStream(stream);

                    writer.writeBytes(payload);
                    writer.flush();
                    writer.close();
                    stream.close();
                    responseString = "Fragment dropped";
                    Log.e("sendData", "err " + connection.getResponseCode());
                }catch (Exception ex) {
                    Log.e("sendData", "err " + connection.getResponseCode());
                }finally {
                    connection.disconnect();
                }
            } catch (Exception ex) {
                Log.e("sendData", ex.getMessage());
            }

        }catch(Exception ex){
            Log.e("sendData", ex.getMessage());
        }

        Intent broadcastIntent = new Intent(MapsActivity.sendDataReceiver.MESSAGE);
        SharedPreferences.Editor editor = getSharedPreferences(MapsActivity.PREFS_NAME,0).edit();
        editor.putString(sendData.RESPONSE_MESSAGE, responseString);
        editor.commit();
        sendBroadcast(broadcastIntent);
    }
}
