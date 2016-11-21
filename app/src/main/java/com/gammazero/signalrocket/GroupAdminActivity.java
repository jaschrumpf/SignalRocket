package com.gammazero.signalrocket;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import java.security.acl.Group;

/**
 * Created by Jamie on 9/21/2016.
 */
public class GroupAdminActivity extends Activity {

    SimpleCursorAdapter mAdapter;
    static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
            ContactsContract.Data.DISPLAY_NAME};

    // This is the select criteria
    static final String SELECTION = "((" +
            ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
            ContactsContract.Data.DISPLAY_NAME + " != '' ))";

    SharedPreferences appPrefs;
    String myUserID;
    String data = "";
    ListView listView ;
    String[] values;
    String[] group_ids;
    String groupID = "";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups_activity);
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String group_url = "";
        SharedPreferences.Editor prefsEditor = appPrefs.edit();
        myUserID = appPrefs.getString("myUserID", null);
        Bundle extras = getIntent().getExtras();
        String group_type = extras.getString(("GROUP_TYPE"));
        if (group_type.equals("myGroups")) {
            group_url = "http://www.sandbistro.com/signalrocket/get_groups_i_own.php?group_owner_id=" + myUserID;
        } else {
            group_url = "http://www.sandbistro.com/signalrocket/get_groups_im_in.php?user_id=" + myUserID;
        }

        new DownloadTask().execute(group_url);
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
            String groupUrl = parms[0];

            try {
               // url = new URL("http://www.sandbistro.com/signalrocket/get_groups_i_own.php?group_owner_id=" + myUserID );
                url = new URL(groupUrl );
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
            if (!userData.equals("[] ")) {

                String list_of_groups = "{";
                JSONArray jsonMainNode = null;
                JSONObject jsonResponse = null;
                /*********** Process each JSON Node ************/

                /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                /*******  Returns null otherwise.  *******/
                try {
                    jsonResponse = new JSONObject(userData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonMainNode = jsonResponse.optJSONArray("AllMyGroups");
                /****************** End Parse Response JSON Data *************/

                int lengthJsonArr = jsonMainNode.length();
                values = new String[lengthJsonArr];
                group_ids = new String[lengthJsonArr];

                for (int i = 0; i < lengthJsonArr; i++) {
                    /****** Get Object for each JSON node.***********/
                    JSONObject jsonChildNode = null;
                    try {
                        jsonChildNode = jsonMainNode.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /******* Fetch node values **********/
                    String group_name = jsonChildNode.optString("group_name").toString();
                    String group_id = jsonChildNode.optString("group_id").toString();
                    values[i] = group_name;
                    group_ids[i] = group_id;
            }
                ArrayAdapter adapter = new ArrayAdapter<String>(GroupAdminActivity.this,R.layout.group_activity_listview, values);
                final ListView listView = (ListView) findViewById(R.id.mobile_list);
                listView.setAdapter(adapter);


                // ListView Item Click Listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition     = position;

                        // ListView Clicked item value
                        String  itemValue    = (String) listView.getItemAtPosition(position);

                        // Show Alert
                    //    Toast.makeText(getApplicationContext(),
                      //          "Position :"+itemPosition+"  Group Name : " +itemValue+"  Group ID : " +group_ids[itemPosition] , Toast.LENGTH_LONG)
                      //          .show();

                        new GetMembersTask().execute(group_ids[itemPosition]);


                    }

                });

            } else {
                Toast.makeText(getBaseContext(),"No members found for your group",Toast.LENGTH_LONG).show();
                Intent MapsActivityIntent = new Intent(getBaseContext(), MapsActivity.class);
                Bundle extras = getIntent().getExtras();
                MapsActivityIntent.putExtra("ZOOMLEVEL", extras.getFloat("ZOOMLEVEL"));
                MapsActivityIntent.putExtra("LATITUDE", extras.getDouble("LATITUDE"));
                MapsActivityIntent.putExtra("LONGITUDE", extras.getDouble("LONGITUDE"));
                startActivity(MapsActivityIntent);

            }
    }
    }

    public class GetMembersTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        @Override
        protected String doInBackground(String... parms) {

            URL url = null;
            String Content = "";
            groupID = parms[0];

            try {
                url = new URL("http://www.sandbistro.com/signalrocket/get_group_members.php?group_id=" + groupID );
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
            if (!userData.equals("[]")) {

                String list_of_members = "{";
                JSONArray jsonMainNode = null;
                JSONObject jsonResponse = null;
                /*********** Process each JSON Node ************/

                /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                /*******  Returns null otherwise.  *******/
                try {
                    jsonResponse = new JSONObject(userData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonMainNode = jsonResponse.optJSONArray("AllGroupMembers");
                /****************** End Parse Response JSON Data *************/

                int lengthJsonArr = jsonMainNode.length();
                values = new String[lengthJsonArr];
                group_ids = new String[lengthJsonArr];

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
                    String member_id = jsonChildNode.optString("user_id").toString();
                    values[i] = member_name;
                    group_ids[i] = member_id;
                }
                ArrayAdapter adapter = new ArrayAdapter<String>(GroupAdminActivity.this,R.layout.group_activity_listview, values);
                final ListView listView = (ListView) findViewById(R.id.mobile_list);
                listView.setAdapter(adapter);


                // ListView Item Click Listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition     = position;

                        // ListView Clicked item value
                        String  itemValue    = (String) listView.getItemAtPosition(position);

                        // Show Alert
                        Toast.makeText(getApplicationContext(),
                                "Position :"+itemPosition+"  Member Name : " +itemValue+"  Member ID : " +group_ids[itemPosition] , Toast.LENGTH_LONG)
                                .show();


                    }

                });

            }
        }
    }

}
