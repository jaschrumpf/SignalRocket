package com.gammazero.signalrocket;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Jamie on 12/3/2016.
 */

public class SendGroupMessageActivity extends Activity {

    SharedPreferences appPrefs;
    SharedPreferences.Editor prefsEditor;
    Float zoomLevel;
    Double dlatitude;
    Double dlongitude;
    Bundle extras;
    String myUserID;
    String myUserName;
    String myGroupID;
    String myGroupName;
    String data = "";
    Menu menu;
    final String TAG = "SendGroupMessage";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting SendGroupMessageActivity");
        Bundle extras = getIntent().getExtras();
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();
        myUserName = appPrefs.getString("myUserName", "");
        myUserID = appPrefs.getString("myUserID", "");
        myGroupName = appPrefs.getString("myGroupName", "");
        myGroupID = appPrefs.getString("myGroupID", "");

        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        setContentView(R.layout.send_group_message_activity);
        Button send_it = (Button) findViewById(R.id.send_it);
        final EditText my_message = (EditText) findViewById(R.id.message_body);
        send_it.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String myMessage = my_message.getText().toString();

                new SendMessage().execute(myMessage);
            }

        });
    }
    public class SendMessage extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... parms) {
            String message = parms[0];
            String result = "";
            BufferedReader reader;

            try {
                // initialize the dialog
                String data = "";
                    data = URLEncoder.encode("message", "UTF-8")
                            + "=" + URLEncoder.encode(message, "UTF-8");
                    data += "&" + URLEncoder.encode("user_id", "UTF-8") + "="
                            + URLEncoder.encode(myUserID, "UTF-8");

                URL url = new URL("http://www.sandbistro.com/signalrocket/sendMessage.php");
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

        @Override
        protected void onPostExecute(String result) {

            if (result.startsWith("Success")) {
                Toast.makeText(getApplicationContext(), "Message sent successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Message failed", Toast.LENGTH_LONG).show();
            }
            Intent mainIntent = new Intent(getApplicationContext(), MapsActivity.class);
            mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
            mainIntent.putExtra("LATITUDE", dlatitude);
            mainIntent.putExtra("LONGITUDE", dlongitude);
            startActivity(mainIntent);


        }
    }

    //==================================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        updateMenuTitles();
        return true;
    }

    private void updateMenuTitles() {
        MenuItem groupMenuItem = menu.findItem(R.id.current_group);
        groupMenuItem.setTitle("Current group is " + myGroupName);
        groupMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {


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
                startActivity(mainIntent);

        }

        return true;
    }
    //==================================================================================================
}
