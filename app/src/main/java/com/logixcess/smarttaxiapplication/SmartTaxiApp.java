package com.logixcess.smarttaxiapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.StorageReference;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Constants;

import java.io.InputStream;

import io.fabric.sdk.android.Fabric;

public class SmartTaxiApp extends Application
{
    private static SmartTaxiApp mInstance;
    private static SharedPreferences sharedPreferences;
    private static Firebase firebase_instance;
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
      //  Fabric.with(this, new Answers(), new Crashlytics());
        Firebase.setAndroidContext(getApplicationContext());
        Firebase.getDefaultConfig().setPersistenceEnabled(false);
        //firebase_instance = new Firebase("https://travel-application-c72cb.firebaseio.com/");
        //mInstance = this;
        firebase_instance = new Firebase(Constants.Database_Path);
        mInstance = this;
        startLocationService();
    }

    private void startLocationService() {
        startService(new Intent(this, LocationManagerService.class));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //sharedPreferences = base.getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE);
        MultiDex.install(this);
    }

    public static synchronized SmartTaxiApp getInstance() {
        return mInstance;
    }

    public Firebase getFirebaseInstance(){
        return firebase_instance;
    }


    public SharedPreferences getSharedPreferences(){
        return this.sharedPreferences;
    }



}
