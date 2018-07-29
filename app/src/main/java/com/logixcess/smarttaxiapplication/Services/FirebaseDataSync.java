package com.logixcess.smarttaxiapplication.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.logixcess.smarttaxiapplication.Utils.Helper;

public class FirebaseDataSync extends Service {

    DatabaseReference db_ref, db_user, db_order, db_group, db_passenger, db_driver;

    @Override
    public void onCreate() {
        super.onCreate();
        db_ref = FirebaseDatabase.getInstance().getReference();
        db_driver = db_ref.child(Helper.REF_DRIVERS);
        db_user = db_ref.child(Helper.REF_USERS);
        db_group = db_ref.child(Helper.REF_GROUPS);
        db_order = db_ref.child(Helper.REF_ORDERS);
        db_passenger = db_ref.child(Helper.REF_PASSENGERS);

//        setupOrderListener();

    }

//    private void setupOrderListener() {
//        db_user.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
