package com.gammazero.signalrocket;

import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by Jamie on 11/25/2016.
 */

public class UserAdminActivity extends Activity{

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
    String[] group_ids;
    String[] member_ids;
    String groupID = "";
    String group_type = "";
    Float zoomLevel;
    Double dlatitude;
    Double dlongitude;
    Boolean[] memberChecked = new Boolean[50];
    String group_relation = "";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting UserAdminActivity");
        Bundle extras = getIntent().getExtras();
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();

        String group_name = extras.getString("GROUP_NAME");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");

        if (group_relation.equals("OWNER")) {
            setContentView(R.layout.user_activity);
        } else if (group_relation.equals("MEMBER")) {
            setContentView(R.layout.user_activity_member);

        }
        String group_url = "";
        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");

        TextView tv = new TextView(this);
        tv=(TextView)findViewById(R.id.display_group);
        tv.setText("Showing group " + group_name);

        new UserAdminActivity.DownloadTask().execute(group_name);
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
        String group_name = parms[0];

        try {
            url = new URL("http://www.sandbistro.com/signalrocket/getGroupMembers.php?user_id=" + myUserID + "&group_name=" + group_name );
        } catch (MalformedURLException e) {
            e.printStackTrace();       }
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
            JSONObject jObject = null;

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                try {
                    jObject = jArray.getJSONObject(i);
                    String member_name = jObject.getString("name");
                    String member_id = jObject.getString("id");
                    values[i] = member_name;
                    member_ids[i] = member_id;


                } catch (NullPointerException npe) {
                    Log.e(TAG, npe.getMessage());
                    GoHome();

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    GoHome();

                }

            }
            listView = (ListView) findViewById(R.id.user_list);
         //   ArrayAdapter adapter = new ArrayAdapter<String>(UserAdminActivity.this, R.layout.user_activity_listview, values);
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplication(), R.layout.user_activity_listview, values);
            listView.setAdapter(adapter);


            // ListView Item Click Listener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    // ListView Clicked item index
                    int itemPosition = position;
                    if (memberChecked[itemPosition] == null) {
                        memberChecked[itemPosition] = true;
                    } else if (memberChecked[itemPosition] == true) {
                        memberChecked[itemPosition] = false;
                    } else if (memberChecked[itemPosition] == false) {
                        memberChecked[itemPosition] = true;
                    }
                    if (memberChecked[itemPosition] == true) {
                        listView.getChildAt(itemPosition).setBackgroundColor(Color.parseColor("#aaaaaa"));
                    } else if (memberChecked[itemPosition] == false) {
                        listView.getChildAt(itemPosition).setBackgroundColor(Color.BLACK);
                    }
                    // ListView Clicked item value
                    final String itemValue = (String) listView.getItemAtPosition(position);

                    // Show Alert
                   // Toast.makeText(getApplicationContext(),
                     //       "Position :" + itemPosition + "  Group Name : " + itemValue + "  Group ID : " + group_ids[itemPosition], Toast.LENGTH_LONG)
                     //       .show();

                    // new GetMembersTask().execute(group_ids[itemPosition]);


                }

            });
        }
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

}
