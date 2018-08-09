package com.logixcess.smarttaxiapplication.DriverModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.Activities.MyNotificationManager;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DriverMainActivity extends AppCompatActivity {
    private FirebaseDatabase firebase_db;
    private DatabaseReference db_ref_order;
    private DatabaseReference db_ref_drivers,db_ref_users;
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
    private boolean IS_FIRST_TIME = false;
    private SharedRide CURRENT_SHARED_RIDE;
    private String CURRENT_USER_ID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        setupBroadcastReceivers();
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_drivers = firebase_db.getReference().child(Helper.REF_DRIVERS);
        db_ref_users = firebase_db.getReference().child(Helper.REF_USERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        USER_ME = FirebaseAuth.getInstance().getCurrentUser();
        checkAssignedSingleOrder();
        everyTenSecondsTask();
    }

    private void checkAssignedSingleOrder() {
        db_ref_order_to_driver.child(USER_ME.getUid())
                .child(Helper.REF_SINGLE_ORDER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_ORDER_ID = (String) dataSnapshot.getValue();
                    goFetchOrderByID(CURRENT_ORDER_ID,true);
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
                    goFetchGroupByID(CURRENT_GROUP_ID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    int count_for_region = 0;
    private void everyTenSecondsTask() {
        new Timer().schedule(new TenSecondsTask(),5000,10000);
    }

    private class TenSecondsTask extends TimerTask {
        @Override
        public void run() {
            updateUserLocation();
            count_for_region ++;
            if(count_for_region == 60)
            {
                count_for_region = 0;
                getRegionName(DriverMainActivity.this,MY_LOCATION.getLatitude(),MY_LOCATION.getLongitude());
            }
        }
    }

    private void updateUserLocation(){
        MY_LOCATION = LocationManagerService.mLastLocation;
        if(MY_LOCATION != null && USER_ME != null){
            String latitude = "latitude";
            String longitude = "longitude";
            db_ref_drivers.child(USER_ME.getUid()).child(latitude).setValue(MY_LOCATION.getLatitude());
            db_ref_drivers.child(USER_ME.getUid()).child(longitude).setValue(MY_LOCATION.getLongitude());
        }
    }
    public void getRegionName(Context context, double lati, double longi) {
        String regioName = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 1);
            if (addresses.size() > 0) {
                regioName = addresses.get(0).getLocality();
                if(!TextUtils.isEmpty(regioName))
                {
                    String region_name = "region_name";
                    db_ref_drivers.child(USER_ME.getUid()).child(region_name).setValue(regioName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openOrderHistory(View view) {

    }

    public void openRunningOrder(View view) {
        if(CURRENT_ORDER_ID != null && !CURRENT_ORDER_ID.isEmpty()) {
            openOrderActivity();
        }else{
            Toast.makeText(this, "No Order in Progress", Toast.LENGTH_SHORT).show();
        }
    }

    private void openOrderActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_CURRENT_ORDER,CURRENT_ORDER);
        startActivity(intent);
    }

    private void acceptOrder(String orderId){
        db_ref_order.child(orderId).child("status").setValue(Order.OrderStatusInProgress);
        db_ref_order.child(orderId).child("driver_id").setValue(USER_ME.getUid());
        db_ref_order.child(orderId).child("order_id").setValue(orderId);
        CURRENT_ORDER_ID = orderId;
        goFetchOrderByID(orderId,false);
        IS_FIRST_TIME = true;
    }

    private void goFetchOrderByID(String orderId, boolean isAlreadyAccepted) {
        db_ref_order.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_ORDER = dataSnapshot.getValue(Order.class);
                    if(CURRENT_ORDER != null){
                        CURRENT_USER_ID = CURRENT_ORDER.getUser_id();
                        CURRENT_ORDER_ID = CURRENT_ORDER.getOrder_id();
                        goFetchCustomerById(isAlreadyAccepted);
                        String groupId = Helper.getConcatenatedID(CURRENT_ORDER_ID, USER_ME.getUid());
                        if(CURRENT_ORDER.getShared())
                            goFetchGroupByID(groupId);
                        if(!isAlreadyAccepted) {
                            Toast.makeText(DriverMainActivity.this, "Order Accepted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goFetchCustomerById(boolean isAlreadyAccepted) {
        db_ref_users.child(CURRENT_USER_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_USER = dataSnapshot.getValue(User.class);
                    if(CURRENT_USER != null){
                        if(!isAlreadyAccepted) {
                            sendPushNotification();
                        }
                        openOrderActivity();
                        db_ref_order_to_driver.child(USER_ME.getUid()).child(Helper.REF_SINGLE_ORDER).setValue(CURRENT_ORDER_ID);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendPushNotification() {
        NotificationPayload payload = new NotificationPayload();
        payload.setOrder_id(CURRENT_ORDER_ID);
        payload.setPercentage_left("");
        payload.setTitle("Order Accepted");
        payload.setDescription("Tap to View Details");
        payload.setType(Helper.NOTI_TYPE_ORDER_ACCEPTED);
        if(CURRENT_ORDER.getShared())
            payload.setGroup_id(CURRENT_GROUP_ID);
        else
            payload.setGroup_id("--NA--");
        payload.setUser_id(CURRENT_USER_ID);
        payload.setDriver_id(USER_ME.getUid());
        new PushNotifictionHelper(this).execute(CURRENT_USER.getUser_token(),payload);
    }

    private void goFetchGroupByID(String groupId) {
        db_ref_order_to_driver.child(USER_ME.getUid()).child(Helper.REF_GROUP_ORDER).setValue(groupId);
        db_ref_group.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    CURRENT_SHARED_RIDE = dataSnapshot.getValue(SharedRide.class);
                    if(CURRENT_SHARED_RIDE != null){
                        if(CURRENT_ORDER_ID != null)
                            openOrderActivity(CURRENT_SHARED_RIDE);
                        else{
                            if(CURRENT_ORDER == null)
                                goFetchOrderByID(CURRENT_SHARED_RIDE.getOrder_id(),true);
                            else{
                                CURRENT_ORDER_ID = CURRENT_ORDER.getOrder_id();
                                openOrderActivity(CURRENT_SHARED_RIDE);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openOrderActivity(SharedRide current_shared_ride) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_CURRENT_SHARED_RIDE,current_shared_ride);
        intent.putExtra(MapsActivity.KEY_CURRENT_ORDER, CURRENT_ORDER);
        startActivity(intent);
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
