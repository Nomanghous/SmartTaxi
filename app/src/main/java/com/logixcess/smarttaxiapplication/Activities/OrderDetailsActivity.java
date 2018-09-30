package com.logixcess.smarttaxiapplication.Activities;

import android.app.Activity;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
// import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.Fragments.MapFragment;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.Models.UserFareRecord;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FareCalculation;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.logixcess.smarttaxiapplication.Fragments.MapFragment.CREATE_NEW_GROUP;
import static com.logixcess.smarttaxiapplication.Fragments.MapFragment.new_order;

public class OrderDetailsActivity extends AppCompatActivity {
    TextView tv_pickup, tv_destination, tv_shared, tv_distance, tv_cost, tv_time, tv_vehicle;
    public static List<LatLng> SELECTED_ROUTE = null;
    ValueEventListener valueEventListener;
    Firebase firebase_instance;
    private DatabaseReference db_ref_group;
    String my_region_name;
    private SharedRide currentSharedRide;
    private HashMap<String, Boolean> mPassengerList;
    private HashMap<String, Boolean> mOrderList;
    private DatabaseReference db_ref_order_to_driver;
    DatabaseReference db_ref;
    private HashMap<String, UserFareRecord> mPassengerFares;
    private HashMap<String, List<RoutePoints>> mJourneyPoints;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        db_ref = FirebaseDatabase.getInstance().getReference();
        db_ref_group = db_ref.child(Helper.REF_GROUPS);
        tv_pickup = findViewById(R.id.tv_pickup);
        tv_destination = findViewById(R.id.tv_destination);
        tv_distance = findViewById(R.id.tv_distance);
        tv_cost = findViewById(R.id.tv_cost);
        tv_vehicle = findViewById(R.id.tv_vehicle_type);
        tv_shared = findViewById(R.id.tv_shared);
        tv_time = findViewById(R.id.tv_time);

        if(new_order != null){
            new_order.setUser_id(MainActivity.mFirebaseUser.getUid());
            new_order.setOnRide(false);
            tv_pickup.setText(new_order.getPickup());
            tv_destination.setText(new_order.getDropoff());
            tv_distance.setText(new_order.getTotal_kms());
            tv_cost.setText(new_order.getEstimated_cost());
            tv_vehicle.setText(new_order.getVehicle_id());
            tv_shared.setText(new_order.getShared() ? "Yes" : "No");
            tv_time.setText(new_order.getPickup_time());

        }else{
            Toast.makeText(this, "No Order Details Found", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void goConfirmBooking(View view)
    {
        db_ref_order_to_driver = db_ref.child(Helper.REF_ORDER_TO_DRIVER);
        saveOrderOnline();
    }

    private void saveOrderOnline() {
        db_ref.child(Helper.REF_ORDERS)
                .push().setValue(new_order, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                new_order.setOrder_id(databaseReference.getKey());
                if(new_order == null)
                    return;
                if(new_order.getShared()) {
                    if(!CREATE_NEW_GROUP && !Constants.group_id.isEmpty()) {
                        goFetchGroupByID(Constants.group_id);
                    }else{
                        goCreateGroupForSharedRide();
                    }
                }else {
                    new_order.setStatus(Order.OrderStatusWaiting);
                    db_ref.child(Helper.REF_ORDERS).child(new_order.getOrder_id()).setValue(new_order).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                db_ref_order_to_driver.child(new_order.getDriver_id()).child(Helper.REF_SINGLE_ORDER).setValue(new_order.getOrder_id());
                            }
                            CloseActivity();
                        }
                    });

                }
            }
        });
    }
// group is already created and if the user wants to join then using this function the group information will be fetched.
    private void goFetchGroupByID(String groupId) {
        db_ref_group.child(groupId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentSharedRide = dataSnapshot.getValue(SharedRide.class);
                    if(currentSharedRide != null){
                        mPassengerList = currentSharedRide.getPassengers();
                        mOrderList = currentSharedRide.getOrderIDs();
                        mPassengerList.put(MainActivity.mFirebaseUser.getUid(), false);
                        mOrderList.put(new_order.getOrder_id(), false);
                        currentSharedRide.setPassengers(mPassengerList);
                        currentSharedRide.setOrderIDs(mOrderList);
                        mPassengerFares = currentSharedRide.getPassengerFares();
                        UserFareRecord fareRecord = new UserFareRecord();
                        fareRecord.setUserId(new_order.getUser_id());
                        fareRecord.setBaseFare(new FareCalculation().getBaseFare2(new_order.getVehicle_id()));
                        HashMap<String,Double> userFare = new HashMap<>();
                        LatLng latLng = new LatLng(new_order.getPickupLat(),new_order.getPickupLong());
                        String latlngKey = String.valueOf(latLng.latitude) + String.valueOf(latLng.longitude);
                        userFare.put(Helper.getRefinedLatLngKeyForHashMap(latlngKey),0.0);
                        List<RoutePoints> latLngs = new ArrayList<>();
                        latLngs.add(new RoutePoints(new_order.getPickupLat(), new_order.getPickupLong()));
                        fareRecord.setLatLngs(latLngs);
                        fareRecord.setUserFare(userFare);
                        mJourneyPoints = new HashMap<>();
                        mJourneyPoints.put(new_order.getUser_id(),latLngs);
                        currentSharedRide.setAllJourneyPoints(mJourneyPoints);
                        mPassengerFares.put(new_order.getUser_id(),fareRecord);
                        currentSharedRide.setPassengerFares(mPassengerFares);
                        new_order.setGroup_id(groupId);
                        updateThatSpecificOrderToAccepted(currentSharedRide.getGroup_id());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateThatSpecificOrderToAccepted(String group_id) {
        
        db_ref_group.child(group_id).setValue(currentSharedRide).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    updateOrderForSharedRide();

                }
            }
        });


    }

    private void updateOrderForSharedRide() {
        new_order.setStatus(Order.OrderStatusWaiting);
        new_order.setGroup_id(Constants.group_id);
        db_ref.child(Helper.REF_ORDERS).child(new_order.getOrder_id()).setValue(new_order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    CloseActivity();
                }
            }
        });
    }

    private void goCreateGroupForSharedRide() {
        String userId = MainActivity.mFirebaseUser.getUid();
        String groupId = Helper.getConcatenatedID(new_order.getOrder_id(),new_order.getDriver_id());
        SharedRide sharedRide = new SharedRide();
        sharedRide.setDriver_id(new_order.getDriver_id());
        sharedRide.setGroup_id(groupId);
        sharedRide.setRadius_constraint(Constants.group_radius);
        sharedRide.setStartingLat(new_order.getPickupLat());
        sharedRide.setStartingLng(new_order.getPickupLong());
        getRegionName(OrderDetailsActivity.this,LocationManagerService.mLastLocation.getLatitude(),LocationManagerService.mLastLocation.getLongitude(),groupId);
        sharedRide.setTime(System.currentTimeMillis());
        sharedRide.setUser_id(userId);
        sharedRide.setOrder_id(new_order.getOrder_id());
        HashMap<String, Boolean> ordersIds = new HashMap<>();
        ordersIds.put(new_order.getOrder_id(), false);
        sharedRide.setOrderIDs(ordersIds);
        if(MapFragment.IS_RIDE_SCHEDULED){
            MapFragment.mPassengerList.put(new_order.getUser_id(),false);
            sharedRide.setPassengers(MapFragment.mPassengerList);
        }else{
            HashMap<String, Boolean> passengersIds = new HashMap<>();
            passengersIds.put(userId,false);
            //passengersIds.put(userId,true);
            sharedRide.setPassengers(passengersIds);
        }
        mPassengerFares = new HashMap<>();
        UserFareRecord fareRecord = new UserFareRecord();
        fareRecord.setUserId(new_order.getUser_id());
        fareRecord.setBaseFare(new   FareCalculation().getBaseFare2(new_order.getVehicle_id()));
        HashMap<String ,Double> userFare = new HashMap<>();
        LatLng latLng = new LatLng(new_order.getPickupLat(),new_order.getPickupLong());
        String latlngKey = String.valueOf(latLng.latitude) + String.valueOf(latLng.longitude);
        userFare.put(Helper.getRefinedLatLngKeyForHashMap(latlngKey),0.0);
        new_order.setGroup_id(groupId);
        List<RoutePoints> latLngs = new ArrayList<>();
        latLngs.add(new RoutePoints(new_order.getPickupLat(),new_order.getPickupLong()));
        fareRecord.setLatLngs(latLngs);
        fareRecord.setUserFare(userFare);
        mJourneyPoints = new HashMap<>();
        mJourneyPoints.put(new_order.getUser_id(),latLngs);
        sharedRide.setAllJourneyPoints(mJourneyPoints);
        mPassengerFares.put(new_order.getUser_id(),fareRecord);
        sharedRide.setPassengerFares(mPassengerFares);
        new_order.setStatus(Order.OrderStatusWaiting);
        db_ref_group.child(groupId).setValue(sharedRide);
        db_ref.child(Helper.REF_ORDERS).child(new_order.getOrder_id()).setValue(new_order);
        db_ref_order_to_driver.child(new_order.getDriver_id()).child(Helper.REF_GROUP_ORDER).setValue(groupId);
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
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
