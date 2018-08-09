package com.logixcess.smarttaxiapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {
    TextView tv_pickup, tv_destination, tv_shared, tv_distance, tv_cost, tv_time, tv_vehicle;
    public static List<LatLng> SELECTED_ROUTE = null;
    ValueEventListener valueEventListener;
    Firebase firebase_instance;
    private DatabaseReference db_ref_group;
    String my_region_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        db_ref_group = FirebaseDatabase.getInstance().getReference().child(Helper.REF_GROUPS);
        tv_pickup = findViewById(R.id.tv_pickup);
        tv_destination = findViewById(R.id.tv_destination);
        tv_distance = findViewById(R.id.tv_distance);
        tv_cost = findViewById(R.id.tv_cost);
        tv_vehicle = findViewById(R.id.tv_vehicle_type);
        tv_shared = findViewById(R.id.tv_shared);
        tv_time = findViewById(R.id.tv_time);

        if(Helper.CURRENT_ORDER != null){
            tv_pickup.setText(Helper.CURRENT_ORDER.getPickup());
            tv_destination.setText(Helper.CURRENT_ORDER.getDropoff());
            tv_distance.setText(Helper.CURRENT_ORDER.getTotal_kms());
            tv_cost.setText(Helper.CURRENT_ORDER.getEstimated_cost());
            tv_vehicle.setText(Helper.CURRENT_ORDER.getVehicle_id());
            tv_shared.setText(Helper.CURRENT_ORDER.getShared() ? "Yes" : "No");
            tv_time.setText(Helper.CURRENT_ORDER.getPickup_time());

        }else{
            Toast.makeText(this, "No Order Details Found", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void goConfirmBooking(View view)
    {
        saveOrderOnline();

    }

    private void saveOrderOnline() {

        Helper.CURRENT_ORDER.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference().child(Helper.REF_ORDERS)
                .push().setValue(Helper.CURRENT_ORDER, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Helper.CURRENT_ORDER.setOrder_id(databaseReference.getKey());
                sendNotificationToken();

            }
        });
    }


    public void sendNotificationToken()
    {
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference().child(Helper.REF_USERS);
        db_ref_user.child(Helper.CURRENT_ORDER.getDriver_id()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User driver = dataSnapshot.getValue(User.class);
                    String token = driver.getUser_token();
                    NotificationPayload notificationPayload = new NotificationPayload();
                    notificationPayload.setType(Helper.NOTI_TYPE_ORDER_CREATED);
                    notificationPayload.setTitle("Order Created");
                    notificationPayload.setDescription("Do you want to accept it");
                    notificationPayload.setUser_id(Helper.CURRENT_ORDER.getUser_id());
                    notificationPayload.setDriver_id(Helper.CURRENT_ORDER.getDriver_id());
                    notificationPayload.setPercentage_left(Helper.CURRENT_ORDER.getOrder_id());
                    String str = new Gson().toJson(notificationPayload);
                    try {
                        JSONObject json = new JSONObject(str);
                        new PushNotifictionHelper(getApplicationContext()).execute(token,json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(Helper.CURRENT_ORDER.getShared())
                        goCreateGroupForSharedRide();
                    else
                        CloseActivity();
                }
                else
                {
                    Toast.makeText(OrderDetailsActivity.this,"Driver not found!",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void goCreateGroupForSharedRide() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //String groupId = Helper.getConcatenatedID(userId,Helper.CURRENT_ORDER.getOrder_id());
        String groupId = Helper.getConcatenatedID(Helper.CURRENT_ORDER.getOrder_id(),Helper.CURRENT_ORDER.getDriver_id());
        SharedRide sharedRide = new SharedRide();
        sharedRide.setGroup_id(groupId);
        getRegionName(OrderDetailsActivity.this,LocationManagerService.mLastLocation.getLatitude(),LocationManagerService.mLastLocation.getLongitude(),groupId);
        sharedRide.setTime(System.currentTimeMillis());
        sharedRide.setUser_id(userId);
        sharedRide.setOrder_id(Helper.CURRENT_ORDER.getOrder_id());
        HashMap<String, Boolean> ordersIds = new HashMap<>();
        ordersIds.put(Helper.CURRENT_ORDER.getOrder_id(), false);
        sharedRide.setOrderIDs(ordersIds);
        HashMap<String, Boolean> passengersIds = new HashMap<>();
        passengersIds.put(userId,true);
        sharedRide.setPassengers(passengersIds);
        db_ref_group.child(groupId).setValue(sharedRide);
        CloseActivity();
    }
    public void getRegionName(Context context, double lati, double longi,String group_id) {
        String regioName = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 1);
            if (addresses.size() > 0) {
                regioName = addresses.get(0).getLocality();
                if(!TextUtils.isEmpty(regioName))
                {
                    String region_name = "region_name";
                    db_ref_group.child(group_id).child(region_name).setValue(regioName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CloseActivity() {
        Toast.makeText(OrderDetailsActivity.this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(OrderDetailsActivity.this, MapsActivity.class));
        finish();
    }

}
