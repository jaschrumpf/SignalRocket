package com.gammazero.signalrocket;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Jamie on 11/21/2016.
 */

public class InvitationActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private Button shareIntent;
    private Button send;
    private EditText phoneNo;
    private TextView messageBody;
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
            Toolbar rocketToolbar = (Toolbar) findViewById(R.id.rocket_toolbar);
            setSupportActionBar(rocketToolbar);
            rocketToolbar.setTitleTextColor(Color.WHITE);
            getSupportActionBar().setTitle("Invitations");
            //rocketToolbar.setNavigationIcon(R.drawable.ic_drawer);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            final EditText invitation_code = (EditText) findViewById(R.id.invitation_code);

            invitation_code.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        handled = true;
                        String invitation_number = invitation_code.getText().toString();
                        String[] new_member_info = new String[2];
                        new ProcessInvitation().execute(invitation_number);
                    }
                    return handled;
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
                Toolbar rocketToolbar = (Toolbar) findViewById(R.id.rocket_toolbar);
                setSupportActionBar(rocketToolbar);
                rocketToolbar.setTitleTextColor(Color.WHITE);
                getSupportActionBar().setTitle("Invitations");
                //rocketToolbar.setNavigationIcon(R.drawable.ic_drawer);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);


       //         setContentView(R.layout.invitation_activity);
                String SENT = "sent";
                String DELIVERED = "delivered";
                Intent sentIntent = new Intent(SENT);
                /*Create Pending Intents*/
                final PendingIntent sentPI = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, sentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Intent deliveryIntent = new Intent(DELIVERED);

                final PendingIntent deliverPI = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, deliveryIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                registerReceiver(new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String result = "";

                        switch (getResultCode()) {

                            case Activity.RESULT_OK:
                                result = "Transmission successful";
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                result = "Transmission failed";
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                result = "Radio off";
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                result = "No PDU defined";
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                result = "No service";
                                break;
                        }

                        Toast.makeText(getApplicationContext(), result,
                                Toast.LENGTH_LONG).show();
                    }

                }, new IntentFilter(SENT));

                registerReceiver(new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Toast.makeText(getApplicationContext(), "Deliverd",
                                Toast.LENGTH_LONG).show();
                    }

                }, new IntentFilter(DELIVERED));



                phoneNo = (EditText) findViewById(R.id.mobileNumber);
                phoneNo.setText(number, TextView.BufferType.EDITABLE);
                sms = "Please join my group, \"" + myGroupName + "\", by opening SignalRocket and entering the six-digit " +
                        "number below on the Join A Group page.\n\nYour join code is " + joinCode;
                messageBody = (TextView) findViewById(R.id.smsBody);
                messageBody.setText(sms, TextView.BufferType.EDITABLE);

                send = (Button) findViewById(R.id.send);
                send.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        number = phoneNo.getText().toString();

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
                            Toast.makeText(getApplicationContext(), "SMS Sent!",
                                    Toast.LENGTH_LONG).show();
                            // return to main activity
                            Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                            mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                            mapsIntent.putExtra("LATITUDE", dlatitude);
                            mapsIntent.putExtra("LONGITUDE", dlongitude);

                            startActivity(mapsIntent);

                        }
                    } else {
                           SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(number, null, sms, sentPI, deliverPI);

                        Toast.makeText(getApplicationContext(), "SMS Sent!",
                                Toast.LENGTH_LONG).show();
                        // return to main activity
                        Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                        mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                        mapsIntent.putExtra("LATITUDE", dlatitude);
                        mapsIntent.putExtra("LONGITUDE", dlongitude);

                        startActivity(mapsIntent);

                         /*Send SMS*/
                         //  sendSMS(number, sms);
                    //    Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
                //        smsIntent.putExtra("sms_body", sms);
                //        smsIntent.setType("vnd.android-dir/mms-sms");
                //        startActivity(smsIntent);
                    }

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
                        Toast.makeText(getApplicationContext(), "SMS Sent!",
                                Toast.LENGTH_LONG).show();
                        // return to main activity
                        Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                        mapsIntent.putExtra("ZOOMLEVEL", zoomLevel);
                        mapsIntent.putExtra("LATITUDE", dlatitude);
                        mapsIntent.putExtra("LONGITUDE", dlongitude);

                        startActivity(mapsIntent);

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
        }
        return true;
    }

    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}

