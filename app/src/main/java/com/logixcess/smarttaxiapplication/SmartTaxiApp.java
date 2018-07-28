package com.logixcess.smarttaxiapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.logixcess.smarttaxiapplication.Utils.Constants;

public class SmartTaxiApp extends Application
{
    private static SmartTaxiApp mInstance;
    private static SharedPreferences sharedPreferences;
    private static Firebase firebase_instance;

    @Override
    public void onCreate() {
        super.onCreate();
        //  Fabric.with(this, new Answers(), new Crashlytics());
        Firebase.setAndroidContext(getApplicationContext());
        Firebase.getDefaultConfig().setPersistenceEnabled(false);
        firebase_instance = new Firebase(Constants.Database_Path);
        mInstance = this;
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
