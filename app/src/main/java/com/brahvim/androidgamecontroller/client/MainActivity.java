package com.brahvim.androidgamecontroller.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import processing.android.CompatUtils;
import processing.android.PFragment;

public class MainActivity extends AppCompatActivity {
    public static AppCompatActivity appAct;
    public static SketchWithScenes sketch;
    public static PFragment fragment;
    public static FrameLayout frame;

    // Note: calling the super class's methods first is a good idea, IF
    // their functioning / 'default behvaior' does not interfere with yours.

    // Since keeping a `static` field holding `Context`s causes a (very large) memory leak,
    // let's use a getter! OOP kept me safe :>
    public static Context getContext() {
        return MainActivity.appAct.getApplicationContext();
    }

    public void keepScreenOn(boolean p_status) {
        if (p_status)
            this.getWindow().addFlags(
              android.view.WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            this.getWindow().clearFlags(
              android.view.WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // region "Processing defaults".
    @Override
    // Let this one remain `protected`. DON'T RESTART DA APP! WAAAH!
    protected void onCreate(Bundle p_savedInstanceState) {
        super.onCreate(p_savedInstanceState);

        // Thanks to the Fisica library's "AndroidCanica" example!:
        // Fix to prevent screen from
        // sleeping when app is active:
        //this.whateverTurnItOn(true); // Let's not do it right now, okay?

        // Remove the flag when AGC is disconnected or something! Waaah! Battery life!!!

        // NEVER do this in actual networking apps! AGC is snappy since it is on LAN...

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        MainActivity.frame = new FrameLayout(this);
        MainActivity.frame.setId(CompatUtils.getUniqueViewId());
        super.setContentView(MainActivity.frame,
          new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));

        MainActivity.sketch = new SketchWithScenes();
        MainActivity.fragment = new PFragment(MainActivity.sketch);
        MainActivity.fragment.setView(MainActivity.frame, this);
        MainActivity.appAct = this;

        System.out.println("The Sketch should start now.");
    }

    @Override
    public void onRequestPermissionsResult(
      int p_requestCode, @NonNull String[] p_permissions, @NonNull int[] p_grantResults) {
        super.onRequestPermissionsResult(p_requestCode, p_permissions, p_grantResults);

        if (sketch != null)
            sketch.onRequestPermissionsResult(p_requestCode, p_permissions, p_grantResults);
    }

    @Override
    public void onNewIntent(Intent p_intent) {
        super.onNewIntent(p_intent);

        if (sketch != null)
            sketch.onNewIntent(p_intent);
    }
// endregion

    // Android activity lifecycle events
    // (PASSED STRAIGHT TO `ClientScene`s! with NULL CHECKS!11!)
    // (I would've called super class methods LATER so I could do my own work first...):

    // region
    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // Interference!

        if (ClientScene.currentScene != null)
            ClientScene.currentScene.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (ClientScene.currentScene != null)
            ClientScene.currentScene.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ClientScene.currentScene != null)
            ClientScene.currentScene.onResume();
    }
    // endregion

}
