package com.logixcess.smarttaxiapplication.DriverModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.DriverModule.MapsActivity;
import com.logixcess.smarttaxiapplication.Activities.MyNotificationManager;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class DriverMainActivity extends AppCompatActivity {
    private FirebaseDatabase firebase_db;
    private DatabaseReference db_ref_order;
    private DatabaseReference db_ref_user;
    private DatabaseReference db_ref_group;
    private DatabaseReference db_ref_single_order;
    private DatabaseReference db_ref_shared_order;
    private DatabaseReference db_ref_order_to_driver;
    private FirebaseUser USER_ME;
    private Location MY_LOCATION = null;
    private Order CURRENT_ORDER = null;
    private User CURRENT_USER = null;
    private String CURRENT_ORDER_ID;
    private String CURRENT_GROUP_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_user = firebase_db.getReference().child(Helper.REF_DRIVERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        USER_ME = FirebaseAuth.getInstance().getCurrentUser();
        checkAssignedSingleOrder();

        setupBroadcastReceivers();
        everyTenSecondsTask();
    }

    private void checkAssignedSingleOrder() {
        db_ref_order_to_driver.child(USER_ME.getUid())
                .child(Helper.REF_SINGLE_ORDER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_ORDER_ID = (String) dataSnapshot.getValue();
                }else{
                    checkAssignedGroupOrder();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkAssignedGroupOrder(){
        db_ref_order_to_driver.child(USER_ME.getUid())
                .child(Helper.REF_GROUP_ORDER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_GROUP_ID = (String) dataSnapshot.getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        if(CURRENT_ORDER_ID != null && !CURRENT_ORDER_ID.isEmpty()) {
            openOrderActivity(CURRENT_ORDER_ID);
        }else{
            Toast.makeText(this, "No Order in Progress", Toast.LENGTH_SHORT).show();
        }
    }

    private void openOrderActivity(String current_order_id) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(com.logixcess.smarttaxiapplication.Activities.MapsActivity.KEY_CURRENT_ORDER,current_order_id);
        startActivity(intent);
    }

    private void acceptOrder(String orderId){
        db_ref_order.child(orderId).child("status").setValue(Order.OrderStatusInProgress);
        db_ref_order.child(orderId).child("driver_id").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        db_ref_order.child(orderId).child("order_id").setValue(orderId);
        db_ref_order_to_driver.child(USER_ME.getUid()).child(Helper.REF_SINGLE_ORDER).setValue(orderId);
        // TODO: add orderr to driver for shared ride as well.

        openOrderActivity(orderId);
        Toast.makeText(this, "Order Accepted", Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver mAcceptOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getExtras().getString("data");
            try {
                String order_id = new JSONObject(data).getString("order_id");
                acceptOrder(order_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver mRejectOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    private BroadcastReceiver mViewOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    private void setupBroadcastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mAcceptOrderReceiver,
                new IntentFilter(MyNotificationManager.INTENT_FILTER_ACCEPT_ORDER));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRejectOrderReceiver,
                new IntentFilter(MyNotificationManager.INTENT_FILTER_REJECT_ORDER));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRejectOrderReceiver,
                new IntentFilter(MyNotificationManager.INTENT_FILTER_VIEW_ORDER));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAcceptOrderReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRejectOrderReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mViewOrderReceiver);
        super.onDestroy();
    }

}
