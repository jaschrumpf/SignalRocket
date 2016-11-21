package com.gammazero.signalrocket;

//import android.location.LocationListener;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.IconGenerator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;

//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Double dlatitude = 0.0;
    Double dlongitude = 0.0;
    Location mCurrentLocation;
    Marker teamLocationMarker;
    Marker myLocationMarker;
    Marker myNewLocationMarker;
    MarkerOptions markerOptions;
    String Content;
    String Error = null;
    String data = "";
    int sizeData = 0;
    JSONArray jsonData = null;
    SharedPreferences appPrefs;
    String myUserName = "";
    String myGroupName = "";
    String myUserID = "";
    String myGroupID = "";
    String myTimeInt = "";
    String myDistInt = "";
    int myMapType = 0;
    float zoomLevel = 13;
    LatLng latLng = null;
    LocationManager mlocManager;
    Activity myActivity;
    LocationListener mlocListener;
    // ================================================================================================
    int locTimeInterval;
    int locDistanceInterval;
    private GoogleApiClient client;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        zoomLevel = mMap.getCameraPosition().zoom;
        dlatitude = mCurrentLocation.getLatitude();
        dlongitude = mCurrentLocation.getLongitude();
        savedInstanceState.putFloat("ZOOMLEVEL", zoomLevel);
        savedInstanceState.putDouble("LATITUDE", dlatitude);
        savedInstanceState.putDouble("LONGITUDE",dlongitude);
        super.onSaveInstanceState(savedInstanceState);
    }


    protected void onPause(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        zoomLevel = mMap.getCameraPosition().zoom;
        dlatitude = mCurrentLocation.getLatitude();
        dlongitude = mCurrentLocation.getLongitude();
        savedInstanceState.putFloat("ZOOMLEVEL", zoomLevel);
        savedInstanceState.putDouble("LATITUDE", dlatitude);
        savedInstanceState.putDouble("LONGITUDE",dlongitude);
        Toast.makeText(this,"onPause called",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        zoomLevel = savedInstanceState.getFloat("ZOOMLEVEL");
        dlatitude = savedInstanceState.getDouble("LATITUDE");
        dlongitude = savedInstanceState.getDouble("LONGITUDE");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState != null) {
            zoomLevel = savedInstanceState.getFloat("ZOOMLEVEL");
            dlatitude = savedInstanceState.getDouble("LATITUDE");
            dlongitude = savedInstanceState.getDouble("LONGITUDE");
        }
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = appPrefs.edit();
        myMapType = appPrefs.getInt("myMapType", GoogleMap.MAP_TYPE_NORMAL);
        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");
        myTimeInt = appPrefs.getString("myTimeInterval", "30000");
        if (myTimeInt.equals("30 sec")) {
            myTimeInt = "30000";
            appPrefs.edit().putString("myTimeInterval", "30000");
            (appPrefs.edit()).commit();
        }
        myDistInt = appPrefs.getString("myDistanceInterval", "100");
        locTimeInterval = Integer.valueOf(myTimeInt);
        locDistanceInterval = Integer.valueOf(myDistInt);
        if (myUserName == "") {
            setContentView(R.layout.activity_startup);
            Button lets_go = (Button) findViewById(R.id.lets_go);
            final EditText my_user_name = (EditText) findViewById(R.id.my_user_name);
            final EditText my_group_name = (EditText) findViewById(R.id.my_group_name);
            lets_go.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    myUserName = my_user_name.getText().toString();
                    myGroupName = my_group_name.getText().toString();
                    String[] new_member_info = new String[2];
                    new_member_info[0] = myUserName;
                    new_member_info[1] = myGroupName;

                    new CreateNewMember().execute(new_member_info);
                }

            });

        }
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Asking user if explanation is needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            } else {
                long mintime = 30000;
                float mindistance = 250;

                mlocListener = new MyLocationListener();
                mlocManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        Long.parseLong(myTimeInt),
                        Float.parseFloat(myDistInt),
                        mlocListener);
            }
        } else {
            mlocListener = new MyLocationListener();
            mlocManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    Long.parseLong(myTimeInt),
                    Float.parseFloat(myDistInt),
                    mlocListener);
        }
        mCurrentLocation = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(myMapType);
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        dlatitude = mCurrentLocation.getLatitude();
        dlongitude = mCurrentLocation.getLongitude();
        latLng = new LatLng(dlatitude, dlongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));

        markerOptions = new MarkerOptions().title(myUserName);
        //   Toast.makeText(this,"myUserName = " + myUserName + "\nmyGroupId = " + myGroupID,Toast.LENGTH_LONG).show();
    }



    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("myUserName")){
            myUserName = sharedPreferences.getString(key, "Unknown");
        }
    }

    //==================================================================================================
    public class CreateNewMember extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected JSONArray doInBackground(String[] parms) {
            URL url = null;
            JSONArray jsonMainNode = null;
            String myGroupName = parms[1];

            try {
                String myUserName = URLEncoder.encode(parms[0], "UTF-8");
                url = new URL("http://76.106.11.111:7778/breadcrumbs/createMember.php?username="  + myUserName + "&groupname=" + myGroupName);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
            }
            BufferedReader reader = null;
            JSONObject jsonResponse;
            try {

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }
                reader.close();
                Content = sb.toString();

                /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                /*******  Returns null otherwise.  *******/
                jsonResponse = new JSONObject(Content);
                jsonMainNode = jsonResponse.optJSONArray("NewMember");
                /****************** End Parse Response JSON Data *************/

            } catch (Exception ex) {
                Error = ex.getMessage();
            }
            return jsonMainNode;
        }

        @Override
        protected void onPostExecute (JSONArray result){

            /*********** Process each JSON Node ************/

            int lengthJsonArr = result.length();
            appPrefs = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
            String user_id = "";
            String group_id = "";

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                JSONObject jsonChildNode = null;
                try {
                    jsonChildNode = result.getJSONObject(i);
                    String checkID = jsonChildNode.optString("new_user_id");
                    if (checkID != "") {
                        user_id = checkID;
                    }
                    String checkGroup = jsonChildNode.optString("new_group_id");
                    if (checkGroup != "") {
                        group_id = checkGroup;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

                /******* Fetch node values **********/


                SharedPreferences.Editor editor = appPrefs.edit();
                editor.putString("myUserName", myUserName);
                editor.putString("myUserID", user_id);
                editor.putString("myGroupName", myGroupName);
                editor.putString("myGroupID", group_id);

                editor.commit();


            Intent mapsActivityIntent = new Intent(MapsActivity.this, MapsActivity.class);
            startActivity(mapsActivityIntent);

            // start the asynchronous task after everthing else is done
            // callAsynchronousTask();


        }
    }
    //==================================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
//		case R.id.main_activity:
//			Intent mainIntent = new Intent(this, MainActivity.class);
//			startActivity(mainIntent);
//			return true;

            case R.id.preferences_activity:
                Intent preferencesIntent = new Intent(this, MyPreferencesActivity.class);
                startActivity(preferencesIntent);
                return true;

            case R.id.group_i_own:
                Intent groupsIntent = new Intent(this, GroupAdminActivity.class);
                zoomLevel = mMap.getCameraPosition().zoom;
                groupsIntent.putExtra("GROUP_TYPE", "myGroups");
                groupsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                groupsIntent.putExtra("LATITUDE", dlatitude);
                groupsIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(groupsIntent);
                return true;


        }

        return true;
    }
    //==================================================================================================

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

            // Getting coordinates of the current location
            Double dlat = location.getLatitude();
            Double dlng = location.getLongitude();
            String[] locationInfo = new String[3];
            locationInfo[0] = myUserID;
            locationInfo[1] = dlat.toString();
            locationInfo[2] = dlng.toString();

            latLng = new LatLng(dlat, dlng);
            //Toast.makeText(this, "My location is " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_LONG). show();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            // remove the old maker before setting the new one
            if (myLocationMarker != null) {
                myLocationMarker.remove();
            }
            myLocationMarker  = mMap.addMarker(new MarkerOptions()
                               .position(latLng)
                               .title(myUserName));
            //---stop listening for location changes---
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Asking user if explanation is needed
                    if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(myActivity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);


                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(myActivity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                } else {
                    mlocManager.removeUpdates(mlocListener);
                }
            }
            new PostLocation().execute(locationInfo);
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MapsActivity.this, "Provider " + provider + " status changed, status = " + status, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MapsActivity.this, "Provider status " + provider + " enabled", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MapsActivity.this, "Provider status " + provider + " disabled", Toast.LENGTH_LONG).show();
        }
    }
    public class PostLocation extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected Void doInBackground(String[] parms) {

            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.jamie_marker)));
            try {

                String id = parms[0];
                String latitude = parms[1];
                String longitude = parms[2];
                URL url = new URL("http://76.106.11.111:7778/breadcrumbs/receiveLocation.php?id=" + id + "&latitude=" + latitude + "&longitude=" + longitude);
                BufferedReader reader = null;

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }
                reader.close();
                Content = sb.toString();

            } catch (Exception e) {

                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Exception:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute () {
            Intent mapsActivityIntent = new Intent(MapsActivity.this, MapsActivity.class);
            startActivity(mapsActivityIntent);
        }
    }

}









