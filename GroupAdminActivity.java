package com.gammazero.signalrocket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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
import java.util.ArrayList;

/**
 * Created by Jamie on 9/21/2016.
 */
public class GroupAdminActivity extends AppCompatActivity  {

    private static final String TAG = "GroupAdminActivity";
    ArrayList<Item> items = new ArrayList<Item>();


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
    String memberName;
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
    Context context;
    Boolean[] memberChecked = new Boolean[50];
    String groupList = "";
    String memberId;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        final String TAG = "MA/onSaveInstanceState";
        savedInstanceState.putFloat("ZOOMLEVEL", zoomLevel);
        savedInstanceState.putDouble("LATITUDE", dlatitude);
        savedInstanceState.putDouble("LONGITUDE",dlongitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Log.d(TAG, "Starting GroupAdminActivity");
        context = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();

        Bundle extras = getIntent().getExtras();
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");
        memberName = extras.getString("MEMBER_NAME");
        memberId = extras.getString("MEMBER_ID");
        String group_url = "";
        String rocketTitle = "";
        if (group_relation.equals("OWNER")) {
            setContentView(R.layout.groups_activity_owner);
            rocketTitle = "My Groups";
        } else if (group_relation.equals("MEMBER_OWNER")) {
            setContentView(R.layout.groups_activity_owner);
            TextView tv2 = (TextView) findViewById(R.id.textView5);
            tv2.setText("Tap name to select");
            rocketTitle = memberName + "'s Groups";
        } else if (group_relation.equals("MEMBER") || (group_relation.equals("ALL_MEMBERS"))) {
            setContentView(R.layout.groups_activity_member);
            rocketTitle = "Groups I'm In";
        }


        Toolbar rocketToolbar = (Toolbar) findViewById(R.id.rocket_toolbar);
        setSupportActionBar(rocketToolbar);
        rocketToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(rocketTitle);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rocketToolbar.setNavigationIcon(R.drawable.ic_rocket_arrow);



        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");
        if (group_relation.equals("OWNER")) {
            group_url = "http://www.sandbistro.com/signalrocket/getOwnedGroups.php?group_owner_id=" + myUserID;
        } else if (group_relation.equals("MEMBER") || (group_relation.equals("ALL_MEMBERS"))) {
            group_url = "http://www.sandbistro.com/signalrocket/getMemberGroups.php?user_id=" + myUserID;
        } else if (group_relation.equals("MEMBER_OWNER")) {
            group_url = "http://www.sandbistro.com/signalrocket/getOwnedGroups.php?group_owner_id=" + memberId;
        }

        new DownloadTask().execute(group_url);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("GroupAdmin Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(GroupAdminActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
            dialog = new ProgressDialog(GroupAdminActivity.this,R.style.RocketSpinner);
            dialog.setCancelable(false);
            dialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            dialog.show();

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

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            prefsEditor.putString("userData", userData);
            prefsEditor.commit();

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
                    Log.d(TAG, "Json array length = 0");
                    GoHome();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Json exception");
                GoHome();
            }
            //jsonMainNode = jsonResponse.optJSONArray("AllMyGroups");
            /****************** End Parse Response JSON Data *************/


            values = new String[lengthJsonArr];
            group_ids = new String[lengthJsonArr];
            JSONObject jObject = null;
            String saved_owner_id = "";

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                try {
                    jObject = jArray.getJSONObject(i);
                    values[i] = jObject.getString("name");
                    group_ids[i] = jObject.getString("id");
                    try {
                        String owner_name = jObject.getString("owner_name");
                        String owner_id = jObject.getString("owner_id");
                        if (!owner_id.equals(saved_owner_id)) {
                            items.add(new SectionItem(owner_name));
                            saved_owner_id = owner_id;
                        }
                        items.add(new EntryItem(values[i], group_ids[i]));
                        values[i] = owner_name + "\n   " + values[i];
                       // prefsEditor.putString(values[i], group_ids[i]);
                        prefsEditor.putString(group_ids[i], values[i]);
                        if (!prefsEditor.commit()) {
                            Toast.makeText(context, "Groups save failed", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        items.add(new EntryItem(values[i], group_ids[i]));
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
                    Log.d(TAG, "Json exception 2");
                    GoHome();

                }
            }

            listView = (ListView) findViewById(R.id.mobile_list);  // layout groups_activity_member has this view
           // ArrayAdapter adapter = new ArrayAdapter<String>(GroupAdminActivity.this, R.layout.group_activity_listview, values);
            EntryAdapter adapter = new EntryAdapter(GroupAdminActivity.this, items);

            listView.setAdapter(adapter);
            registerForContextMenu(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                EntryItem item = null;

                if(!items.get(position).isSection()){

                    item = (EntryItem)items.get(position);
                }

                // ListView Clicked item index
                if (memberChecked[position] == null) {
                    memberChecked[position] = true;
                } else if (memberChecked[position]) {
                    memberChecked[position] = false;
                } else if (!memberChecked[position]) {
                    memberChecked[position] = true;
                }
                if (memberChecked[position]) {
                    groupList = groupList + item.subtitle + ":";
                    listView.getChildAt(position).setBackgroundColor(Color.parseColor("#aaaaaa"));
                } else if (!memberChecked[position]) {
                    groupList = groupList.replace(item.subtitle, "");
                    listView.getChildAt(position).setBackgroundColor(Color.TRANSPARENT);
                }
                }
            });
        }

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
                url = new URL("http://www.sandbistro.com/signalrocket/createGroup.php?user_id=" + userId + "&group_name=" + groupName );
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
            Intent groupIntent = new Intent(getApplicationContext(), GroupAdminActivity.class);
            groupIntent.putExtra("ZOOMLEVEL", zoomLevel);
            groupIntent.putExtra("LATITUDE", dlatitude);
            groupIntent.putExtra("LONGITUDE", dlongitude);
            groupIntent.putExtra("GROUP_RELATION", group_relation);

            startActivity(groupIntent);
            finish();
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
                    break;

                    case "show":
                        url = new URL("http://www.sandbistro.com/signalrocket/showMemberInGroup.php?user_id=" + user_id + "&groupList=" + groupIDs);
                    break;

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
        if (! group_relation.equals("MEMBER_OWNER")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.group_long_press_menu, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem mItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) mItem.getMenuInfo();
        EntryItem item = null;

        if(!items.get(info.position).isSection()){

            item = (EntryItem)items.get(info.position);
        }


        switch (mItem.getItemId()) {
            case R.id.list_members:
                Intent useAdminIntent = new Intent(this, UserAdminActivity.class);
                useAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
                useAdminIntent.putExtra("LATITUDE", dlatitude);
                useAdminIntent.putExtra("LONGITUDE", dlongitude);
                useAdminIntent.putExtra("GROUP_ID", item.subtitle);
                useAdminIntent.putExtra("GROUP_NAME", item.title);
                useAdminIntent.putExtra("GROUP_RELATION", group_relation);
                startActivity(useAdminIntent);
                finish();

                return true;

            case R.id.make_active:
                myGroupID = item.subtitle;
                myGroupName = item.title;
                Toast.makeText(this, "group id = " + myGroupID + " group name = " + myGroupName, Toast.LENGTH_LONG).show();
                prefsEditor.putString("myGroupID", myGroupID);
                prefsEditor.putString("myGroupName", myGroupName);
                prefsEditor.commit();
                GoHome();
                return true;
            default:
                return super.onContextItemSelected(mItem);
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

    // calculate the zoom level to get all members on one map
    private float getZoomLevel(String distance) {

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
                Toolbar rocketToolbar = (Toolbar) findViewById(R.id.rocket_toolbar);
                setSupportActionBar(rocketToolbar);
                rocketToolbar.setTitleTextColor(Color.WHITE);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                rocketToolbar.setNavigationIcon(R.drawable.ic_rocket_arrow);


                // Button new_group = (Button) findViewById(R.id.new_group_button);
                final EditText new_group_name = (EditText) findViewById(R.id.new_group_name);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(new_group_name, InputMethodManager.SHOW_IMPLICIT);
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
                doSomethingWithMe("hide", groupList);
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

}
