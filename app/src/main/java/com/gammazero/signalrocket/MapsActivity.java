package com.gammazero.signalrocket;

//import android.location.LocationListener;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;
import static android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;


//import com.google.android.gms.location.LocationListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MapsActivity";

    public GoogleMap mMap;
    Double dlatitude = 0.0;
    Double dlongitude = 0.0;
    Location mCurrentLocation;
    Double myLatitude;
    Double myLongitude;
    Marker[] groupMarkers = new Marker[100];
    String Content;
    String data = "";
    SharedPreferences appPrefs;
    SharedPreferences.Editor prefsEditor;
    String myUserName = "";
    String myGroupName = "";
    String myUserID = "";
    String myGroupID = "";
    String myTimeInt = "";
    String myDistInt = "";
    int myMapType = GoogleMap.MAP_TYPE_NORMAL;
    String prefMapType = "";
    float zoomLevel = 13;
    LatLng latLng = null;
    LocationManager mlocManager;
    LocationListener mlocListener;
    Activity myActivity;
    Menu menu;
    Timer timer = new Timer();
    boolean isTimerRunning;

    float fakeLat;

    // ================================================================================================
    int locTimeInterval;
    int locDistanceInterval;
    private GoogleApiClient client;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_STOP = 1;
    private ClusterManager<MyItem> mClusterManager;
    String[] locationInfo = new String[3];
    Context context;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        final String TAG = "MA/onSaveInstanceState";
        try {
            zoomLevel = mMap.getCameraPosition().zoom;
        } catch (Exception e) {
            zoomLevel = 13;
        }
        if (mCurrentLocation != null) {
            dlatitude = mCurrentLocation.getLatitude();
            dlongitude = mCurrentLocation.getLongitude();
        };
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
        Log.d(TAG, "Starting MapsActivity");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            zoomLevel = extras.getFloat("ZOOMLEVEL");
            dlatitude = extras.getDouble("LATITUDE");
            dlongitude = extras.getDouble("LONGITUDE");
        } else {
            zoomLevel = 13;
            dlatitude = 38.9;
            dlongitude = -77.0;
        }


        context = this;

        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener rocketPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Toast.makeText(getApplicationContext(), "Shared maps activity preference changed:" + key, Toast.LENGTH_LONG).show();
                if (key.equals("myUserName")) {
                    myUserName = prefs.getString(key, "Unknown");
                }
            }
        };

        appPrefs.registerOnSharedPreferenceChangeListener(rocketPrefsListener);
        prefsEditor = appPrefs.edit();
        prefMapType = appPrefs.getString("myMapType", "MAP_TYPE_NORMAL");
        Log.d(TAG, "prefMapType = " + prefMapType);
        if (prefMapType.equals("MAP_TYPE_NORMAL")) {
            myMapType = GoogleMap.MAP_TYPE_NORMAL;
        } else if (prefMapType.equals("MAP_TYPE_SATELLITE")){
                myMapType = GoogleMap.MAP_TYPE_SATELLITE;
        }

        myDistInt = appPrefs.getString("myDistanceInterval", "100");
        myTimeInt = appPrefs.getString("myTimeInterval", "100");
        locTimeInterval = Integer.valueOf(myTimeInt);
        locDistanceInterval = Integer.valueOf(myDistInt);
        try {
            String zLevel = appPrefs.getString("zoomLevel", "13");
            zoomLevel = Float.parseFloat(zLevel);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        dlatitude = Double.parseDouble(appPrefs.getString("dlatitude", "38.9"));
        dlongitude = Double.parseDouble(appPrefs.getString("dlongitde", "-77.0"));



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
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {

                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.marker_info_window_menu, menu);
            }
        });


        Log.d(TAG, "Entering onMapReady");
       // Toast.makeText(this, "Entering onMapReady", Toast.LENGTH_SHORT).show();

        // set up location manager and listener
        try {
            mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mlocListener = new RocketLocationListener();
            Log.d(TAG, "Entering permissions section");
        //    Toast.makeText(this, "Setting LocationManager, entering permssions entry", Toast.LENGTH_SHORT).show();
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
                Log.d(TAG, "Asking if explanation is needed");
                    // Asking user if explanation is needed
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                        //Prompt the user once explanation has been shown
                        Log.d(TAG, "Prompting for permissions");
                         ActivityCompat.requestPermissions(this,
                             new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                             MY_PERMISSIONS_REQUEST_LOCATION);

                    } else {
                        // No explanation needed, we can request the permission.
                        Log.d(TAG, "Asking for permissions");
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                } else {
                Log.d(TAG, "Permissions not needed");
               // Toast.makeText(this, "Setting LocationListener", Toast.LENGTH_SHORT).show();
                            mlocManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            Long.parseLong(myTimeInt),
                            Float.parseFloat(myDistInt),
                            mlocListener);

                    if (myMapType == GoogleMap.MAP_TYPE_NORMAL) {
                        Log.d(TAG, "map type = MAP_TYPE_NORMAL");

                    } else if (myMapType == GoogleMap.MAP_TYPE_SATELLITE){
                        Log.d(TAG, "map type = MAP_TYPE_SATELLITE");

                     };
                    mMap.setMapType(myMapType);
                    mCurrentLocation = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //   ClusterManager mClusterManager = new ClusterManager<MyItem>(this, mMap);
        //    mMap.setOnCameraIdleListener(mClusterManager);
        //    mMap.setOnMarkerClickListener(mClusterManager);

                Log.d(TAG, "Zooming map to level " + zoomLevel);
       // mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));

        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");
        myTimeInt = appPrefs.getString("myTimeInterval", "30000");
        myDistInt = appPrefs.getString("myDistanceInterval", "100");
        locTimeInterval = Integer.valueOf(myTimeInt);
        locDistanceInterval = Integer.valueOf(myDistInt);


        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        if (mCurrentLocation != null) {
            dlatitude = mCurrentLocation.getLatitude();
            dlongitude = mCurrentLocation.getLongitude();
        }
        latLng = new LatLng(dlatitude, dlongitude);
        Log.d(TAG, "Moving map to location " + dlatitude.toString() + " and " +
                   dlongitude.toString());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
               // mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 5000, null);
                Float mapZoom = mMap.getCameraPosition().zoom;
                String mapZoomString = mapZoom.toString();
                Log.d(TAG, "Zoom level from extras: " + zoomLevel + "; zoom level from camera = " + mapZoomString);

            }

        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Unable to set up location manager", Toast.LENGTH_LONG). show();
        }

        if (! isTimerRunning) {
           locationInfo[1] = dlatitude.toString();
           locationInfo[2] = dlongitude.toString();

            Log.d(TAG, "Starting timer job");
            setRepeatingAsyncTask();
        } else {
            Log.d(TAG, "Timer job already running");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        updateMenuTitles();
        return true;
    }

    private void updateMenuTitles() {
        MenuItem groupMenuItem = menu.findItem(R.id.current_group);
        groupMenuItem.setTitle("Current group is " + myGroupName);
        groupMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {


            case R.id.preferences_activity:
                Intent preferencesIntent = new Intent(this, MyPreferencesActivity.class);
                preferencesIntent.putExtra("ZOOMLEVEL", zoomLevel);
                preferencesIntent.putExtra("LATITUDE", dlatitude);
                preferencesIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(preferencesIntent);
                return true;

            case R.id.group_i_own:
                Intent groupsIntent = new Intent(this, GroupAdminActivity.class);
                zoomLevel = mMap.getCameraPosition().zoom;
                groupsIntent.putExtra("GROUP_TYPE", "myGroups");
                groupsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                groupsIntent.putExtra("LATITUDE", dlatitude);
                groupsIntent.putExtra("LONGITUDE", dlongitude);
                groupsIntent.putExtra("GROUP_RELATION", "OWNER");
                startActivity(groupsIntent);
                return true;

            case R.id.member_group:
                groupsIntent = new Intent(this, GroupAdminActivity.class);
                zoomLevel = mMap.getCameraPosition().zoom;
                groupsIntent.putExtra("GROUP_TYPE", "memberGroups");
                groupsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                groupsIntent.putExtra("LATITUDE", dlatitude);
                groupsIntent.putExtra("LONGITUDE", dlongitude);
                groupsIntent.putExtra("GROUP_RELATION", "MEMBER");
                startActivity(groupsIntent);
                return true;

            case R.id.send_to_group:
                Intent messsageIntent = new Intent(this, SendGroupMessageActivity.class);
                messsageIntent.putExtra("GROUP_TYPE", "sendGroups");
                messsageIntent.putExtra("ZOOMLEVEL", zoomLevel);
                messsageIntent.putExtra("LATITUDE", dlatitude);
                messsageIntent.putExtra("LONGITUDE", dlongitude);
                messsageIntent.putExtra("GROUP_RELATION", "");
                startActivity(messsageIntent);
                return true;

            case R.id.send_invitation:
                Intent invitationIntent = new Intent(this, InvitationActivity.class);
                zoomLevel = mMap.getCameraPosition().zoom;
                invitationIntent.putExtra("REQUEST_TYPE", "SEND");
                invitationIntent.putExtra("ZOOMLEVEL", zoomLevel);
                invitationIntent.putExtra("LATITUDE", dlatitude);
                invitationIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(invitationIntent);
                return true;

            case R.id.accept_invitation:
                Intent acceptIntent = new Intent(this, InvitationActivity.class);
                zoomLevel = mMap.getCameraPosition().zoom;
                acceptIntent.putExtra("REQUEST_TYPE", "RECEIVE");
                acceptIntent.putExtra("ZOOMLEVEL", zoomLevel);
                acceptIntent.putExtra("LATITUDE", dlatitude);
                acceptIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(acceptIntent);
                return true;

            case R.id.show_all:
                appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                String maxDistance = appPrefs.getString("maxDistance", "9.0");
                zoomLevel = getZoomLevel(maxDistance);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
                return true;

            case R.id.main_activity:
                Intent mainIntent = new Intent(this, MapsActivity.class);
                latLng = new LatLng(myLatitude, myLongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));


        }

        return true;
    }
    //==================================================================================================
    public class RocketLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            Log.d(TAG, "onLocationChanged.  New location is " + location.getLatitude() +
                    ", " + location.getLongitude());
            IconGenerator iconFactory = new IconGenerator(context);

            // Getting coordinates of the current location
            Double dlat = location.getLatitude();
            myLatitude = dlat;
            Double dlng = location.getLongitude();
            myLongitude = dlng;
            locationInfo[0] = myUserID;
            locationInfo[1] = dlat.toString();
            locationInfo[2] = dlng.toString();

            latLng = new LatLng(dlat, dlng);

            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                Log.d(TAG, "onLocationChanged: Moved camera to " + latLng.latitude + ", " + latLng.longitude);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(getApplicationContext(), "Unable to move camera", Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "onLocationChanged.  Location from camera is " + mMap.getCameraPosition().toString());
            //---stop listening for location changes---
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Asking user if explanation is needed
                    if (ActivityCompat.shouldShowRequestPermissionRationale(myActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(myActivity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION_STOP);


                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(myActivity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION_STOP);
                    }
                } else {
                    Log.d(TAG, "Calling PostLocation from position 1 in onLocationChanged");
                    new PostLocation().execute(locationInfo);
                }
            } else {
                Log.d(TAG, "Calling PostLocation from position 2 in onLocationChanged");
                new PostLocation().execute(locationInfo);
            }
        }


    @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.v(TAG, "Status Changed: Out of Service");
               // Toast.makeText(getApplicationContext(), "Status Changed: Out of Service",
               //         Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v(TAG, "Status Changed: Temporarily Unavailable");
          //      Toast.makeText(getApplicationContext(), "Status Changed: Temporarily Unavailable",
          //              Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:
                Log.v(TAG, "Status Changed: Available");
           //     Toast.makeText(getApplicationContext(), "Status Changed: Available",
          //             Toast.LENGTH_SHORT).show();
                break;
        }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS has been enabled.",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS has been disabled.",
                    Toast.LENGTH_SHORT).show();

        }
        };

    public class PostLocation extends AsyncTask<String, String, String> {

        String result;

        protected void onPreExecute() {
        }
        @Override
        protected String doInBackground(String... params) {

            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.jamie_marker)));
            String id = params[0];
            String latitude = params[1];
            String longitude = params[2];
            Log.d(TAG, "longitude = " + longitude + " and latitude = " + latitude);
                try {
                    Log.d(TAG, "Calling sendLocation web page");
                    URL url = new URL("http://www.sandbistro.com/signalrocket/sendLocation.php?id=" + id + "&latitude=" + latitude + "&longitude=" + longitude);
                    BufferedReader reader;

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

                    latitude = params[1];
                    longitude = params[2];
                    Log.w(TAG, "Exception running sendLocation:" + e.getMessage());
                }

                // Now go for the group info
                try {

                    URL url = new URL("http://www.sandbistro.com/signalrocket/getAllJson.php?group_id=" + myGroupID + "&my_lat=" + latitude + "&my_lng=" + longitude);
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
                    result = sb.toString();

                } catch (Exception e) {

                    Toast.makeText(context, "Unable to get location information", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Exception:" + e.getMessage());
                }
                return result;
            }


        protected void onPostExecute (String myResult) {

            IconGenerator iconFactory = new IconGenerator(context);
            Double maxDistance = 0.0;
            //  mClusterManager = new ClusterManager<MyItem>(context, mMap);

                //parse JSON data
                try {
                    JSONArray jArray = new JSONArray(myResult);
                    int arrayLength = jArray.length();

                    for (int i = 0; i < arrayLength; i++) {
                        if (groupMarkers[i] != null) {
                            groupMarkers[i].remove();
                        }

                        JSONObject jObject = jArray.getJSONObject(i);

                        String memberName = jObject.getString("name");
                        String memberID = jObject.getString("id");
                        String memberLat = jObject.getString("latitude");
                        Double memberDlat = Double.parseDouble(memberLat);
                        String memberLng = jObject.getString("longitude");
                        Double memberDlng = Double.parseDouble(memberLng);
                        Double memberDdist = jObject.getDouble("distance");
                        DecimalFormat df = new DecimalFormat("#.##");
                        DecimalFormat df1 = new DecimalFormat("#.#");
                        memberLat = df.format(memberDlat);
                        memberLng = df.format(memberDlng);
                        String memberDist = df1.format(memberDdist);
                        String snippet = "(" + memberLat + ", " + memberLng + ")";
                        String text = memberDist + " mi.";
                        if (Double.parseDouble(memberDist) > maxDistance) {
                            maxDistance = Double.parseDouble(memberDist);
                        }
                        String message_text = "";
                        try {
                            message_text = jObject.getString("message_text");
                            if (!message_text.equals("null")) {
                                memberName = memberName + ": " + message_text;
                            }

                            Log.d(TAG, "memberName = " + memberName + " and memberLat = " + memberLat + " and memberLng = " + memberLng);
                        //   Toast.makeText(getApplicationContext(), "memberName = " + memberName + " and memberLat = " + memberLat + " and memberLng = " + memberLng, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            message_text = "";
                        }
                        LatLng memberLatLng = new LatLng(memberDlat, memberDlng);
                        int timeDiff = jObject.getInt("timediff");
                        // get absolute value in a really cheap and dirty way
                        if (timeDiff < 0) {
                            timeDiff = timeDiff * -1;
                        }
                        // set colors for markers depending on how stale the record is
                        if (timeDiff > 1 && timeDiff < 5) {
                            iconFactory.setColor(Color.YELLOW);
                        } else {
                            if (timeDiff >= 5) {
                                iconFactory.setColor(Color.RED);
                                iconFactory.setTextAppearance(Color.WHITE);
                            }
                        }

                        groupMarkers[i] = addIcon(iconFactory, memberName, memberLatLng, snippet, text);
                        //MyItem offsetItem = new MyItem(memberDlat, memberDlng);
                        //mClusterManager.addItem(offsetItem);

                        //  groupMarkers[i] = mMap.addMarker(new MarkerOptions()
                        //                  .position(memberLatLng)
                        //                  .title(memberName)
                        //                  .snippet(message_text));

                    } // End Loop
//                startActivity(mapsActivityIntent);



                    appPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    prefsEditor = appPrefs.edit();
                    prefsEditor.putString("zoomLevel", Float.toString(zoomLevel));
                    prefsEditor.commit();

                    //Toast.makeText(getApplicationContext(), "Max distance = " + maxDistance, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Log.e("JSONException", "Error: " + e.toString());
                }
            }
    }

    private Marker addIcon(IconGenerator iconFactory, CharSequence message_text, LatLng position, String snippet, String text) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(message_text))).
                title(text).
                snippet(snippet).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        Marker marker = mMap.addMarker(markerOptions);
        return marker;
    }

    private CharSequence makeCharSequence() {
        String prefix = "Mixing ";
        String suffix = "different fonts";
        String sequence = prefix + suffix;
        SpannableStringBuilder ssb = new SpannableStringBuilder(sequence);
        ssb.setSpan(new StyleSpan(ITALIC), 0, prefix.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(BOLD), prefix.length(), sequence.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }


    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMapType(myMapType);
                        mlocManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                Long.parseLong(myTimeInt),
                                Float.parseFloat(myDistInt),
                                mlocListener);
                        mCurrentLocation = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Failed to set location updates", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_LOCATION_STOP: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mlocManager.removeUpdates(mlocListener);
                        new PostLocation().execute(locationInfo);
                    } catch (SecurityException se) {
                        Toast.makeText(this, "Failed to end location updates", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private boolean setRepeatingAsyncTask() {

        final Handler handler = new Handler();
        timer = new Timer();
        isTimerRunning = true;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.d(TAG, "Calling PostLocation task from timer: locationInfo[1] = " + locationInfo[1] + " and location[2]" + locationInfo[2]);
                            new PostLocation().execute(locationInfo);
                         //   myMapType = appPrefs.getInt("myMapType", GoogleMap.MAP_TYPE_NORMAL);
                         //   myUserName = appPrefs.getString("myUserName", "");
                         //   myUserID = appPrefs.getString("myUserID", "");
                         //   myGroupName = appPrefs.getString("myGroupName", "");
                         //   myGroupID = appPrefs.getString("myGroupID", "");
                         //   myTimeInt = appPrefs.getString("myTimeInterval", "30000");
                         //   myDistInt = appPrefs.getString("myDistanceInterval", "100");

                        } catch (Exception e) {
                            Toast.makeText(context, "Error posting location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, Integer.parseInt(myTimeInt));
        return isTimerRunning;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
        timer = null;

        try {
            mlocManager.removeUpdates(mlocListener);
        } catch (SecurityException e) {
            Log.d(TAG, "Exception removing updates: " + e.getMessage());
        }


    }

       public void updatePrefs (SharedPreferences newPrefs) {


                    prefMapType = newPrefs.getString("myMapType", "MAP_TYPE_NORMAL");
                    if (prefMapType.equals("MAP_TYPE_NORMAL")) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    } else if (prefMapType.equals("MAP_TYPE_SATELLITE")) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    }
                    myDistInt = newPrefs.getString("myDistanceInterval", "100");
                    locTimeInterval = Integer.valueOf(myTimeInt);
                    myTimeInt = newPrefs.getString("myTimeInterval", "30000");
                    locDistanceInterval = Integer.valueOf(myDistInt);

                    try {
                        mlocManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                Long.parseLong(myTimeInt),
                                Float.parseFloat(myDistInt),
                                mlocListener);
                    } catch (SecurityException se) {
                        Log.d(TAG, "Unable to update location manager");
                    }
        }
    private float getZoomLevel (String distance) {

        Double maxDistance = Double.parseDouble(distance);
        if (maxDistance > 3360.0) {
            zoomLevel = 1.0f;
        } else if (maxDistance > 1280.0) {
            zoomLevel = 2.0f;
        } else if (maxDistance > 640.0) {
            zoomLevel = 3.0f;
        } else if (maxDistance > 320.0) {
            zoomLevel = 4.0f;
        } else if (maxDistance > 160.0) {
            zoomLevel = 5.0f;
        } else if (maxDistance > 80.0) {
            zoomLevel = 6.0f;
        } else if (maxDistance > 40.0) {
            zoomLevel = 7.0f;
        } else if (maxDistance > 20.0) {
            zoomLevel = 8.0f;
        } else if (maxDistance > 10.0) {
            zoomLevel = 9.0f;
        } else if (maxDistance > 5.0) {
            zoomLevel = 10.0f;
        } else if (maxDistance > 2.5) {
            zoomLevel = 13.0f;
        } else if (maxDistance > 1.25) {
            zoomLevel = 14.0f;
        }
        return zoomLevel;
    }

}











