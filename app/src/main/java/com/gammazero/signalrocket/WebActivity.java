package com.gammazero.signalrocket;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jamie on 9/30/2016.
 */
public class WebActivity {

    private Marker GetMembersLocations (String groupId, GoogleMap gMap, Marker teamMarker) {

        URL url = null;
        String Content = "";
        String myUserID = "0";
        String myGroupID = groupId;
        String data = null;
        teamMarker.remove();

        try {
            url = new URL("http://www.sandbistro.com/signalrocket/get_members.php?user_id=" + myUserID + "&group_id=" + myGroupID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BufferedReader reader = null;

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!Content.equals("[] ")) {

            JSONArray jsonMainNode = null;
            JSONObject jsonResponse = null;
            /*********** Process each JSON Node ************/

            /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
            /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
            /*******  Returns null otherwise.  *******/
            try {
                jsonResponse = new JSONObject(Content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonMainNode = jsonResponse.optJSONArray("AllMembers");
            /****************** End Parse Response JSON Data *************/

            int lengthJsonArr = jsonMainNode.length();

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                JSONObject jsonChildNode = null;
                try {
                    jsonChildNode = jsonMainNode.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /******* Fetch node values **********/
                String member_name = jsonChildNode.optString("member_name").toString();
                String lat = jsonChildNode.optString("loc_lat").toString();
                String lng = jsonChildNode.optString("loc_long").toString();
                Double loc_lat = 0.0;
                loc_lat = loc_lat.parseDouble(lat);
                Double loc_lng = 0.0;
                loc_lng = loc_lng.parseDouble(lng);
                String last_rec_time = jsonChildNode.optString("last_rec_time").toString();
                LatLng latLng = new LatLng(loc_lat, loc_lng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(member_name);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                markerOptions.snippet(last_rec_time);
                teamMarker = gMap.addMarker(markerOptions);
            }
        }

        return teamMarker;
    }
}



