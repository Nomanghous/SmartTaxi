package com.logixcess.smarttaxiapplication.Services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.DeviceInfoUtils;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.logixcess.smarttaxiapplication.CustomerModule.CustomerMapsActivity.btn_waiting_time;
import static com.logixcess.smarttaxiapplication.CustomerModule.CustomerMapsActivity.mDriverMarker;
import static com.logixcess.smarttaxiapplication.CustomerModule.CustomerMapsActivity.total_fare;
import static com.logixcess.smarttaxiapplication.MainActivity.mRunningOrder;
import static com.logixcess.smarttaxiapplication.Utils.Constants.playNotificationSound;
import static com.logixcess.smarttaxiapplication.Utils.Constants.userIsReady;

public class FirebaseDataSync extends Service implements RoutingListener {

    DatabaseReference db_ref, db_user, db_order, db_group, db_passenger, db_driver;
    public static Order currentOrder;
    FirebaseUser mUser;
    Location pickupLocation;
    public static Location driverLocation;
    public static Driver currentDriver;
    double totalDistance;
    double currentDistance;
    private CountDownTimer mCountDowntimer;
    public static User currentUser;
    
    @Override
    public void onCreate() {
        super.onCreate();
        db_ref = FirebaseDatabase.getInstance().getReference();
        db_driver = db_ref.child(Helper.REF_DRIVERS);
        db_user = db_ref.child(Helper.REF_USERS);
        db_group = db_ref.child(Helper.REF_GROUPS);
        db_order = db_ref.child(Helper.REF_ORDERS);
        db_passenger = db_ref.child(Helper.REF_PASSENGERS);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser == null)
            return;
        if(mRunningOrder != null) {
            currentOrder = mRunningOrder;
            setDriverUpdates();
            setOrderUpdates();
        } else {
            Query query = db_order.orderByChild("user_id").equalTo(mUser.getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
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
       
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                pingDistanceAPI();
            }
        }, 0, 10000);
        
        
    }
    
    private void setOrderUpdates() {
        pickupLocation = new Location("pickup");
        pickupLocation.setLatitude(currentOrder.getPickupLat());
        pickupLocation.setLongitude(currentOrder.getPickupLong());
        db_order.child(currentOrder.getOrder_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                if(order != null) {
                    currentOrder = order;
                    if(total_fare != null) {
                        total_fare.setText(String.valueOf(currentOrder.getTotal_fare()));
                    }
                }
    
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    private void setDriverUpdates() {
        driverLocation = new Location("Driver");
        Log.i("DriverId",currentOrder.getDriver_id());
        db_driver.child(currentOrder.getDriver_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver != null) {
                    currentDriver = driver;
                    driverLocation.setLatitude(currentDriver.getLatitude());
                    driverLocation.setLongitude(currentDriver.getLongitude());
                    if(mDriverMarker != null) {
                        mDriverMarker.setPosition(new LatLng(driverLocation.getLatitude()
                                ,driverLocation.getLongitude()));
                    }
                    try {
                        calculateDistance();
                        checkDistanceAndNotify();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
        

    }
    private void getUserDetails() {
        db_driver.child(currentOrder.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null) {
                    currentUser = user;
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    private void calculateDistance() {
//        if(totalDistance == 0) {
//            totalDistance = pickupLocation.distanceTo(driverLocation);
//        }
//        currentDistance = pickupLocation.distanceTo(driverLocation);
      
    }
    
    private void checkDistanceAndNotify() {
        if(currentOrder.getStatus() == Order.OrderStatusInProgress) {
            if(mDriverMarker != null)
                mDriverMarker.setIcon(getDrawableByType(currentOrder.getVehicle_id(),100));
            return;
        }
        double percentageLeft = currentDistance / totalDistance * 100;
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
        Log.i("percentage", percentageLeft + " cd: " + currentDistance + " td: " + totalDistance);
        
        if(percentageLeft < 5 && mCountDowntimer == null) {
            mCountDowntimer = new CountDownTimer(300000, 60000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(currentOrder.getStatus() == Order.OrderStatusInProgress) {
                        mCountDowntimer.cancel();
                        mCountDowntimer = null;
                        return;
                    }
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
                mDriverMarker.setIcon(getDrawableByType(currentOrder.getVehicle_id(),percentageLeft));
        }else if(percentageLeft < 50 && !NotificationsDone[1]){
            NotificationsDone[1] = true;
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription("Driver is on his way");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
                mDriverMarker.setIcon(getDrawableByType(currentOrder.getVehicle_id(),percentageLeft));
        }else if(percentageLeft < 75 && !NotificationsDone[2]){
            NotificationsDone[2] = true;
            NotificationsDone[3] = true;
            payload.setDescription("Driver is coming your way");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
                mDriverMarker.setIcon(getDrawableByType(currentOrder.getVehicle_id(),percentageLeft));
        }else if(percentageLeft < 100 && !NotificationsDone[3]){
            NotificationsDone[3] = true;
            payload.setDescription("Driver is coming");
            payload.setType(Helper.NOTI_TYPE_ORDER_WAITING);
            String str = new Gson().toJson(payload);
            NotificationUtils.showNotificationForUserActions(getApplicationContext(),str);
            if(mDriverMarker != null)
            {
                mDriverMarker.setIcon(getDrawableByType(currentOrder.getVehicle_id(),percentageLeft));
            }
                ////mDriverMarker.setIcon(BitmapDescriptorFactory.fromBitmap());//mDriverMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }
        currentOrder.setNotificaionsDone(NotificationsDone);
    }
    private BitmapDescriptor getDrawableByType(String vehicleType,double percentage) {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_option_nano);
        switch (vehicleType){
            case Helper.VEHICLE_CAR:
                if(percentage <=100 && percentage>=75)
                {
                    drawable = getResources().getDrawable(R.drawable.ic_option_car);
                }
                else if(percentage<75 && percentage>=50)
                {
                    drawable = getResources().getDrawable(R.drawable.car_lb);
                }
                else if(percentage<50 && percentage>=25)
                {
                    drawable = getResources().getDrawable(R.drawable.car_lr);
                }
                else if(percentage<25 && percentage>=0)
                {
                    drawable = getResources().getDrawable(R.drawable.car_dr);
                }
                break;
            case Helper.VEHICLE_MINI:
                if(percentage<=100 && percentage>=75)
                {
                    drawable = getResources().getDrawable(R.drawable.ic_option_mini);
                }
                else if(percentage<75 && percentage>=50)
                {
                    drawable = getResources().getDrawable(R.drawable.mini_lb);
                }
                else if(percentage<50 && percentage>=25)
                {
                    drawable = getResources().getDrawable(R.drawable.mini_lr);
                }
                else if(percentage<25 && percentage>=0)
                {
                    drawable = getResources().getDrawable(R.drawable.mini_dr);
                }
                break;
            case Helper.VEHICLE_NANO:
                if(percentage<=100 && percentage>=75)
                {
                    drawable = getResources().getDrawable(R.drawable.ic_option_nano);
                }
                else if(percentage<75 && percentage>=50)
                {
                    drawable = getResources().getDrawable(R.drawable.nano_lb);
                }
                else if(percentage<50 && percentage>=25)
                {
                    drawable = getResources().getDrawable(R.drawable.nano_lr);
                }
                else if(percentage<25 && percentage>=0)
                {
                    drawable = getResources().getDrawable(R.drawable.nano_dr);
                }
                break;
            case Helper.VEHICLE_THREE_WHEELER:
                if(percentage<=100 && percentage>=75)
                {
                    drawable = getResources().getDrawable(R.drawable.ic_option_three_wheeler);
                }
                else if(percentage<75 && percentage>=50)
                {
                    drawable = getResources().getDrawable(R.drawable.three_wheeler_lb);
                }
                else if(percentage<50 && percentage>=25)
                {
                    drawable = getResources().getDrawable(R.drawable.three_wheeler_lr);
                }
                else if(percentage<25 && percentage>=0)
                {
                    drawable = getResources().getDrawable(R.drawable.three_wheeler_dr);
                }
                break;
            case Helper.VEHICLE_VIP:
                if(percentage<=100 && percentage>=75)
                {
                    drawable = getResources().getDrawable(R.drawable.ic_option_vip);
                }
                else if(percentage<75 && percentage>=50)
                {
                    drawable = getResources().getDrawable(R.drawable.vip_lb);
                }
                else if(percentage<50 && percentage>=25)
                {
                    drawable = getResources().getDrawable(R.drawable.vip_lr);
                }
                else if(percentage<25 && percentage>=0)
                {
                    drawable = getResources().getDrawable(R.drawable.vip_dr);
                }
                break;
        }
        Bitmap driverPin = Helper.convertToBitmap(drawable, 100, 100);
        return BitmapDescriptorFactory.fromBitmap(driverPin);
    }
    @Override
    public boolean stopService(Intent name) {
        currentDistance = 0;
        totalDistance = 0;
        if(mCountDowntimer != null)
            mCountDowntimer.cancel();
        mCountDowntimer = null;
        currentOrder = null;
        currentDriver = null;
        return super.stopService(name);
    }
    
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public static Order getCurrentOrder() {
        return currentOrder;
    }
    
    
    private void pingDistanceAPI(){
        if(currentUser == null){
            if(currentOrder != null)
                getUserDetails();
        }
        
        if(driverLocation == null || pickupLocation == null)
            return;
        LatLng driver = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
        LatLng pickup = new LatLng(pickupLocation.getLatitude(),pickupLocation.getLongitude());
        List<LatLng> points = new ArrayList<>();
        points.add(driver);
        points.add(pickup);
        Routing routing = new Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .alternativeRoutes(true)
            .waypoints(points)
                .key(getResources().getString(R.string.google_maps_api))
            .build();
        routing.execute();
    }
    
    
    @Override
    public void onRoutingFailure(RouteException e) {
        e.printStackTrace();
    }
    
    @Override
    public void onRoutingStart() {
    
    }
    
    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        Route shortest = arrayList.get(i);
        double distance = shortest.getDistanceValue();
        if(totalDistance == 0) {
            totalDistance = distance;
        }
        currentDistance = distance;
    
    
        try {
            checkDistanceAndNotify();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        double time = shortest.getDurationValue() / 60;
        if(time == 20 && !userIsReady){
            DeviceInfoUtils.increaseDeviceSound(this);
            playNotificationSound(this,R.raw.beep);
            NotificationUtils.preparePendingIntentForReadiness(this);
        }else if(time == 14 && !userIsReady){
            DeviceInfoUtils.increaseDeviceSound(this);
            playNotificationSound(this,R.raw.beep);
            playNotificationSound(this,R.raw.beep);
            NotificationUtils.preparePendingIntentForReadiness(this);
        }else if(time == 10 && !userIsReady){
            DeviceInfoUtils.increaseDeviceSound(this);
            playNotificationSound(this,R.raw.beep);
            playNotificationSound(this,R.raw.beep);
            playNotificationSound(this,R.raw.beep);
            NotificationUtils.preparePendingIntentForReadiness(this);
        }else if(time == 5 && !userIsReady){
            DeviceInfoUtils.increaseDeviceSound(this);
            playNotificationSound(this,R.raw.beep);
            playNotificationSound(this,R.raw.beep);
            playNotificationSound(this,R.raw.beep);
            playNotificationSound(this,R.raw.beep);
            NotificationUtils.preparePendingIntentForReadiness(this);
        }else if(time < 1 && shortest.getDurationValue() < 10 && !userIsReady){
            DeviceInfoUtils.increaseDeviceSound(this);
            new CountDownTimer(4000, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    playNotificationSound(FirebaseDataSync.this, R.raw.beep);
                }
    
                @Override
                public void onFinish() {
        
                }
            }.start();
            if(!NotificationUtils.isAppIsInBackground(this)){
                if(btn_waiting_time != null) {
                    if (currentOrder.getStatus() == Order.OrderStatusWaiting
                            && !userIsReady) {
                        btn_waiting_time.setVisibility(View.VISIBLE);
                    }else
                        btn_waiting_time.setVisibility(View.GONE);
                }
            }
            NotificationUtils.preparePendingIntentForReadiness(this);
        }
        
    }
    
    @Override
    public void onRoutingCancelled() {
    
    }
}
