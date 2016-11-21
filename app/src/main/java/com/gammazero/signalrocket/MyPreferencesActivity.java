package com.gammazero.signalrocket;

/**
 * Created by Jamie on 9/15/2016.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;

import java.util.List;

public class MyPreferencesActivity extends PreferenceActivity {

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public void onBuildHeaders(List<Header> target)
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    public void modifyUserName (View view) {
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = appPrefs.edit();
        final EditText my_user_name = (EditText) findViewById(R.id.new_user_name);
        String myUserName = my_user_name.getText().toString();
        prefsEditor.putString("myUserName", myUserName);
        prefsEditor.commit();


    }

    public void onPreferenceChange(View view) {

    }
}

