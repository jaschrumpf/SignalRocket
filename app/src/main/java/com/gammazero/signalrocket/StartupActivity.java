package com.gammazero.signalrocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;

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

/**
 * Created by Jamie on 12/3/2016.
 */

public class StartupActivity extends Activity {

    SharedPreferences appPrefs;
    SharedPreferences.Editor prefsEditor;
    String myUserName = "";
    String myGroupName = "";
    String myUserID = "";
    String myGroupID = "";
    String myTimeInt = "";
    String myDistInt = "";
    int myMapType = 0;
    final String TAG = "StartupActivity";
    String data = "";
    Context context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Starting StartupActivity");

        context = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();
        myUserName = appPrefs.getString("myUserName", "");
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

        } else {

            Intent mapsActivityIntent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(mapsActivityIntent);
            finish();

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
            myGroupName = parms[1];

            try {
                String myUserName = URLEncoder.encode(parms[0], "UTF-8");
                url = new URL("http://www.sandbistro.com/signalrocket/createMember.php?username="  + myUserName + "&groupname=" + myGroupName);
            } catch (MalformedURLException e) {
                Log.d(TAG, e.getMessage());
            } catch (UnsupportedEncodingException uee) {
                Log.d(TAG, uee.getMessage());
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
                data = sb.toString();

                /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                /*******  Returns null otherwise.  *******/
                jsonResponse = new JSONObject(data);
                jsonMainNode = jsonResponse.optJSONArray("NewMember");
                /****************** End Parse Response JSON Data *************/

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            return jsonMainNode;
        }

        @Override
        protected void onPostExecute (JSONArray result){

            /*********** Process each JSON Node ************/

            int lengthJsonArr = result.length();
            String user_id = "";
            String group_id = "";

            try {
                for (int i = 0; i < lengthJsonArr; i++) {
                    /****** Get Object for each JSON node.***********/
                    JSONObject jsonChildNode = null;

                    jsonChildNode = result.getJSONObject(i);
                    String checkID = jsonChildNode.optString("new_user_id");
                    String checkGroup = jsonChildNode.optString("new_group_id");
                    if (checkID != "" ) {
                        user_id = checkID;
                    }
                    if (checkGroup != "") {
                        group_id = checkGroup;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();

            }

            /******* Fetch node values **********/


            prefsEditor.putString("myUserName", myUserName);
            prefsEditor.putString("myUserID", user_id);
            prefsEditor.putString("myGroupName", myGroupName);
            prefsEditor.putString("myGroupID", group_id);

            prefsEditor.commit();

            Intent mapsActivityIntent = new Intent(context, MapsActivity.class);
            startActivity(mapsActivityIntent);


        }
    }
    //==================================================================================================
}
