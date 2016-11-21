package com.gammazero.signalrocket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationListener;


/**
 * Created by Jamie on 10/2/2016.
 */
public class LocationService extends Service implements LocationListener {
    public final static int MINUTE = 1000 * 60;


    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location; // location
    double latitude = 0; // latitude
    double longitude = 0; // longitude
    String provider;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1 * MINUTE;

    // Declaring a Location Manager
    protected LocationManager locationManager;



    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public
            // methods
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled. DEFAULT COORDINATES


            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                   // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                  //          this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                  //      location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                    //    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                           //     this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                    //        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("LOCATION", "Latitude: " + latitude + "- Longitude: " + longitude);


        return location;
    }

    @Override
    public void onLocationChanged(Location arg0) {

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = null;

      //  intent = new Intent(this, CompleteSurveyActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    //    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true)
   //             .setContentIntent(contentIntent).setContentTitle(this.getString(R.string.app_name)).setContentText("text");

        // mBuilder.setContentIntent(contentIntent);
      //  mNotificationManager.notify((int) System.currentTimeMillis() % Integer.MAX_VALUE, mBuilder.build());

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        Log.i("LOCATION", "Latitude: " + latitude + "- Longitude: " + longitude);

    }

//    @Override
    public void onProviderDisabled(String arg0) {
    }

//    @Override
    public void onProviderEnabled(String arg0) {
    }

  //  @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

}