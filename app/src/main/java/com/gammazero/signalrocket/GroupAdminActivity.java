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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
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

/**
 * Created by Jamie on 9/21/2016.
 */
public class GroupAdminActivity extends Activity {

    private static final String TAG = "GroupAdminActivity";
    private static final String PREFERENCE_FILE = "com.gammazero.signalrocket.prefs";
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
    ListView listView ;
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


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting GroupAdminActivity");
        context = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();

        Bundle extras = getIntent().getExtras();
        String group_type = extras.getString("GROUP_TYPE");
      //  myGroupName = extras.getString("GROUP_NAME");
      //  myGroupID = extras.getString("GROUP_ID");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");
        if (group_relation.equals("OWNER")) {
            setContentView(R.layout.groups_activity);
        } else if (group_relation.equals("MEMBER")){
            setContentView(R.layout.groups_activity_member);}

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
                                if (! prefsEditor.commit()) {
                                    Toast.makeText(context, "Groups save failed", Toast.LENGTH_LONG).show();
                                }

                            } catch (Exception e) {
                                values[i] = values[i];
                               prefsEditor.putString(values[i], group_ids[i]);
                                if (! prefsEditor.commit()) {
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
                ArrayAdapter adapter = new ArrayAdapter<String>(GroupAdminActivity.this,R.layout.group_activity_listview, values);
                listView.setAdapter(adapter);
                registerForContextMenu(listView);


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
                }

            });
    }
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu,
                                    final View v, final ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_groups_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = (int) info.id;
        final String  groupName    = (String) listView.getItemAtPosition(id);
        switch (item.getItemId()) {
            case R.id.list_group:
                Intent UserAdminIntent = new Intent(getBaseContext(), UserAdminActivity.class);
                Bundle extras = getIntent().getExtras();
                UserAdminIntent.putExtra("ZOOMLEVEL", extras.getFloat("ZOOMLEVEL"));
                UserAdminIntent.putExtra("LATITUDE", extras.getDouble("LATITUDE"));
                UserAdminIntent.putExtra("LONGITUDE", extras.getDouble("LONGITUDE"));
                UserAdminIntent.putExtra("GROUP_RELATION", group_relation);
                UserAdminIntent.putExtra("GROUP_NAME", groupName);
                startActivity(UserAdminIntent);
                return true;
            case R.id.make_active_group:
                prefsEditor.putString("myGroupName", groupName);
                prefsEditor.putString("myGroupID", (appPrefs.getString(groupName,"")));
                prefsEditor.commit();
                appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (! prefsEditor.commit()) {
                    Toast.makeText(context, "Preference save failed", Toast.LENGTH_LONG).show();
                }
                Intent mainIntent = new Intent(getBaseContext(), MapsActivity.class);
                extras = getIntent().getExtras();
                mainIntent.putExtra("ZOOMLEVEL", extras.getFloat("ZOOMLEVEL"));
                mainIntent.putExtra("LATITUDE", extras.getDouble("LATITUDE"));
                mainIntent.putExtra("LONGITUDE", extras.getDouble("LONGITUDE"));
                startActivity(mainIntent);

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
                preferencesIntent.putExtra("ZOOMLEVEL", zoomLevel);
                preferencesIntent.putExtra("LATITUDE", dlatitude);
                preferencesIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(preferencesIntent);
                return true;

            case R.id.group_i_own:
                Intent groupsIntent = new Intent(this, GroupAdminActivity.class);
                groupsIntent.putExtra("GROUP_TYPE", "myGroups");
                groupsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                groupsIntent.putExtra("LATITUDE", dlatitude);
                groupsIntent.putExtra("LONGITUDE", dlongitude);
                groupsIntent.putExtra("GROUP_RELATION", "OWNER");
                startActivity(groupsIntent);
                return true;

            case R.id.member_group:
                groupsIntent = new Intent(this, GroupAdminActivity.class);
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
                invitationIntent.putExtra("REQUEST_TYPE", "SEND");
                invitationIntent.putExtra("ZOOMLEVEL", zoomLevel);
                invitationIntent.putExtra("LATITUDE", dlatitude);
                invitationIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(invitationIntent);
                return true;

            case R.id.accept_invitation:
                Intent acceptIntent = new Intent(this, InvitationActivity.class);
                acceptIntent.putExtra("REQUEST_TYPE", "RECEIVE");
                acceptIntent.putExtra("ZOOMLEVEL", zoomLevel);
                acceptIntent.putExtra("LATITUDE", dlatitude);
                acceptIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(acceptIntent);
                return true;

            case R.id.main_activity:
                Intent mainIntent = new Intent(this, MapsActivity.class);
                mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
                mainIntent.putExtra("LATITUDE", dlatitude);
                mainIntent.putExtra("LONGITUDE", dlongitude);


        }

        return true;
    }

    //==================================================================================================

    public void deleteGroup(View view) {

        Toast.makeText(this, "deleteGroup button pushed",Toast.LENGTH_LONG).show();
    }


    public void createNewGroup(View view) {

        setContentView(R.layout.create_new_group_activity);
        Button new_group = (Button) findViewById(R.id.new_group_button);
      //  final EditText my_user_name = (EditText) findViewById(R.id.my_user_name);
        final EditText my_group_name = (EditText) findViewById(R.id.new_group_name);
        new_group.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            //    myUserName = my_user_name.getText().toString();
                myGroupName = my_group_name.getText().toString();
                String[] new_member_info = new String[2];
                new_member_info[0] = myUserID;
                new_member_info[1] = myGroupName;

                new CreateNewGroup().execute(new_member_info);
            }

        });
    }
    //==================================================================================================
    public class CreateNewGroup extends AsyncTask<String, Void, String[]> {

        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected String[] doInBackground(String[] parms) {
            URL url = null;
            String[] groupInfo = new String[2];
            String userId = parms[0];
            groupInfo[1] = parms[1];
            String groupName = "";

            try {
                groupName = URLEncoder.encode(parms[1], "UTF-8");
                url = new URL("http://www.sandbistro.com/signalrocket/createGroup.php?user_id=" + userId + "&group_name=" + groupName);
            } catch (MalformedURLException e) {
                Log.d(TAG, e.getMessage());
            } catch (UnsupportedEncodingException uee) {
                Log.d(TAG, uee.getMessage());
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
                groupInfo[0] = sb.toString();

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            return groupInfo;
        }

        @Override
        protected void onPostExecute (String[] result){

            /*********** Process each JSON Node ************/


            if (! result[1].equals("Error")) {

                Toast.makeText(getApplicationContext(), "New group created", Toast.LENGTH_LONG).show();
                prefsEditor.putString(result[1], result[0]);
                prefsEditor.commit();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to create new group", Toast.LENGTH_LONG).show();
            }
            Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
            mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
            mapsIntent.putExtra("LATITUDE", dlatitude);
            mapsIntent.putExtra("LONGITUDE", dlongitude);

            startActivity(mapsIntent);
        }
    }
    //==================================================================================================

    public class GetGroupId extends AsyncTask<String, Void, String[]> {

        public void onPreExecute() {
            super.onPreExecute();

        }

        public String[] doInBackground(String[] parms) {
            String[] result = new String[2];
            URL url = null;
            String userID = parms[0];
            String groupName = parms[1];

            try {
                String myUserName = URLEncoder.encode(parms[0], "UTF-8");
                url = new URL("http://www.sandbistro.com/signalrocket/getGroupID.php?user_id=" + userID + "&group_name=" + groupName);
            } catch (MalformedURLException e) {
                Log.d(TAG, e.getMessage());
            } catch (UnsupportedEncodingException uee) {
                Log.d(TAG, uee.getMessage());
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
                result[0] = sb.toString();
                result[1] = groupName;

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            return result;
        }

        public void onPostExecute(String[] result) {

            if (result[1].equals("Error")) {
                Toast.makeText(getApplicationContext(), "Error activating group", Toast.LENGTH_LONG).show();
            } else {
                appPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefsEditor = appPrefs.edit();

                prefsEditor.putString("myGroupID", result[0]);
                prefsEditor.putString("myGroupName", result[1]);
                prefsEditor.commit();
            }
            Intent mainIntent = new Intent(getApplicationContext(), MapsActivity.class);
            mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
            mainIntent.putExtra("LATITUDE", dlatitude);
            mainIntent.putExtra("LONGITUDE", dlongitude);
            startActivity(mainIntent);

        }
    }
}
