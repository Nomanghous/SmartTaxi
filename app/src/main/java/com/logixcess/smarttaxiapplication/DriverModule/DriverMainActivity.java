package com.logixcess.smarttaxiapplication.DriverModule;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.Timer;
import java.util.TimerTask;

public class DriverMainActivity extends AppCompatActivity {
    private FirebaseDatabase firebase_db;
    private DatabaseReference db_ref_order;
    private DatabaseReference db_ref_user;
    private DatabaseReference db_ref_group;
    private FirebaseUser USER_ME;
    private Location MY_LOCATION = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_user = firebase_db.getReference().child(Helper.REF_DRIVERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        USER_ME = FirebaseAuth.getInstance().getCurrentUser();

        everyTenSecondsTask();
    }

    private void everyTenSecondsTask() {
        new Timer().schedule(new TenSecondsTask(),5000,10000);
    }

    private class TenSecondsTask extends TimerTask {
        @Override
        public void run() {

            updateUserLocation();
        }
    }
    private void updateUserLocation(){
        MY_LOCATION = LocationManagerService.mLastLocation;
        if(MY_LOCATION != null && USER_ME != null){
            String latitude = "latitude";
            String longitude = "longitude";
            db_ref_user.child(USER_ME.getUid()).child(latitude).setValue(MY_LOCATION.getLatitude());
            db_ref_user.child(USER_ME.getUid()).child(longitude).setValue(MY_LOCATION.getLongitude());
        }
    }
    public void openOrderHistory(View view) {

    }

    public void openRunningOrder(View view) {

    }
}
