package com.gammazero.signalrocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.gammazero.signalrocket.EntryAdapter;
import com.gammazero.signalrocket.EntryItem;
import com.gammazero.signalrocket.Item;
import com.gammazero.signalrocket.SectionItem;

/**
 * Created by Jamie on 1/2/2017.
 */

public class GroupAdminFragment extends ListFragment {

    String[] values;
    String[] group_ids;
    JSONArray jsonMainNode = null;
    JSONObject jsonResponse = null;
    JSONArray jArray = null;
    int lengthJsonArr = 0;
    final String TAG = "GroupAdminFragment";
    SharedPreferences appPrefs;
    SharedPreferences.Editor prefsEditor;
    String userData;
    MenuInflater inflater = new MenuInflater(getActivity());
    ArrayList<Item> items = new ArrayList<Item>();
    String group_type = "";
    float zoomLevel = 0f;
    Double dlatitude = 0.0;
    Double dlongitude = 0.0;
    String group_relation = "";
    String myUserID;
    String myUserName;
    String myGroupID;
    String myGroupName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getActivity().getIntent().getExtras();
        group_type = extras.getString("GROUP_TYPE");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        group_relation = extras.getString("GROUP_RELATION");


        setHasOptionsMenu(true);

        appPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefsEditor = appPrefs.edit();
        userData = appPrefs.getString("userData", "");


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
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Json exception");
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
                   // values[i] = owner_name + "\n   " + values[i];
                  //  prefsEditor.putString(values[i], group_ids[i]);
                    prefsEditor.putString(group_ids[i], values[i]);
                    if (!prefsEditor.commit()) {
                        Toast.makeText(getActivity(), "Groups save failed", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    values[i] = values[i];
                    prefsEditor.putString(values[i], group_ids[i]);
                    if (!prefsEditor.commit()) {
                        Toast.makeText(getActivity(), "Groups save failed", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (NullPointerException npe) {
                Log.e(TAG, npe.getMessage());
                values[i] = "Not currently a member of any groups";
                group_ids[i] = null;


            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                Log.d(TAG, "Json exception 2");

            }
        }
        EntryAdapter adapter = new EntryAdapter(getActivity(), items);
        setListAdapter(adapter);

    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if(!items.get(position).isSection()){

            EntryItem item = (EntryItem)items.get(position);

            Toast.makeText(getActivity(), "You clicked " + item.subtitle , Toast.LENGTH_SHORT).show();


        }

        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
   //     MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_long_press_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.list_members:
                Intent useAdminIntent = new Intent(getActivity(), UserAdminActivity.class);
                useAdminIntent.putExtra("ZOOMLEVEL", zoomLevel);
                useAdminIntent.putExtra("LATITUDE", dlatitude);
                useAdminIntent.putExtra("LONGITUDE", dlongitude);
                useAdminIntent.putExtra("GROUP_ID", group_ids[info.position]);
                useAdminIntent.putExtra("GROUP_NAME", values[info.position]);
                useAdminIntent.putExtra("GROUP_RELATION", group_relation);
                useAdminIntent.putExtra("GROUP_TYPE", group_type);
                startActivity(useAdminIntent);

                return true;

            case R.id.make_active:
                myGroupID = group_ids[info.position];
                myGroupName = values[info.position];
                Toast.makeText(getActivity(), "group id = " + myGroupID + " group name = " + myGroupName, Toast.LENGTH_LONG).show();
                prefsEditor.putString("myGroupID", myGroupID);
                prefsEditor.putString("myGroupName", myGroupName);
                prefsEditor.commit();
                Intent mainIntent = new Intent(getActivity(), MapsActivity.class);
                mainIntent.putExtra("ZOOMLEVEL", zoomLevel);
                mainIntent.putExtra("LATITUDE", dlatitude);
                mainIntent.putExtra("LONGITUDE", dlongitude);
                mainIntent.putExtra("GROUP_ID", group_ids[info.position]);
                mainIntent.putExtra("GROUP_NAME", values[info.position]);
                mainIntent.putExtra("GROUP_RELATION", group_relation);
                mainIntent.putExtra("GROUP_TYPE", group_type);
                startActivity(mainIntent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
