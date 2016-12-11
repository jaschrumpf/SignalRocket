package com.gammazero.signalrocket;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

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
import java.net.URLEncoder;

/**
 * Created by Jamie on 11/25/2016.
 */

public class UserAdminActivity extends AppCompatActivity {

    private static final String TAG = "GroupAdminActivity";
    Menu menu;

    SimpleCursorAdapter mAdapter;
    static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
            ContactsContract.Data.DISPLAY_NAME};

    // This is the select criteria
    static final String SELECTION = "((" +
            ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
            ContactsContract.Data.DISPLAY_NAME + " != '' ))";

    SharedPreferences appPrefs;
    SharedPreferences.Editor prefsEditor;

    String myUserID;
    String myUserName;
    String myGroupID;
    String myGroupName;
    String data = "";
    ListView listView;
    String[] values;
    String[] member_ids;
    String group_id;
    Float zoomLevel;
    Double dlatitude;
    Double dlongitude;
    Boolean[] memberChecked = new Boolean[50];
    String group_relation = "";
    String membersList = "";
    String group_name = "";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting UserAdminActivity");
        Bundle extras = getIntent().getExtras();
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();

        group_name = extras.getString("GROUP_NAME");
        group_id = extras.getString("GROUP_ID");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");

        if (group_relation.equals("OWNER")) {
            setContentView(R.layout.user_activity);
        } else if (group_relation.equals("MEMBER")) {
            setContentView(R.layout.user_activity_member);

        }
        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");

        TextView tv;
        tv=(TextView)findViewById(R.id.display_group);
        tv.setText("Showing group " + group_name);

        new UserAdminActivity.DownloadTask().execute(group_id);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }


    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

public class DownloadTask extends AsyncTask<String, Void, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // initialize the dialog
    }

    @Override
    protected String doInBackground(String... parms) {

        URL url = null;
        String Content = "";
        String group_id = parms[0];

        try {
            url = new URL("http://www.sandbistro.com/signalrocket/getGroupMembers.php?group_id=" + group_id);
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

        return Content;
    }

    protected void onPostExecute(String userData) {
        if (userData.equals("[] ")) {
            Toast.makeText(getApplication(), "No members in this group.", Toast.LENGTH_LONG).show();
        } else {

            JSONArray jArray = null;
            int lengthJsonArr = 0;
            /*********** Process each JSON Node ************/

            /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
            /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
            /*******  Returns null otherwise.  *******/
            try {
                //jsonResponse = new JSONObject(userData);
                jArray = new JSONArray(userData);
                lengthJsonArr = jArray.length();

                if (lengthJsonArr == 0) {
                    GoHome();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                GoHome();
            }
            values = new String[lengthJsonArr];
            member_ids = new String[lengthJsonArr];
            JSONObject jObject;

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                try {
                    jObject = jArray.getJSONObject(i);
                    String member_name = jObject.getString("name");
                    String member_id = jObject.getString("id");
                    String isActive = jObject.getString("active");
                    values[i] = member_name;
                    member_ids[i] = member_id;

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    GoHome();

                }

            }
            listView = (ListView) findViewById(R.id.user_list);
            //   ArrayAdapter adapter = new ArrayAdapter<String>(UserAdminActivity.this, R.layout.user_activity_listview, values);
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplication(), R.layout.user_activity_listview, values);
            listView.setAdapter(adapter);


            if (group_relation.equals("OWNER")) {
                // ListView Item Click Listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {

                        // ListView Clicked item index
                        if (memberChecked[position] == null) {
                            memberChecked[position] = true;
                        } else if (memberChecked[position]) {
                            memberChecked[position] = false;
                        } else if (!memberChecked[position]) {
                            memberChecked[position] = true;
                        }
                        if (memberChecked[position]) {
                            membersList = membersList + member_ids[position] + ":";
                            listView.getChildAt(position).setBackgroundColor(Color.parseColor("#aaaaaa"));
                        } else if (!memberChecked[position]) {
                            membersList = membersList.replace(member_ids[position], "");
                            listView.getChildAt(position).setBackgroundColor(Color.TRANSPARENT);
                        }

                    }

                });
            }
        }
    }
}
    //==================================================================================================
    public void markMembersInactive(String members) {

        if (group_relation.equals("OWNER") && members.equals("")) {
            Toast.makeText(this, "No groups selected to delete", Toast.LENGTH_LONG).show();
        } else {
            if (group_relation.equals("MEMBER")) {
                new MarkInactive().execute(myUserID);
            } else {
                new MarkInactive().execute(members);
            }
        }
    }


    public class MarkInactive extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected String doInBackground(String... parms) {
            String result = "";
            BufferedReader reader;
            String members= parms[0];

            try {
                // initialize the dialog
                String data = "";
                String groupIDs = URLEncoder.encode(members, "UTF-8");

                URL url = new URL("http://www.sandbistro.com/signalrocket/markMembersInactive.php?group_id=" + group_id + "&memberList=" + members);
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

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }

            return result;
        }


        protected void onPostExecute (String result){

            /*********** Process each JSON Node ************/


            if (result.startsWith("Success")) {

                Toast.makeText(getApplicationContext(), "Members marked inactive", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to deactivate members", Toast.LENGTH_LONG).show();
            }
            Intent userAdminIntent = new Intent(getApplicationContext(), UserAdminActivity.class);
            userAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
            userAdminIntent.putExtra("LATITUDE", dlatitude);
            userAdminIntent.putExtra("LONGITUDE", dlongitude);;
            userAdminIntent.putExtra("GROUP_ID", group_id);
            userAdminIntent.putExtra("GROUP_NAME", group_name);
            userAdminIntent.putExtra("GROUP_RELATION", group_relation);
            startActivity(userAdminIntent);


            //      startActivity(mapsIntent);
        }
    }
    //==================================================================================================
    public void markMembersActive(String members) {

        if (members.equals("")) {
            Toast.makeText(this, "No members selected", Toast.LENGTH_LONG).show();
        } else {
            new MarkActive().execute(members);
        }
    }


    public class MarkActive extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected String doInBackground(String... parms) {
            String result = "";
            BufferedReader reader;
            String members= parms[0];

            try {
                // initialize the dialog
                String data = "";
                String groupIDs = URLEncoder.encode(members, "UTF-8");

                URL url = new URL("http://www.sandbistro.com/signalrocket/markMembersActive.php?group_id=" + group_id  + "&memberList=" + members);
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

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }

            return result;
        }


        protected void onPostExecute (String result){

            /*********** Process each JSON Node ************/


            if (result.startsWith("Success")) {

                Toast.makeText(getApplicationContext(), "Members activated", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to activate members", Toast.LENGTH_LONG).show();
            }
            Intent userAdminIntent = new Intent(getApplicationContext(), UserAdminActivity.class);
            userAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
            userAdminIntent.putExtra("LATITUDE", dlatitude);
            userAdminIntent.putExtra("LONGITUDE", dlongitude);;
            userAdminIntent.putExtra("GROUP_ID", group_id);
            userAdminIntent.putExtra("GROUP_NAME", group_name);
            userAdminIntent.putExtra("GROUP_RELATION", group_relation);
            startActivity(userAdminIntent);

            //startActivity(mapsIntent);
        }
    }
    //==================================================================================================
    public void deleteMembers(String members) {

        if (members.equals("")) {
            Toast.makeText(this, "No members selected", Toast.LENGTH_LONG).show();
        } else {
            new DeleteMembers().execute(members);
        }
    }


    public class DeleteMembers extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected String doInBackground(String... parms) {
            String result = "";
            BufferedReader reader;
            String members= parms[0];

            try {
                // initialize the dialog
                String data = "";
                String groupIDs = URLEncoder.encode(members, "UTF-8");

                URL url = new URL("http://www.sandbistro.com/signalrocket/deleteMembers.php?group_id=" + group_id  + "&memberList=" + members);
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

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }

            return result;
        }


        protected void onPostExecute (String result){

            /*********** Process each JSON Node ************/


            if (result.startsWith("Success")) {

                Toast.makeText(getApplicationContext(), "Members deleted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to delete Members", Toast.LENGTH_LONG).show();
            }
            Intent userAdminIntent = new Intent(getApplicationContext(), UserAdminActivity.class);
            userAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
            userAdminIntent.putExtra("LATITUDE", dlatitude);
            userAdminIntent.putExtra("LONGITUDE", dlongitude);;
            userAdminIntent.putExtra("GROUP_ID", group_id);
            userAdminIntent.putExtra("GROUP_NAME", group_name);
            userAdminIntent.putExtra("GROUP_RELATION", group_relation);
            startActivity(userAdminIntent);

        }
    }
    //==================================================================================================
    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (group_relation.equals("OWNER")) {
            getMenuInflater().inflate(R.menu.user_owner_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.user_member_menu, menu);

        }
        return true;
    }


    private void updateMenuTitles() {
        MenuItem groupMenuItem = menu.findItem(R.id.current_group);
        groupMenuItem.setTitle("Current group is " + group_name);
        groupMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.member_inactive:
                markMembersInactive(membersList);
                return true;
            case R.id.member_active:
                markMembersActive(membersList);
                return true;

            case R.id.member_delete:
                deleteMembers(membersList);
                return true;

            case R.id.preferences_activity:
                Intent preferencesIntent = new Intent(this, MyPreferencesActivity.class);
                preferencesIntent.putExtra("ZOOMLEVEL", zoomLevel);
                preferencesIntent.putExtra("LATITUDE", dlatitude);
                preferencesIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(preferencesIntent);
                return true;




            case R.id.go_home:
                Intent MapsActivityIntent = new Intent(getBaseContext(), MapsActivity.class);
                Bundle extras = getIntent().getExtras();
                MapsActivityIntent.putExtra("ZOOMLEVEL", extras.getFloat("ZOOMLEVEL"));
                MapsActivityIntent.putExtra("LATITUDE", extras.getDouble("LATITUDE"));
                MapsActivityIntent.putExtra("LONGITUDE", extras.getDouble("LONGITUDE"));
                startActivity(MapsActivityIntent);
                return true;
        }
        return true;
    }

        private void GoHome() {
        Intent MapsActivityIntent = new Intent(getBaseContext(), MapsActivity.class);
        Bundle extras = getIntent().getExtras();
        MapsActivityIntent.putExtra("ZOOMLEVEL", extras.getFloat("ZOOMLEVEL"));
        MapsActivityIntent.putExtra("LATITUDE", extras.getDouble("LATITUDE"));
        MapsActivityIntent.putExtra("LONGITUDE", extras.getDouble("LONGITUDE"));
        startActivity(MapsActivityIntent);
    }

}

