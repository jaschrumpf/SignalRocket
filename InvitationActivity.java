package com.gammazero.signalrocket;

import android.Manifest.permission;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.prefs.Preferences;

import static android.Manifest.permission.SEND_SMS;

/**
 * Created by Jamie on 11/21/2016.
 */

public class InvitationActivity extends Activity {

    private static final int REQUEST_CODE = 1;
    private Button shareIntent;
    private Button send;
    private EditText phoneNo;
    private EditText messageBody;
    SharedPreferences appPrefs;
    SharedPreferences.Editor prefsEditor;
    String myGroupName;
    String myUserID;
    final String TAG = "InvitationActivity";
    Float zoomLevel;
    Double dlatitude;
    Double dlongitude;
    public static final int MY_PERMISSIONS_REQUEST_SMS_SEND = 0;
    Context context;
    String number = "";
    String sms = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Starting InvitationActivity");
        context = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = appPrefs.edit();
        myGroupName = appPrefs.getString("myGroupName", "");
        String myGroupID = appPrefs.getString("myGroupID", "");
        myUserID = appPrefs.getString("myUserID", "");
        Bundle extras = getIntent().getExtras();
        String request_type = extras.getString("REQUEST_TYPE");
        zoomLevel = extras.getFloat("ZOOMLEVEL");
        dlatitude = extras.getDouble("LATITUDE");
        dlongitude = extras.getDouble("LONGITUDE");
        if (request_type.equals("SEND")) {

            new GetInvitationId().execute(myGroupID);

        } else if (request_type.equals("RECEIVE")) {

            setContentView(R.layout.receive_invitation_activity);
            Button invite = (Button) findViewById(R.id.process_invitation);
            final EditText invitation_code = (EditText) findViewById(R.id.invitation_code);
            invite.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String invitation_number = invitation_code.getText().toString();
                    new ProcessInvitation().execute(invitation_number);
                }

            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = intent.getData();
                String[] projection = {Phone.NUMBER, Phone.DISPLAY_NAME};

                Cursor cursor = getContentResolver().query(uri, projection,
                        null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(Phone.NUMBER);
                number = cursor.getString(numberColumnIndex);
                phoneNo = (EditText) findViewById(R.id.mobileNumber);
                if (number != null) {
                    phoneNo.setText(number, TextView.BufferType.EDITABLE);
                }

                int nameColumnIndex = cursor.getColumnIndex(Phone.DISPLAY_NAME);
                String name = cursor.getString(nameColumnIndex);

            }
        }
    }

    ;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public class GetInvitationId extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
        }

        protected String doInBackground(String[] parms) {

            URL url = null;
            String myGroupID = parms[0];
            String data = "";

            try {
                String myUserName = URLEncoder.encode(parms[0], "UTF-8");
                url = new URL("http://www.sandbistro.com/signalrocket/getInvitationNumber.php?group_id=" + myGroupID);
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
                data = sb.toString();

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            return data;
        }

        protected void onPostExecute(String joinCode) {

            if (!joinCode.equals("Error")) {

                Uri uri = Uri.parse("content://contacts");
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                intent.setType(Phone.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_CODE);

                setContentView(R.layout.invitation_activity);

                phoneNo = (EditText) findViewById(R.id.mobileNumber);
                phoneNo.setText(number, TextView.BufferType.EDITABLE);
                messageBody = (EditText) findViewById(R.id.smsBody);
                messageBody.setText("Please join my group, \"" + myGroupName + "\", by opening your SignalRocket app and entering the six-digit " +
                        "number below on the Join A Group page.\n\nYour join code is " + joinCode, TextView.BufferType.EDITABLE);

                send = (Button) findViewById(R.id.send);
                send.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        number = phoneNo.getText().toString();
                        sms = messageBody.getText().toString();

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ContextCompat.checkSelfPermission(InvitationActivity.this,
                                        android.Manifest.permission.SEND_SMS)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    // Asking user if explanation is needed
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(InvitationActivity.this,
                                            android.Manifest.permission.SEND_SMS)) {

                                        //Prompt the user once explanation has been shown
                                        ActivityCompat.requestPermissions(InvitationActivity.this,
                                                new String[]{android.Manifest.permission.SEND_SMS},
                                                MY_PERMISSIONS_REQUEST_SMS_SEND);


                                    } else {
                                        // No explanation needed, we can request the permission.
                                        ActivityCompat.requestPermissions(InvitationActivity.this,
                                                new String[]{android.Manifest.permission.SEND_SMS},
                                                MY_PERMISSIONS_REQUEST_SMS_SEND);
                                    }
                                } else {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(number, null, sms, null, null);
                                }
                            } else {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(number, null, sms, null, null);
                            }
                            Toast.makeText(getApplicationContext(), "SMS Sent!",
                                    Toast.LENGTH_LONG).show();
                            // return to main activity
                            Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                            mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                            mapsIntent.putExtra("LATITUDE", dlatitude);
                            mapsIntent.putExtra("LONGITUDE", dlongitude);

                            startActivity(mapsIntent);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "SMS failed, please try again later!",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Error getting invitation number", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ProcessInvitation extends AsyncTask<String, Void, String> {

        URL url;
        String result = "";


        public void onPreExecute() {

            super.onPreExecute();
        }

        public String doInBackground(String[] parms) {

            String invitation_number = parms[0];
            String data = "";

            try {
                String myUserName = URLEncoder.encode(parms[0], "UTF-8");
                url = new URL("http://www.sandbistro.com/signalrocket/acceptInvitation.php?invite_id=" + invitation_number + "&user_id=" + myUserID);
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
                data = sb.toString();

            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }
            return data;

        }

        public void onPostExecute(String result) {

            if (result.equals("Error")) {
                Toast.makeText(getApplicationContext(), "Error adding to group", Toast.LENGTH_LONG).show();
            } else if (result.equals("Invalid")) {
                Toast.makeText(getApplicationContext(), "Invalid invitation number", Toast.LENGTH_LONG).show();
            } else if (result.equals("Already")) {
                Toast.makeText(getApplicationContext(), "Invitation number already used", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Added to group!", Toast.LENGTH_LONG).show();
                Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                mapsIntent.putExtra("LATITUDE", dlatitude);
                mapsIntent.putExtra("LONGITUDE", dlongitude);
                startActivity(mapsIntent);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SMS_SEND: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(number, null, sms, null, null);
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}

