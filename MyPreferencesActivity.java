package com.gammazero.signalrocket;

/**
 * Created by Jamie on 9/15/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MyPreferencesActivity extends AppCompatPreferenceActivity{

    Menu menu;
    String myUserID = "";
    String myUserName = "";
    String myGroupID = "";
    String myGroupName = "";
    String prefMapType = "";
    String myDistInt = "";
    String myTimeInt = "";
    Float zoomLevel;
    Double dlatitude;
    Double dlongitude;
    final String TAG = "MyPreferencesActivity";
    Context context;
    private AppCompatDelegate mDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

 //       getDelegate().onPostCreate(savedInstanceState);
 //             Toolbar rocketToolbar = (Toolbar) findViewById(R.id.rocket_toolbar);
  //            setSupportActionBar(rocketToolbar);
 //             rocketToolbar.setTitleTextColor(Color.WHITE);
 //             getSupportActionBar().setTitle("Settings");
        //rocketToolbar.setNavigationIcon(R.drawable.ic_drawer);
 //             getSupportActionBar().setDisplayShowHomeEnabled(true);
//              getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar rocketToolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar_settings, root, false);
        rocketToolbar.setTitle("Settings");
        rocketToolbar.setTitleTextColor(Color.WHITE);
        rocketToolbar.setNavigationIcon(R.drawable.ic_white_back_arrow_97dp);
        root.addView(rocketToolbar, 0); // insert at top
        Drawable myArrow = rocketToolbar.getNavigationIcon();
        //      String myArrowText = myArrow.toString();
        rocketToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FragmentManager fMgr = getFragmentManager();
        context = this;
        Log.d(TAG, "Starting MyPreferencesActivity");

//        String group_type = extras.getString("GROUP_TYPE");
        //  myGroupName = extras.getString("GROUP_NAME");
       //  myGroupID = extras.getString("GROUP_ID");
        Bundle extras = getIntent().getExtras();
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");

        final SharedPreferences rocketPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener rocketPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                if (key.equals("myUserName")) {

                    myUserID = rocketPrefs.getString("myUserID", "");
                    String newUserName = rocketPrefs.getString("myUserName", "");
                    String[] newUserInfo = new String[2];
                    newUserInfo[0] = myUserID;
                    newUserInfo[1] = newUserName;
                    new ChangeUserName().execute(newUserInfo);

                }
                Intent mainIntent = new Intent(context, MapsActivity.class);
                startActivity(mainIntent);

            }
        };

        rocketPrefs.registerOnSharedPreferenceChangeListener(rocketPrefsListener);
        rocketPrefs.edit().putString("zoomLevel", zoomLevel.toString());
        rocketPrefs.edit().putString("latitude", dlatitude.toString());
        rocketPrefs.edit().putString("longitude", dlongitude.toString());
        rocketPrefs.edit().commit();


        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");

        EditText editText = new EditText(this);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
                Log.d(TAG, "onEditorAction() called");
                return false;
            }

        });
    }

    public void setOnEditorActionListener (TextView.OnEditorActionListener l) {

        Log.d(TAG, "onEditorAction() called");
    }

    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preferences, target);

    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        //  return AppPreferencesFragment.class.getName().contains(fragmentName);
        return fragmentName.contains (AppPreferencesFragment.class.getName());
    }

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
                startActivity(preferencesIntent);
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


    public class ChangeUserName extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... parms) {
            String result = "";
            URL url = null;
            String data = "";
            String myId = parms[0];
            String newName = parms[1];

            try {

                url = new URL("http://www.sandbistro.com/signalrocket/changeUserName.php?user_id=" +  myId + "&user_name=" + newName);
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
                result = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute () {
            Intent mainIntent = new Intent(context, MapsActivity.class);
            startActivity(mainIntent);
        }
    }


    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

  }


