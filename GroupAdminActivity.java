package com.gammazero.signalrocket;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.acl.Group;
import java.util.ArrayList;

/**
 * Created by Jamie on 9/21/2016.
 */
public class GroupAdminActivity extends AppCompatActivity {

    private static final String TAG = "GroupAdminActivity";
    private static final String PREFERENCE_FILE = "com.gammazero.signalrocket.prefs";
    Menu menu;

    SimpleCursorAdapter mAdapter;
    static final String[] PROJECTION = new String[]{ContactsContract.Data._ID,
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
    String groupID = "";
    String group_type = "";
    Float zoomLevel;
    Double dlatitude;
    Double dlongitude;
    String group_relation = "";
    String newActiveGroup = "";
    String newActiveGroupID = "";
    Context context;
    Boolean[] memberChecked = new Boolean[50];
    String groupList = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Starting GroupAdminActivity");
        context = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();

        Bundle extras = getIntent().getExtras();
        String group_type = extras.getString("GROUP_TYPE");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");
        if (group_relation.equals("OWNER")) {
            setContentView(R.layout.groups_activity);
        } else if (group_relation.equals("MEMBER")) {
            setContentView(R.layout.groups_activity_member);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String group_url = "";
        Context context = this;
        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");
        if (group_type.equals("myGroups")) {
            group_url = "http://www.sandbistro.com/signalrocket/getOwnedGroups.php?group_owner_id=" + myUserID;
        } else {
            group_url = "http://www.sandbistro.com/signalrocket/getMemberGroups.php?user_id=" + myUserID;
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

                url = new URL(groupUrl);
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
            //if (!userData.equals("[] ")) {

            String group_name = "";
            String group_id = "";
            JSONArray jsonMainNode = null;
            JSONObject jsonResponse = null;
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
            //jsonMainNode = jsonResponse.optJSONArray("AllMyGroups");
            /****************** End Parse Response JSON Data *************/


            values = new String[lengthJsonArr];
            group_ids = new String[lengthJsonArr];
            JSONObject jObject = null;

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                try {
                    jObject = jArray.getJSONObject(i);
                    values[i] = jObject.getString("name");
                    group_ids[i] = jObject.getString("id");
                    try {
                        String owner_name = jObject.getString("owner_name");
                        values[i] = values[i] + "   /   " + owner_name;
                        prefsEditor.putString(values[i], group_ids[i]);
                        if (!prefsEditor.commit()) {
                            Toast.makeText(context, "Groups save failed", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        values[i] = values[i];
                        prefsEditor.putString(values[i], group_ids[i]);
                        if (!prefsEditor.commit()) {
                            Toast.makeText(context, "Groups save failed", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (NullPointerException npe) {
                    Log.e(TAG, npe.getMessage());
                    values[i] = "Not currently a member of any groups";
                    group_ids[i] = null;


                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    GoHome();

                }

            }
            listView = (ListView) findViewById(R.id.mobile_list);
            ArrayAdapter adapter = new ArrayAdapter<String>(GroupAdminActivity.this, R.layout.group_activity_listview, values);
            listView.setAdapter(adapter);
            registerForContextMenu(listView);

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
                        groupList = groupList + group_ids[position] + ":";
                        listView.getChildAt(position).setBackgroundColor(Color.parseColor("#aaaaaa"));
                    } else if (!memberChecked[position]) {
                        groupList = groupList.replace(group_ids[position], "");
                        listView.getChildAt(position).setBackgroundColor(Color.TRANSPARENT);
                    }
                 }
            });
        }
    }

    //==================================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        if (group_relation.equals("OWNER")) {
            inflater.inflate(R.menu.my_groups_menu, menu);
        } else {
            inflater.inflate(R.menu.group_member_menu, menu);
        }
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

            case 16908332:
                Intent mainIntent = new Intent(this, MapsActivity.class);
                mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
                mainIntent.putExtra("LATITUDE", dlatitude);
                mainIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(mainIntent);
                return true;

            case R.id.preferences_activity:
                Intent preferencesIntent = new Intent(this, MyPreferencesActivity.class);
                preferencesIntent.putExtra("ZOOMLEVEL", zoomLevel);
                preferencesIntent.putExtra("LATITUDE", dlatitude);
                preferencesIntent.putExtra("LONGITUDE", dlongitude);
                preferencesIntent.putExtra("GROUP_RELATION", group_relation);
                preferencesIntent.putExtra("GROUP_TYPE", group_type);
                startActivity(preferencesIntent);
                return true;


            case R.id.main_activity:
                 mainIntent = new Intent(this, MapsActivity.class);
                mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
                mainIntent.putExtra("LATITUDE", dlatitude);
                mainIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(mainIntent);
                return true;

            case R.id.create_group:
                setContentView(R.layout.create_new_group_activity);
                // Button new_group = (Button) findViewById(R.id.new_group_button);
                final EditText new_group_name = (EditText) findViewById(R.id.new_group_name);
                new_group_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        boolean handled = false;
                        if (actionId == EditorInfo.IME_ACTION_SEND) {
                            handled = true;
                            String newGroupName = new_group_name.getText().toString();
                            String[] new_group_info = new String[2];
                            new_group_info[0] = myUserID;
                            new_group_info[1] = newGroupName;
                            new CreateNewGroup().execute(new_group_info);
                        }
                        return handled;
                    }
                });

                return true;


            case R.id.delete_group:
                deleteGroup(groupList);
                return true;

            case R.id.hide_me:
                doSomethingWithMe("hide",groupList);
                return true;

            case R.id.show_me:
                doSomethingWithMe("show", groupList);
                return true;

            case R.id.remove_me:
                doSomethingWithMe("remove", groupList);
                return true;


        }

        return true;
    }

    //==================================================================================================
    public class CreateNewGroup extends AsyncTask<String, Void, JSONArray> {

        String response = "";

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected JSONArray doInBackground(String[] parms) {
            URL url = null;
            JSONArray jsonMainNode = null;
            String userId = parms[0];
            String data = "";

            try {
                String groupName = URLEncoder.encode(parms[1], "UTF-8");
                url = new URL("http://www.sandbistro.com/signalrocket/createGroup.php?user_id=" + userId + "&group_name=" + groupName);
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
                jsonResponse = new JSONObject(data);
                jsonMainNode = jsonResponse.optJSONArray("NewGroup");

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            return jsonMainNode;
        }

        @Override
        protected void onPostExecute(JSONArray result) {

            /*********** Process each JSON Node ************/
            int lengthJsonArr = result.length();
            String new_group_name = "";
            String new_group_id = "";

            try {
                for (int i = 0; i < lengthJsonArr; i++) {
                    /****** Get Object for each JSON node.***********/
                    JSONObject jsonChildNode = null;

                    jsonChildNode = result.getJSONObject(i);
                    String checkName = jsonChildNode.optString("new_group_name");
                    String checkID = jsonChildNode.optString("new_group_id");
                    if (!checkID.equals("")) {
                        new_group_id = checkID;
                    }
                    if (!checkName.equals("")) {
                        new_group_name = checkName;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();

            }
            Toast.makeText(getApplicationContext(), "New group created", Toast.LENGTH_LONG).show();
            prefsEditor.putString(new_group_name, new_group_id);
            prefsEditor.commit();
            Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
            mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
            mapsIntent.putExtra("LATITUDE", dlatitude);
            mapsIntent.putExtra("LONGITUDE", dlongitude);

            startActivity(mapsIntent);
        }
    }

    //==================================================================================================
    public void deleteGroup(String groupList) {

        if (groupList.equals("")) {
            Toast.makeText(this, "No groups selected to delete", Toast.LENGTH_LONG).show();
        } else {
            new DeleteGroup().execute(groupList);
        }
    }


    public class DeleteGroup extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected String doInBackground(String... parms) {
            String result = "";
            BufferedReader reader;
            String groupIds = parms[0];

            try {
                // initialize the dialog
                String data = "";
                String groupIDs = URLEncoder.encode(groupIds, "UTF-8");

                URL url = new URL("http://www.sandbistro.com/signalrocket/deleteGroups.php?groupList=" + groupIDs);
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


        protected void onPostExecute(String result) {

            /*********** Process each JSON Node ************/


            if (result.startsWith("Success")) {

                Toast.makeText(getApplicationContext(), "Groups deleted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to delete groups", Toast.LENGTH_LONG).show();
            }
            Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
            mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
            mapsIntent.putExtra("LATITUDE", dlatitude);
            mapsIntent.putExtra("LONGITUDE", dlongitude);

            startActivity(mapsIntent);
        }
    }

    //==================================================================================================
    public void doSomethingWithMe(String action, String groups) {

        String[] doList = new String[3];
        if (groups.equals("")) {
            Toast.makeText(this, "No groups selected", Toast.LENGTH_LONG).show();
        } else {
            doList[0] = action;
            doList[1] = myUserID;
            doList[2] = groups;
            new DoSomethingWithMe().execute(doList);

        }
    }
    protected class DoSomethingWithMe extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... parms) {
            String result = "";
            BufferedReader reader;
            String userAction = parms[0];
            String user_id = parms[1];
            String groupIds = parms[2];

            try {
                // initialize the dialog
                String data = "";
                String groupIDs = URLEncoder.encode(groupIds, "UTF-8");
                URL url = null;

                switch (userAction) {
                    case "hide":
                        url = new URL("http://www.sandbistro.com/signalrocket/hideMemberInGroup.php?user_id=" + user_id + "&groupList=" + groupIDs);

                    case "show":
                        url = new URL("http://www.sandbistro.com/signalrocket/showMemberInGroup.php?user_id=" + user_id + "&groupList=" + groupIDs);

                    case "remove":
                        url = new URL("http://www.sandbistro.com/signalrocket/removeMemberFromGroup.php?user_id=" + user_id + "&groupList=" + groupIDs);
                }

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


        protected void onPostExecute(String result) {

            if (result.startsWith("Success")) {

                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Uh-oh, didn't work", Toast.LENGTH_LONG).show();
            }
            Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
            mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
            mapsIntent.putExtra("LATITUDE", dlatitude);
            mapsIntent.putExtra("LONGITUDE", dlongitude);

            startActivity(mapsIntent);
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_long_press_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.list_members:
                    Intent useAdminIntent = new Intent(this, UserAdminActivity.class);
                    useAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
                    useAdminIntent.putExtra("LATITUDE", dlatitude);
                    useAdminIntent.putExtra("LONGITUDE", dlongitude);
                    useAdminIntent.putExtra("GROUP_ID",group_ids[info.position]);
                    useAdminIntent.putExtra("GROUP_NAME", values[info.position]);
                    useAdminIntent.putExtra("GROUP_RELATION", group_relation);
                    useAdminIntent.putExtra("GROUP_TYPE", group_type);
                    startActivity(useAdminIntent);
                    finish();

                    return true;

                case R.id.make_active:
                    myGroupID = group_ids[info.position];
                    myGroupName = values[info.position];
                    Toast.makeText(this, "group id = " + myGroupID + " group name = " + myGroupName,Toast.LENGTH_LONG).show();
                    prefsEditor.putString("myGroupID", myGroupID);
                    prefsEditor.putString("myGroupName", myGroupName);
                    prefsEditor.commit();
                    GoHome();
                    return true;
                default:
                    return super.onContextItemSelected(item);
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
