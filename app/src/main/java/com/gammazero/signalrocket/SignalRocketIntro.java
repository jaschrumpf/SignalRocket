package com.gammazero.signalrocket;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.VideoView;


/**
 * Created by Jamie on 9/15/2016.
 */
public class SignalRocketIntro extends Activity{

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_startup);

        }
 //           VideoView myVideoView = (VideoView)findViewById(R.id.videoview);
 //           String SrcPath = "android.resource://net.hilltopper.gamewatch/raw/" + R.raw.gamewatch_intro;
//            myVideoView.setVideoURI(Uri.parse(SrcPath));
//            myVideoView.requestFocus();
 //           myVideoView.start();
 //           myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()  {
                public void onCompletion(MediaPlayer arg0) {
                    Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                    startActivity(intent);
                    finish();
//                }
 //           });
        }
    }
