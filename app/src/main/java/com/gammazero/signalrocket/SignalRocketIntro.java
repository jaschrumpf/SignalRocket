package com.gammazero.signalrocket;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.Timer;


/**
 * Created by Jamie on 9/15/2016.
 */
public class SignalRocketIntro extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.logo);

        ImageView rocketImageView = (ImageView) findViewById(R.id.logo);
        rocketImageView.setImageResource(R.drawable.signal_rocket_logo);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent startupIntent = new Intent(getBaseContext(), StartupActivity.class);
                startActivity(startupIntent);
                finish();
            }
        }, 3000);
    }
}
