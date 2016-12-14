package com.gammazero.signalrocket;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Jamie on 9/20/2016.
 */
public class AppPreferencesFragment extends PreferenceFragment {

    final String TAG = "AppPreferencesFragment";


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.fragment_preferences);
        SharedPreferences fragPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.OnSharedPreferenceChangeListener rocketPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            //    Toast.makeText(getActivity(), "Shared fragment preference changed:" + key, Toast.LENGTH_LONG).show();
                if (key.equals("myUserName")) {
                    String myUserName = prefs.getString(key, "Unknown");
                }
            }
        };
        fragPrefs.registerOnSharedPreferenceChangeListener(rocketPrefsListener);
        SharedPreferences.Editor prefsEditor = fragPrefs.edit();


        EditText editText = new EditText(getActivity());
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

    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
         //   PreferenceManager.setDefaultValues(getActivity(),
       //            R.xml.advanced_preferences, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.location_preferences);
        }

    }

    public void onPreferenceChange(Preference p, Object ob) {
        Toast.makeText(getActivity(), "Preference changed in fregment", Toast.LENGTH_LONG).show();

    }


}
