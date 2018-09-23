package com.logixcess.smarttaxiapplication.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.NotificationUtils;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import static com.logixcess.smarttaxiapplication.CustomerModule.CustomerMapsActivity.mDriverMarker;

public class FirebaseDataSync extends Service {

    DatabaseReference db_ref, db_user, db_order, db_group, db_passenger, db_driver;
    Order currentOrder;
    FirebaseUser mUser;
    Location pickupLocation, driverLocation;
    private Driver mDriver;
    double totalDistance;
    double currentDistance;
    private CountDownTimer mCountDowntimer;
    
    @Override
    public void onCreate() {
        super.onCreate();
        db_ref = FirebaseDatabase.getInstance().getReference();
        db_driver = db_ref.child(Helper.REF_DRIVERS);
        db_user = db_ref.child(Helper.REF_USERS);
        db_group = db_ref.child(Helper.REF_GROUPS);
        db_order = db_ref.child(Helper.REF_ORDERS);
        db_passenger = db_ref.child(Helper.REF_PASSENGERS);
        
        Query query = db_order.orderByChild("user_id").equalTo(mUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            if (order.getStatus() == Order.OrderStatusWaiting ||
                                    order.getStatus() == Order.OrderStatusInProgress) {
                                currentOrder = order;
                                setDriverUpdates();
                                setOrderUpdates();
                                break;
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
    
    private void setOrderUpdates() {
        pickupLocation = new Location("pickup");
        pickupLocation.setLatitude(currentOrder.getPickupLat());
        pickupLocation.setLongitude(currentOrder.getPickupLong());
        db_order.child(currentOrder.getOrder_id()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            
            }
    
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Order order = dataSnapshot.getValue(Order.class);
                if(order != null)
                    currentOrder = order;
            }
    
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        
            }
    
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    private void setDriverUpdates() {
        driverLocation = new Location("Driver");
        db_driver.child(currentOrder.getDriver_id()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            
            }
    
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver != null) {
                    mDriver = driver;
                    driverLocation.setLatitude(mDriver.getLatitude());
                    driverLocation.setLongitude(mDriver.getLongitude());
                    try {
                        calculateDistance();
                        checkDistanceAndNotify();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        
            }
    
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    private void calculateDistance() {
        if(totalDistance == 0)
            totalDistance = pickupLocation.distanceTo(driverLocation);
        currentDistance = totalDistance - pickupLocation.distanceTo(driverLocation);
    }
    
    private void checkDistanceAndNotify() {
        int percentageLeft = (int) ((int) currentDistance / totalDistance * 100);
        if(mDriverMarker != null) {
            LatLng markerPosition = mDriverMarker.getPosition();
            String title = mDriverMarker.getTitle();
        }
        boolean[] NotificationsDone = currentOrder.getNotificaionsDone();
        NotificationPayload payload = new NotificationPayload();
        payload.setDriver_id(mUser.getUid());
        payload.setUser_id(currentOrder.getUser_id());
        String group_id = "--NA--";
        if(currentOrder.getShared())
            group_id = Helper.getConcatenatedID(currentOrder.getOrder_id(), mUser.getUid());
        payload.setGroup_id(group_id);
        payload.setTitle("Order Updates");
        payload.setPercentage_left(""+ currentDistance);
        payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
        payload.setOrder_id(currentOrder.getOrder_id());
        if(percentageLeft < 5 && mCountDowntimer == null) {
            mCountDowntimer = new CountDownTimer(300000, 60000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    payload.setDescription("Driver is Waiting outside");
                    payload.setType(Helper.NOTI_TYPE_ORDER_WAITING_LONG);
                    String str = new Gson().toJson(payload);
                    NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
                }
                @Override
                public void onFinish() {
                
                }
            }.start();
        
        }else if(percentageLeft < 25 && !NotificationsDone[0]){
            NotificationsDone[0] = true;
            NotificationsDone[1] = true;
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription("Driver is reaching soon");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
                mDriverMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }else if(percentageLeft < 50 && !NotificationsDone[1]){
            NotificationsDone[1] = true;
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription("Driver is on his way");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
                mDriverMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }else if(percentageLeft < 75 && !NotificationsDone[2]){
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription("Driver is coming your way");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
                mDriverMarker .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        }else if(percentageLeft < 100 && !NotificationsDone[3]){
            NotificationsDone[3] = true;
            payload.setDescription("Driver is coming");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
                mDriverMarker .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }
        currentOrder.setNotificaionsDone(NotificationsDone);
    }
    
    
    @Override
    public boolean stopService(Intent name) {
        currentDistance = 0;
        totalDistance = 0;
        if(mCountDowntimer != null)
            mCountDowntimer.cancel();
        mCountDowntimer = null;
        currentOrder = null;
        mDriver = null;
        return super.stopService(name);
    }
    
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
