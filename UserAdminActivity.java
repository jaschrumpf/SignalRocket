package com.gammazero.signalrocket;

import android.app.ProgressDialog;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Jamie on 11/25/2016.
 */

public class UserAdminActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
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
    String group_type = "";
    String membersList = "";
    String group_name = "";
    ArrayList<Item> items = new ArrayList<Item>();

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        extras.putString("GROUP_NAME", group_name);
        extras.putString("GROUP_ID", group_id);
        extras.putFloat("ZOOMLEVEL", zoomLevel);
        extras.putDouble("LATITUDE", dlatitude);
        extras.putDouble("LONGITUDE", dlongitude);
        extras.putString("GROUP_RELATION", group_relation);
        extras.putString("GROUP_TYPE", group_type);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Log.d(TAG, "Starting UserAdminActivity");
        Bundle extras = getIntent().getExtras();
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();

        group_name = extras.getString("GROUP_NAME");
        group_id = extras.getString("GROUP_ID");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");

        if (group_relation.equals("OWNER") || group_relation.equals("ALL_MEMBERS") || (group_relation.equals("MEMBER_OWNER"))) {
            setContentView(R.layout.user_activity_owner);
        } else if (group_relation.equals("MEMBER")) {
            setContentView(R.layout.user_activity_member);

        }
        Toolbar rocketToolbar = (Toolbar) findViewById(R.id.rocket_toolbar);
        setSupportActionBar(rocketToolbar);
        rocketToolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rocketToolbar.setNavigationIcon(R.drawable.ic_rocket_arrow);
        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");

        TextView tv;
        String gMembers = "";
        tv=(TextView)findViewById(R.id.display_group);

        // set up the calls to get group members or get all members
        if (group_relation.equals("OWNER") || group_relation.equals("MEMBER") || (group_relation.equals("MEMBER_OWNERS"))) {
            tv.setText("Members of\n     " + group_name);
            gMembers = "http://www.sandbistro.com/signalrocket/getGroupMembers.php?group_id=" + group_id;
        } else {
            tv.setTextSize(15);
            tv.setPadding(40, 10,0,0);
            tv.setText("All the members of every group you're in\n(excpt the ones hiding from you)");
            tv.setHint("Tap name to select, long press to bring up options menu");
            getSupportActionBar().setTitle("All Members");
            gMembers = "http://www.sandbistro.com/signalrocket/getAllMyMembers.php?user_id=" + myUserID;
        }

        new UserAdminActivity.DownloadTask().execute(gMembers);
    }

public class DownloadTask extends AsyncTask<String, Void, String> {

    private ProgressDialog dialog;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // initialize the dialog
      // this.dialog.setMessage("Please wait... Updating locations");
        dialog = new ProgressDialog(UserAdminActivity.this,R.style.RocketSpinner);
        dialog.setCancelable(false);
        dialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        dialog.show();

    }

    @Override
    protected String doInBackground(String... parms) {


        String Content = "";
        URL url = null;

        try {
            url = new URL(parms[0]);
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
            String group_count = "";
            String isActive = "";

            for (int i = 0; i < lengthJsonArr; i++) {
                /****** Get Object for each JSON node.***********/
                try {
                    jObject = jArray.getJSONObject(i);
                    String member_name = jObject.getString("name");
                    String member_id = jObject.getString("id");
                    if (group_relation.equals("ALL_MEMBERS")) {
                        group_count = jObject.getString("group_count");
                    } else {
                        isActive = jObject.getString("active");
                    }
                    values[i] = member_name;
                    member_ids[i] = member_id;
                    items.add(new EntryItem(values[i], group_count));
                } catch (JSONException e) {
               //     Log.e(TAG, e.getMessage());
                    GoHome();

                }

            }
            listView = (ListView) findViewById(R.id.user_list);   // in layout user_activity_owner
          //  ArrayAdapter adapter = new ArrayAdapter<String>(getApplication(), R.layout.user_activity_listview, values);
            if (group_relation.equals("ALL_MEMBERS")) {
                AllMembersEntryAdapter adapter = new AllMembersEntryAdapter(UserAdminActivity.this, items);
                listView.setAdapter(adapter);
            } else if (group_relation.equals("OWNER") || (group_relation.equals("MEMBER"))) {
                ArrayAdapter adapter = new ArrayAdapter<String>(getApplication(), R.layout.user_activity_listview, values);
                listView.setAdapter(adapter);
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemID = item.getItemId();
        switch (item.getItemId()) {

            case 16908332:
                Intent mainIntent = new Intent(this, MapsActivity.class);
                mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
                mainIntent.putExtra("LATITUDE", dlatitude);
                mainIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(mainIntent);
                return true;

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

            case R.id.hide_me_from_member:
                doSomethingWithMe("hide",membersList);
                return true;

            case R.id.unhide_me:
                doSomethingWithMe("show", membersList);
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
    //==================================================================================================
    public void doSomethingWithMe(String action, String members) {

        String[] doList = new String[3];
        if (members.equals("")) {
            Toast.makeText(this, "No groups selected", Toast.LENGTH_LONG).show();
        } else {
            doList[0] = action;
            doList[1] = myUserID;
            doList[2] = members;
            new DoSomethingWithMe().execute(doList);

        }
    }

    protected class DoSomethingWithMe extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... parms) {
            String result = "";
            BufferedReader reader;
            String userAction = parms[0];
            String user_id = parms[1];
            String memberIds = parms[2];

            try {
                // initialize the dialog
                String data = "";
                String groupIDs = URLEncoder.encode(memberIds, "UTF-8");
                URL url = null;

                switch (userAction) {
                    case "hide":
                        url = new URL("http://www.sandbistro.com/signalrocket/hideFromMembers.php?user_id=" + user_id + "&group_id=" + group_id + "&memberList=" + groupIDs);
                    break;

                    case "show":
                        url = new URL("http://www.sandbistro.com/signalrocket/unHideFromMembers.php?user_id=" + user_id + "&memberList=" + groupIDs);

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
        if (! group_relation.equals("OWNER") || (!group_relation.equals("MEMBER"))) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_long_press_menu, menu);
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
        String member_id = member_ids[info.position];

        switch (mItem.getItemId()) {
            case R.id.list_members_groups:
                Intent useAdminIntent = new Intent(this, GroupAdminActivity.class);
                useAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
                useAdminIntent.putExtra("LATITUDE", dlatitude);
                useAdminIntent.putExtra("LONGITUDE", dlongitude);
                useAdminIntent.putExtra("MEMBER_ID", member_id);
                useAdminIntent.putExtra("MEMBER_NAME", item.title);
                useAdminIntent.putExtra("GROUP_RELATION", "MEMBER_OWNER");
                startActivity(useAdminIntent);
                finish();

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

}

