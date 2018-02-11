package com.lee.etirps.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener, OnMyLocationClickListener, LocationListener, OnMapReadyCallback, OnCameraIdleListener,
        OnCameraMoveStartedListener, PostMessageDialogFragment.PostMessageDialogListener, OnMarkerClickListener {
    public static final String PREFS_NAME = "com.lee.etirps.fragments.PREFS";

    public List<Marker> markers = new ArrayList<>();
    public Marker selected_mark;

    private GoogleMap mMap;
    private FusedLocationProviderClient myLocation;
    private LocationCallback mLocationCallback;
    private UiSettings mUiSettings;
    private double viewWidth = 0.002452544867992401;
    private double viewHeight = 0.002948395655657521;

    private LatLng topRight;
    private LatLng bottomLeft;

    private LatLng mostTopRight;
    private LatLng mostBottomLeft;

    private Location currentLocation;
    private View taskBar;

    private getDataReceiver receiver;
    private sendDataReceiver sendreceiver;
    private getMarkerReceiver markerReceiver;
    //private LatLngBounds CenterBound;
    //private LocationManager locationManager;
    //private static final long MIN_TIME = 200;
    //private static final float MIN_DISTANCE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        IntentFilter filter = new IntentFilter(getDataReceiver.MESSAGE);
        receiver = new getDataReceiver();
        registerReceiver(receiver, filter);

        IntentFilter send_filter = new IntentFilter(sendDataReceiver.MESSAGE);
        sendreceiver = new sendDataReceiver();
        registerReceiver(sendreceiver, send_filter);

        IntentFilter marker_filter = new IntentFilter(getMarkerReceiver.MESSAGE);
        markerReceiver = new getMarkerReceiver();
        registerReceiver(markerReceiver, marker_filter);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myLocation = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult result){
                currentLocation = result.getLastLocation();
                moveCameraToLocation(currentLocation);

            }
        };
        taskBar = findViewById(R.id.taskbar);
    }

    @Override
    public void onDestroy(){
        this.unregisterReceiver(receiver);
        this.unregisterReceiver(sendreceiver);
        this.unregisterReceiver(markerReceiver);
        super.onDestroy();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                // Getting view from the layout file
                View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

                TextView title = (TextView) v.findViewById(R.id.author);
                title.setText(marker.getTitle());

                TextView message = (TextView) v.findViewById(R.id.message_text);
                TextView date = (TextView) v.findViewById(R.id.msg_Date);
                //Log.v("getInfoWindow", marker.getSnippet() + ".");
                try{
                    String[] splitted = marker.getSnippet().split("#!#");
                    //Log.v("getInfoWindow", splitted[1]);
                    //Log.v("getInfoWindow", splitted[0]);

                    message.setText(splitted[1]);
                    date.setText(splitted[0]);
                }catch(NullPointerException ex){
                    message.setText("");
                    date.setText("");
                }


                return v;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                return null;

            }
        });

        //mMap.addMarker(new MarkerOptions().position(new LatLng(42.109015, -75.946749)).title("My Home"));

        //boolean styled = mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.lite_json)));

        boolean styled = mMap.setMapStyle( MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        //Log.v("bugsa",getResources().getString(R.string.style_json));

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false);
        mMap.setBuildingsEnabled(false);

        mUiSettings.setScrollGesturesEnabled(true);

        try{
            myLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    moveCameraToLocation(location);

                }
            });
            myLocation.requestLocationUpdates(new LocationRequest().setInterval(200).setSmallestDisplacement(5),mLocationCallback,null);
        }catch(SecurityException e){
            Toast.makeText(this, "Should never happen", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onMyLocationClick(@NonNull Location location) {

        DialogFragment post_diag = new PostMessageDialogFragment();
        post_diag.show(getSupportFragmentManager(), "post");

        //Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

        //VisibleRegion viewPort = mMap.getProjection().getVisibleRegion();
        //mMap.addMarker(new MarkerOptions().position(viewPort.farLeft));
        //mMap.addMarker(new MarkerOptions().position(viewPort.farRight));

        //Log.v("width", "test " + (viewPort.farLeft.longitude - viewPort.nearRight.longitude));
        //Log.v("height", "test " + (viewPort.farLeft.latitude - viewPort.nearRight.latitude));

        //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        //marker.setAlpha(1.0f);
        LatLng latLng = marker.getPosition();
        //Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mMap.animateCamera(cameraUpdate);

        Log.v("onMarkerClick", "Clicked");

        selected_mark = marker;

        Intent fetchData = new Intent(MapsActivity.this, getMarkerData.class);
        fetchData.putExtra(getMarkerData.MARKER, latLng.latitude + ":" + latLng.longitude);
        fetchData.putExtra(getMarkerData.LOCATION, currentLocation.getLatitude() + ":" + currentLocation.getLongitude());
        startService(fetchData);

        return false;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog){
        String name = ((EditText)dialog.getDialog().findViewById(R.id.name)).getText().toString();
        String msg = ((EditText)dialog.getDialog().findViewById(R.id.message_box)).getText().toString();
        String lat = String.valueOf(currentLocation.getLatitude());
        String lng = String.valueOf(currentLocation.getLongitude());
        String date = Calendar.getInstance().get(Calendar.YEAR) + "-" + Integer.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        Log.v("date", date);

        Intent newPoint = new Intent(MapsActivity.this, sendData.class);

        newPoint.putExtra(sendData.ALL, lat + "|||" + lng + "|||" + date + "|||" + name + "|||" + msg);

        startService(newPoint);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog){
        dialog.dismiss();
    }

    @Override
    public void onLocationChanged(Location location) {
        moveCameraToLocation(location);

        /*
        VisibleRegion viewPort = mMap.getProjection().getVisibleRegion();
        CenterBound = viewPort.latLngBounds;
        mMap.setLatLngBoundsForCameraTarget(CenterBound);*/

        //Location lastLocation = location;
        //mMap.addCircle(new CircleOptions().center(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())).radius(5));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
        //locationManager.removeUpdates(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onCameraIdle(){
        taskBar.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                taskBar.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        taskBar.startAnimation(animate);
    }

    @Override
    public void onCameraMoveStarted(int i){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                taskBar.getHeight()); // toYDelta
        animate.setDuration(200);
        animate.setFillAfter(true);
        taskBar.startAnimation(animate);
        taskBar.setVisibility(View.INVISIBLE);
    }

    public void scanButtonPress(View view){
        moveCameraToLocation(currentLocation);

        for(Marker m : markers){
            m.remove();
        }

        Intent getPoints = new Intent(MapsActivity.this, getData.class);

        getPoints.putExtra(getData.TOP_RIGHT, mostTopRight.latitude + ":" + mostTopRight.longitude);
        getPoints.putExtra(getData.BOTTOM_LET, mostBottomLeft.latitude + ":" + mostBottomLeft.longitude);

        startService(getPoints);
    }

    public void moveCameraToLocation(Location location){
        currentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mMap.animateCamera(cameraUpdate);

        LatLng one = new LatLng(latLng.latitude - (viewHeight/2), latLng.longitude - (viewWidth /2));
        LatLng two = new LatLng(latLng.latitude + (viewHeight/2),latLng.longitude + (viewWidth/2));

        LatLngBounds box = new LatLngBounds(one, two);

        bottomLeft = one;
        topRight = two;

        mostTopRight = new LatLng(topRight.latitude + (viewHeight/2), topRight.longitude + (viewHeight/2));
        mostBottomLeft = new LatLng(bottomLeft.latitude - (viewHeight/2), bottomLeft.longitude - (viewWidth/2));

        //mMap.addMarker(new MarkerOptions().position(mostTopRight));
        //mMap.addMarker(new MarkerOptions().position(mostBottomLeft));

        mMap.setLatLngBoundsForCameraTarget(box);

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 42);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            /*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);*/
        }
    }


    public class sendDataReceiver extends BroadcastReceiver{
        public static final String MESSAGE = "BRO THE DATA IS SENT!";

        @Override
        public void onReceive(Context context, Intent intent) {
            String responseMessage = getSharedPreferences(PREFS_NAME,0).getString(sendData.RESPONSE_MESSAGE,"No Message");
            Toast.makeText(MapsActivity.this, responseMessage, Toast.LENGTH_LONG).show();
        }
    }

    public class getMarkerReceiver extends BroadcastReceiver{
        public static final String MESSAGE = "BRO THE MARKER DATA IS DOWNLOADED!!!!!";
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseMessage = getSharedPreferences(PREFS_NAME,0).getString(getMarkerData.RESPONSE_MESSAGE, "No Message");

            try{
                JSONArray obj = JsonPath.read(responseMessage, "$");

                selected_mark.setTitle(String.valueOf(obj.get(1)));
                selected_mark.setSnippet(String.valueOf(obj.get(0)) + "#!#" + String.valueOf(obj.get(2)));
                Log.v("getMarkerReceiver", selected_mark.getSnippet());
                selected_mark.showInfoWindow();
            }catch(Exception ex){
                selected_mark.setTitle("Undiscovered Fragment");
                selected_mark.setSnippet("Move closer to view" + "#!#" + "...");
            }


            //Toast.makeText(MapsActivity.this, responseMessage, Toast.LENGTH_SHORT).show();

            //JSONArray var = JsonPath.read(responseMessage, "$");

            //selected_mark.setTitle(String.valueOf(var.get(0)));
            //selected_mark.setSnippet(String.valueOf(var.get(1)));

            /*
            JSONArray var = JsonPath.read(responseMessage, "$");

            for(Object obj : var){

                float lat = Float.valueOf(String.valueOf(((JSONArray) obj).get(1)));
                float lng = Float.valueOf(String.valueOf(((JSONArray) obj).get(2)));

                //String author = String.valueOf(((JSONArray) obj).get(3));
                //String msg = String.valueOf(((JSONArray) obj).get(4));

                LatLng coord = new LatLng(lat, lng);
                markers.add(mMap.addMarker(new MarkerOptions().position(coord).title("Undiscovered Fragment")));
            }*/

            /*
            try{
                Log.e("MapActivity" , String.valueOf(((JSONArray) var.get(0)).get(0)));
            }catch (Exception ex)
            {
                //
            }*/

            //Toast.makeText(MapsActivity.this, responseMessage, Toast.LENGTH_SHORT).show();

        }
    }

    public class getDataReceiver extends BroadcastReceiver{
        public static final String MESSAGE = "BRO THE DATA IS DOWNLOADED!!!!!";
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseMessage = getSharedPreferences(PREFS_NAME,0).getString(getData.RESPONSE_MESSAGE, "No Message");

            JSONArray var = JsonPath.read(responseMessage, "$");

            for(Object obj : var){

                float lat = Float.valueOf(String.valueOf(((JSONArray) obj).get(0)));
                float lng = Float.valueOf(String.valueOf(((JSONArray) obj).get(1)));

                //String author = String.valueOf(((JSONArray) obj).get(3));
                //String msg = String.valueOf(((JSONArray) obj).get(4));

                LatLng coord = new LatLng(lat, lng);
                markers.add(mMap.addMarker(new MarkerOptions()
                        .position(coord)
                        .title("Undiscovered Fragment")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.custom_marker))
                        ));
            }

            /*
            try{
                Log.e("MapActivity" , String.valueOf(((JSONArray) var.get(0)).get(0)));
            }catch (Exception ex)
            {
                //
            }*/

            //Toast.makeText(MapsActivity.this, responseMessage, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 42: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try{
                        mMap.setMyLocationEnabled(true);
                        /*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);*/
                    }
                    catch (SecurityException e) {
                        Toast.makeText(this, "Should never happen", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You must enable location permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
