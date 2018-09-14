package com.logixcess.smarttaxiapplication.DriverModule;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.maps.model.LatLng;
import com.logixcess.smarttaxiapplication.Activities.MyNotificationManager;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.Requests;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FareCalculation;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.NotificationUtils;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DriverMainActivity extends AppCompatActivity {
    protected FirebaseDatabase firebase_db;
    protected DatabaseReference db_ref_order;
    protected DatabaseReference db_ref_drivers,db_ref_users;
    protected DatabaseReference db_ref_group;
    protected DatabaseReference db_ref_order_to_driver;
    protected FirebaseUser userMe;
    protected Location myLocation = null;
    protected Order currentOrder = null;
    protected User currentUser = null;
    protected String CURRENT_ORDER_ID;
    protected String CURRENT_GROUP_ID = null;
    protected SharedRide currentSharedRide;
    protected String currentUserId = "";
    private TextToSpeech tts;
    protected FareCalculation mFareCalc = new FareCalculation();
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    Boolean isPromptDismissed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promptSpeechInput();
        if(!Helper.IS_FROM_CHILD) {
            setContentView(R.layout.activity_driver_main);
            setupBroadcastReceivers();
            firebase_db = FirebaseDatabase.getInstance();
            db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
            db_ref_drivers = firebase_db.getReference().child(Helper.REF_DRIVERS);
            db_ref_users = firebase_db.getReference().child(Helper.REF_USERS);
            db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
            db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
            userMe = FirebaseAuth.getInstance().getCurrentUser();
            checkAssignedSingleOrder();
            everyTenSecondsTask();
            listenForDriverResponse(this,userMe.getUid());
        }
    }
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
            });
            
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(result.get(0).equalsIgnoreCase("accept"))
                    {
                        acceptingVoice();
                        isPromptDismissed = false;
                    }
                    else if(result.get(0).equalsIgnoreCase("reject"))
                    {
                        rejectingVoice();
                        isPromptDismissed = false;
                    }
                    //mVoiceInputTv.setText(result.get(0));
                }
                break;
            }

        }
    }
 
    public static void listenForDriverResponse(Context context, String driverId){
//        FirebaseDatabase firebase_db = FirebaseDatabase.getInstance();
//        DatabaseReference db_ref_requests = firebase_db.getReference().child(Helper.REF_REQUESTS);
//        db_ref_requests.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Requests request = snapshot.getValue(Requests.class);
//                        if (request != null) {
//                            if (request.getDriverId().equals(driverId) && request.getStatus() == Requests.STATUS_PENDING) {
//                                NotificationPayload notificationPayload = new NotificationPayload();
//                                notificationPayload.setType(Helper.NOTI_TYPE_ACCEPTANCE_FOR_SHARED_RIDE);
//                                notificationPayload.setTitle("\"Request Accepted\"");
//                                notificationPayload.setDescription("\"Your Group Ride Request is Accepted\"");
//                                notificationPayload.setUser_id("\"" + request.getUserId() + "\"");
//                                notificationPayload.setDriver_id("\"" + request.getDriverId() + "\"");
//                                notificationPayload.setOrder_id("\"\"");
//                                notificationPayload.setPercentage_left("\"" + -1 + "\"");
//                                String str = new Gson().toJson(notificationPayload);
//
//                                try {
//                                    JSONObject json = new JSONObject(str);
//                                    NotificationUtils.preparePendingIntentForFriendRequest(context, json.toString(), notificationPayload);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }
    
    private void checkAssignedSingleOrder() {
        db_ref_order_to_driver.child(userMe.getUid())
                .child(Helper.REF_SINGLE_ORDER).addValueEventListener(new ValueEventListener() {
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
        db_ref_order_to_driver.child(userMe.getUid())
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
                getRegionName(DriverMainActivity.this, myLocation.getLatitude(), myLocation.getLongitude());
            }
            if(!TextUtils.isEmpty(Constants.notificationPayload) && (!isPromptDismissed))
            {
                isPromptDismissed = true;
                promptSpeechInput();
            }
        }
    }

    private void updateUserLocation(){
        myLocation = LocationManagerService.mLastLocation;
        if(myLocation != null && userMe != null){
            String latitude = "latitude";
            String longitude = "longitude";
            double lat = Helper.roundOffDouble(myLocation.getLatitude());
            double lng = Helper.roundOffDouble(myLocation.getLongitude());
            db_ref_drivers.child(userMe.getUid()).child(latitude).setValue(lat);
            db_ref_drivers.child(userMe.getUid()).child(longitude).setValue(lng);
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
                    db_ref_drivers.child(userMe.getUid()).child(region_name).setValue(regioName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openOrderHistory(View view) {

    }

    public void openRunningOrder(View view) {
        checkAssignedSingleOrder();
        
    }

    private void openOrderActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_CURRENT_ORDER, currentOrder);
        startActivity(intent);
    }

    private void acceptOrder(String orderId){
        CURRENT_ORDER_ID = orderId;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(CURRENT_ORDER_ID != null)
            goFetchOrderByID(CURRENT_ORDER_ID,true);
    }

    private void goFetchOrderByID(String orderId, boolean isAlreadyAccepted) {
        db_ref_order.child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentOrder = dataSnapshot.getValue(Order.class);
                    if(currentOrder != null){
                        if(currentOrder.getStatus() == Order.OrderStatusCompleted ){
                            db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(DriverMainActivity.this, "Your Order has been Completed", Toast.LENGTH_SHORT).show();
                                    currentOrder = null;
                                }
                            });
                            return;
                        }else if(currentOrder.getStatus() == Order.OrderStatusCancelled){
                            db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(DriverMainActivity.this, "Your Order has been Cancelled", Toast.LENGTH_SHORT).show();
                                    currentOrder = null;
                                }
                            });
                            return;
                        }

                        currentUserId = currentOrder.getUser_id();
                        CURRENT_ORDER_ID = currentOrder.getOrder_id();

                        if(TextUtils.isEmpty(currentUserId)){
                            goFetchCustomerById(isAlreadyAccepted);
                        }
                        if(currentOrder.getShared()) {
                            String groupId = Helper.getConcatenatedID(CURRENT_ORDER_ID, userMe.getUid());
                            if (CURRENT_GROUP_ID == null && currentOrder.getShared())
                                goFetchGroupByID(groupId);
                            if (!isAlreadyAccepted) {
                                Toast.makeText(DriverMainActivity.this, "Order Accepted", Toast.LENGTH_SHORT).show();
                            }
                        }else if(CURRENT_ORDER_ID != null && !CURRENT_ORDER_ID.isEmpty()) {
                            openOrderActivity();
                        }else{
                            Toast.makeText(DriverMainActivity.this, "No Order in Progress", Toast.LENGTH_SHORT).show();
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
        db_ref_users.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUser = dataSnapshot.getValue(User.class);
                    if(currentUser != null){
                        if(!isAlreadyAccepted) {
                            sendPushNotification();
                        }
                        openOrderActivity();
                        if(!currentOrder.getShared())
                            db_ref_order_to_driver.child(userMe.getUid()).child(Helper.REF_SINGLE_ORDER).setValue(CURRENT_ORDER_ID);
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
        payload.setPercentage_left(escapeValue(""));
        payload.setTitle(escapeValue("Order Accepted"));
        payload.setDescription(escapeValue("Tap to View Details"));
        payload.setType(Helper.NOTI_TYPE_ORDER_ACCEPTED);
        if(currentOrder.getShared())
            payload.setGroup_id(escapeValue(CURRENT_GROUP_ID));
        else
            payload.setGroup_id(escapeValue("--NA--"));
        payload.setUser_id(escapeValue(currentUserId));
        payload.setDriver_id(escapeValue(userMe.getUid()));
        String str = new Gson().toJson(payload);
        try {
            JSONObject json = new JSONObject(str);
            new PushNotifictionHelper(getApplicationContext()).execute(currentUser.getUser_token(),json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private String escapeValue(String value) {
        return "\""+value+"\"";
    }
    private void goFetchGroupByID(String groupId) {
        db_ref_order_to_driver.child(userMe.getUid()).child(Helper.REF_GROUP_ORDER).setValue(groupId);
        db_ref_group.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentSharedRide = dataSnapshot.getValue(SharedRide.class);
                    if(currentSharedRide != null){
                        if(CURRENT_ORDER_ID != null && currentOrder != null)
                            openOrderActivity(currentSharedRide);
                        else{
                            if(currentOrder == null)
                                goFetchOrderByID(currentSharedRide.getOrder_id(),true);
                            else{
                                CURRENT_ORDER_ID = currentOrder.getOrder_id();
                                openOrderActivity(currentSharedRide);
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
        intent.putExtra(MapsActivity.KEY_CURRENT_ORDER, currentOrder);
        startActivity(intent);
    }

    private BroadcastReceiver mAcceptOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getExtras().getString("data");
            NotificationPayload notificationPayload = new Gson().fromJson(data,NotificationPayload.class);
            String order_id = notificationPayload.getOrder_id();
            acceptOrder(order_id);
            Constants.notificationPayload = "";

        }
    };

    private BroadcastReceiver mRejectOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Constants.notificationPayload = "";
        }
    };

    private BroadcastReceiver mViewOrderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    public void acceptingVoice()
    {
        if(TextUtils.isEmpty(Constants.notificationPayload))
            return;
        Intent intent = new Intent(DriverMainActivity.this,MyNotificationManager.class);//"mAcceptOrderReceiver");
        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction("HELLO1");
        intent.setAction(MyNotificationManager.INTENT_FILTER_ACCEPT_ORDER);
        //intent.addFlags(intentFilter);
        // Put the random number to intent to broadcast it
        //intent.putExtra("RandomNumber",randomNumber);
        // Send the broadcast
        LocalBroadcastManager.getInstance(DriverMainActivity.this).sendBroadcast(intent);

    }
    public void rejectingVoice()
    {
        if(TextUtils.isEmpty(Constants.notificationPayload))
            return;
        Intent intent = new Intent(DriverMainActivity.this,MyNotificationManager.class);//"mAcceptOrderReceiver");
        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction("HELLO1");
        intent.setAction(MyNotificationManager.INTENT_FILTER_REJECT_ORDER);
        //intent.addFlags(intentFilter);
        // Put the random number to intent to broadcast it
        //intent.putExtra("RandomNumber",randomNumber);
        // Send the broadcast
        LocalBroadcastManager.getInstance(DriverMainActivity.this).sendBroadcast(intent);

    }

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
