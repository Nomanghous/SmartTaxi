package com.logixcess.smarttaxiapplication.DriverModule;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.Models.UserFareRecord;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.FareCalculation;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends DriverMainActivity implements OnMapReadyCallback, RoutingListener {


    
    public static final String KEY_CURRENT_SHARED_RIDE = "key_shared_ride";
    public static final String KEY_CURRENT_ORDER = "current_order";
    private GoogleMap mMap;
    private DatabaseReference db_ref_user,db_ref_drivers;
    
    private Location myLocation = null;
    private LatLng dropoff, pickup;
    private String currentOrderId = "";
    private LatLng start, end;
    private ArrayList<LatLng> waypoints;
    private Marker mDriverMarker;
    private ArrayList<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};
    
    private double totalDistance = 0, totalTime = 120; // total time in minutes
    private DatabaseReference db_ref;
    private LatLng driver = null;
    private boolean IS_ROUTE_ADDED = false;
    
    private HashMap<String, Marker> PickupMarkers;
    private CountDownTimer mCountDowntimer;
    private Marker pmarker;
    private ArrayList<LatLng> mPassengerPoints;
    Boolean isSharedRideCompleted =  true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Helper.IS_FROM_CHILD = true;
        super.onCreate(savedInstanceState);
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_user = firebase_db.getReference().child(Helper.REF_USERS);
        db_ref_drivers = firebase_db.getReference().child(Helper.REF_DRIVERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        userMe = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(KEY_CURRENT_ORDER)){
            currentOrder = bundle.getParcelable(KEY_CURRENT_ORDER);
            if(currentOrder != null && currentOrder.getShared()){

                if(currentSharedRide != null && currentSharedRide.getGroup_id() != null) {
                
                } else if(currentOrder.getShared()){
                    fetchThatGroup();
                }

            }else if(currentOrder != null){
            
            }else {
                finish();
                return;
            }

            askLocationPermission();
            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            db_ref = FirebaseDatabase.getInstance().getReference();
            //getting current user value
            db_ref.child(Helper.REF_USERS).child(currentOrder.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        currentUser = dataSnapshot.getValue(User.class);
                        if(currentUser != null && mMap != null) {
                            if(currentOrder.getShared()){}
//                                requestNewRoute();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                
                }
            });
    
            
            
            try {
                if(currentOrder.getShared()) {
                    ordersInSharedRide = new ArrayList<>();
                    goFetchOrderById();
                }
            }catch (NullPointerException i){}
            new Timer().schedule(new Every10Seconds(),5000,10000);
        }else{
            finish();
        }
        //adding order status listener
        checkOrderStatus();
    }
    private void checkOrderStatus()
    {
        if(currentOrder != null)
        {
            db_ref_order.child(currentOrder.getOrder_id()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {
                        Order order = dataSnapshot.getValue(Order.class);
                        assert order != null;
                        if(!order.getShared())
                        {
                            if(order.getStatus() == Order.OrderStatusCompleted || order.getStatus() == Order.OrderStatusCompletedReview)
                            {
                                currentOrder = null;
                                finishAffinity();
                                Intent returnIntent = new Intent(MapsActivity.this,DriverMainActivity.class);
                                startActivity(returnIntent);
                            }
                        }
                        else// means shared ride
                        {
                            if(ordersInSharedRide.size()>0)
                            {
                                for (Order order1 : ordersInSharedRide) {
                                    if (order1.getOnRide() || !((order1.getStatus() == Order.OrderStatusCompleted) || (order1.getStatus() == Order.OrderStatusCompletedReview))) { // means this order is in progress
                                        isSharedRideCompleted = false;
                                    }
                                }
                                if (isSharedRideCompleted)
                                {
                                    currentOrder = null;
                                    ordersInSharedRide.clear();
                                    finishAffinity();
                                    Intent returnIntent = new Intent(MapsActivity.this,DriverMainActivity.class);
                                    startActivity(returnIntent);
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
    }
    //Only adding markers of driver,pickup and destination location in the route
    private void requestNewRoute() {
        
        if(myLocation == null || IS_ROUTE_ADDED)
            return;
        driver = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        if(pickup == null)
            pickup = new LatLng(currentOrder.getPickupLat(), currentOrder.getPickupLong());
        mPassengerPoints = new ArrayList<>();
        mPassengerPoints.add(driver);
        Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
        Bitmap dropoffPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.dropoff_pin),70,120);
        LatLng temp = new LatLng(currentOrder.getDropoffLat(),currentOrder.getDropoffLong());
    
        if(currentOrder.getShared()){
            for(Order order : ordersInSharedRide){
                LatLng pickup = new LatLng(order.getPickupLat(), order.getPickupLong());
                LatLng dropoff = new LatLng(order.getDropoffLat(), order.getDropoffLong());
                mPassengerPoints.add(pickup);
                if(!order.getOrder_id().equals(currentOrder.getOrder_id()))
                    mPassengerPoints.add(dropoff);
                
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MarkerOptions options = new MarkerOptions();
                        options.position(pickup);
                        options.icon(BitmapDescriptorFactory.fromBitmap(pickupPin));
                        options.title(order.getPickup());
                        if(PickupMarkers == null)
                            PickupMarkers = new HashMap<>();
                        if(!PickupMarkers.containsKey(order.getUser_id()))
                            PickupMarkers.put(order.getUser_id(),mMap.addMarker(options));
                        // End marker
                        options = new MarkerOptions();
                        options.position(dropoff);
                        options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
                        mMap.addMarker(options);
                    }
                });
                
            }
        }else{
            mPassengerPoints.add(pickup);
        }
        if(currentOrder.getShared()) {
            mPassengerPoints.add(temp);
            mPassengerPoints = sortPointsByDistance();
        }
        IS_ROUTE_ADDED = true;
    }
    
    private ArrayList<LatLng> sortPointsByDistance() {
        List<LatLng> points = new ArrayList<>();
        int[] indexes = new int[mPassengerPoints.size()];
        for(int i = 0; i < indexes.length; i++)
            indexes[i] = i;
        double prevD = 0;
        double dist = 0;
        points.add(mPassengerPoints.get(0));
        for(int i = mPassengerPoints.size() - 1; i > 0; i--){
            dist = distance(mPassengerPoints.get(0).latitude, mPassengerPoints.get(0).longitude,
                    mPassengerPoints.get(i).latitude,
                    mPassengerPoints.get(i).longitude, 0.0, 0.0);
            if (dist > prevD) {
                prevD = dist;
                if(i == mPassengerPoints.size() - 1)
                    continue;
                int item = indexes[i];
                indexes[i] = indexes[i + 1];
                indexes[i + 1] = item;
                Log.i("Indexes ",indexes[i] + " and " + indexes[i + 1]);
            }
        }
        return mPassengerPoints;
    }
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {
        
        final int R = 6371; // Radius of the earth
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        
        double height = el1 - el2;
        
        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        
        return Math.sqrt(distance);
    }
    private void populateMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askLocationPermission();
                return;
            }
        }
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if(!currentOrder.getShared())
            addRoute();
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }


    
    /*calculate pickup distance for notification*/
    private void calculatePickupDistance(List<Order> ordersInSharedRide){
        if(currentOrder != null && !currentOrder.getShared()){
            Location pickup = new Location("pickup");
            pmarker = PickupMarkers.get(currentUser.getUser_id());
            pickup.setLatitude(pmarker.getPosition().latitude);
            pickup.setLongitude(pmarker.getPosition().longitude);
            double distanceRemaining = myLocation.distanceTo(pickup);
            
//            checkForDistanceToSendNotification(currentOrder, currentUser, distanceRemaining);
        } else if(currentPassengers != null) {
            
            double distanceRemaining;
            Location pickup = new Location("pickup");
            for (User user : currentPassengers) {
                Marker marker = PickupMarkers.get(user.getUser_id());
                pickup.setLatitude(marker.getPosition().latitude);
                pickup.setLongitude(marker.getPosition().longitude);
                distanceRemaining = myLocation.distanceTo(pickup);
                int counter = 0;
                for (Order order : ordersInSharedRide) {
                    if (distanceRemaining < 10 && order.getStatus() == Order.OrderStatusWaiting) {
                        order = goUpdateOrderStatus(order);
                        orderIDs.put(order.getOrder_id(), true);
                        ordersInSharedRide.add(counter, order);
                        currentSharedRide.setOrderIDs(orderIDs);
                        db_ref_group.child(currentSharedRide.getGroup_id()).setValue(currentSharedRide);
                    }
                    this.ordersInSharedRide = ordersInSharedRide;
                    counter++;
                    if (order.getUser_id().equals(user.getUser_id())) {
//                        checkForDistanceToSendNotification(order, user, distanceRemaining);
            
                        if (order.getStatus() == Order.OrderStatusInProgress) {
                            order.setOnRide(true);
                        } else if (order.getStatus() == Order.OrderStatusCompleted || order.getStatus() == Order.OrderStatusCompletedReview) {
                            order.setOnRide(false);
                        }
            
            
                        break;
                    }
                }
            }
    
        }
    
    }
    
    private Order goUpdateOrderStatus(Order order) {
        order.setStatus(Order.OrderStatusInProgress);
        db_ref_order.child(order.getOrder_id()).setValue(order);
        return order;
    }
    
    
    private String escapeValue(String value) {
        return "\""+value+"\"";
    }

    private MarkerOptions getDesiredMarker(float kind, LatLng posToSet, String title) {
        return new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(kind))
                .position(posToSet).title(title);
    }

    public void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint, final Bitmap bitmap) {
        Marker marker = myMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(directionPoint.get(0))
                .flat(true));

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 10));

        animateMarker(myMap, marker, directionPoint, false);
    }
    
    private void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint,
                               final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                i++;


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }


    public void addRoute() {
        waypoints = new ArrayList<>();
        getRoutePoints();
        start = waypoints.get(0);
        end = waypoints.get(waypoints.size() - 1);
        
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(start,12);
        mMap.animateCamera(center);
        PolylineOptions line = new PolylineOptions().addAll(waypoints);
        mMap.addPolyline(line);
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
        Bitmap dropoffPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.dropoff_pin),70,120);
        options.icon(BitmapDescriptorFactory.fromBitmap(pickupPin));
        options.title(currentOrder.getPickup());
        if(PickupMarkers == null)
            PickupMarkers = new HashMap<>();
        if(!PickupMarkers.containsKey(currentOrder.getUser_id()))
           PickupMarkers.put(currentOrder.getUser_id(),mMap.addMarker(options));
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
        mMap.addMarker(options);
    }

    private void getRoutePoints() {
        for (RoutePoints points : currentOrder.getSelectedRoute()){
            waypoints.add(new LatLng(points.getLatitude(),points.getLongitude()));
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.e("Err",e.getMessage() != null ? e.getMessage() : "error getting Route");
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        polylines = new ArrayList<>();
        //add route(s) to the map.
        IS_ROUTE_ADDED = true;
        Route shortestRoute = route.get(shortestRouteIndex);
//        if (totalDistance < 0 || distanceRemaining > totalDistance)
            totalDistance = shortestRoute.getDistanceValue();
        int colorIndex = shortestRouteIndex % COLORS.length;
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10 + shortestRouteIndex * 3);
        polyOptions.addAll(shortestRoute.getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);
        if (driver == null && myLocation != null)
            driver = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//        distanceRemaining = shortestRoute.getDistanceValue();
        if(mDriverMarker == null)
            mDriverMarker = mMap.addMarker(new FareCalculation().getVehicleMarkerOptions(MapsActivity.this, driver, currentOrder.getVehicle_id()));
        if(mDriverMarker != null && driver != null && myLocation != null){
            calculatePickupDistance(ordersInSharedRide);
        }
        
//            checkForDistanceToSendNotification();
    }

    @Override
    public void onRoutingCancelled() {
    
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1001 && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                populateMap();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    private void fetchThatGroup() {
        if(currentSharedRide != null)
            return;
        String groupId = Helper.getConcatenatedID(currentOrder.getOrder_id(), userMe.getUid());
        db_ref_group.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentSharedRide = dataSnapshot.exists() ? dataSnapshot.getValue(SharedRide.class) : null;
                if(currentSharedRide == null || currentSharedRide.getGroup_id() == null){
                    Toast.makeText(MapsActivity.this, "Something went Wrong.", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    orderIDs = currentSharedRide.getOrderIDs();
                    goGetOrdersForGroup();
                    addListenersForOrders(orderIDs,ordersInSharedRide);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    
    private void addListenersForOrders(HashMap<String, Boolean> orderIDs, List<Order> orderList) {
        for(Map.Entry<String,Boolean> entry : orderIDs.entrySet()){
            db_ref_order.child(entry.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Order order = dataSnapshot.getValue(Order.class);
                        if(order != null){
                            if(!checkIfOrderExists(order.getUser_id(),orderList)){
                                orderList.add(order);
                            }else {
                                int index = 0;
                                for(Order o : orderList){
                                    if(o.getUser_id().equals(order.getUser_id())) {
                                        orderList.set(index, order);
                                        break;
                                    }
                                    index++;
                                }
                            }
                            ordersInSharedRide = orderList;
                            getTheNextNearestDropOff();
                        }
                    }
                }
    
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
        
                }
            });
        }
    }
    
    
    private void showDataOnMap() {

        if(mMap != null && currentUser != null){
            // show User
            pickup = new LatLng(currentOrder.getPickupLat(), currentOrder.getPickupLong());
            dropoff = new LatLng(currentOrder.getDropoffLat(), currentOrder.getDropoffLat());
            MarkerOptions options = new MarkerOptions();
            options.title("Pickup").position(pickup).icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_pin));
            pmarker = mMap.addMarker(options);
            MarkerOptions options2 = new MarkerOptions();
            options2.title("Dropoff").position(dropoff).icon(BitmapDescriptorFactory.fromResource(R.drawable.dropoff_pin));
            mMap.addMarker(options2);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
            // show Driver
        }

    }


    private void updateUserLocation(){
        myLocation = LocationManagerService.mLastLocation;
        if(myLocation != null && userMe != null){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mDriverMarker == null  && mMap != null) {
                        driver = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                        mDriverMarker = mMap.addMarker(new FareCalculation().getVehicleMarkerOptions(MapsActivity.this, driver, currentOrder.getVehicle_id()));
                    }
                }
            });

            String latitude = "latitude";
            String longitude = "longitude";
            double lat = Helper.roundOffDouble(myLocation.getLatitude());
            double lng = Helper.roundOffDouble(myLocation.getLongitude());
            db_ref_drivers.child(userMe.getUid()).child(latitude).setValue(lat);
            db_ref_drivers.child(userMe.getUid()).child(longitude).setValue(lng);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        populateMap();
        showDataOnMap();
    }
    
    public void callCurrentPassenger(View view) {
        String phone = getPassengerPhoneNumber();
        if(phone != null && !phone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            startActivity(intent);
        } else{
            Toast.makeText(this, "Phone number is not correct", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private String getPassengerPhoneNumber() {
        for(User u : currentPassengers)
            if(u.getUser_id().equals(currentOrder.getUser_id()))
                return u.getPhone();
        
        return null;
    }
    
    
    private class Every10Seconds extends TimerTask{
        @Override
        public void run() {
            updateUserLocation();
            if(!IS_ROUTE_ADDED ) {
                if(!currentOrder.getShared())
                    requestNewRoute();
                else{
                    if(ordersInSharedRide.size() > 0)
                        requestNewRoute();
                }
            }
            else {
                try {
                    if(mDriverMarker != null && driver != null && myLocation != null) {
                        if(start == null)
                            start = new LatLng(currentOrder.getPickupLat(),currentOrder.getPickupLong());
                        Location location = new Location("pickup");
                        location.setLatitude(start.latitude);
                        location.setLongitude(start.longitude);
                        if(totalDistance == 0)
                            totalDistance = myLocation.distanceTo(location);
//                        distanceRemaining = myLocation.distanceTo(location);
                        driver = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
//                        if(distanceRemaining > totalDistance)
//                            return;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                calculatePickupDistance(ordersInSharedRide);
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runtimeFareCalculation();
        }
    }
    
    
    private void initNextOrderVars(){
        totalDistance = 0;
        totalTime = 0;
        findViewById(R.id.phone_call_container).setVisibility(View.GONE);
        if(mCountDowntimer != null)
            mCountDowntimer.cancel();
        mCountDowntimer = null;
    }
    

    
    
    
    
    private void getTheNextNearestDropOff(){
        double totalDistance = 0;
        boolean isAllOrdersCompleted = true;
        if(userMe == null)
            userMe = FirebaseAuth.getInstance().getCurrentUser();
        if(db_ref_order_to_driver == null)
            db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        
        
        if(currentSharedRide == null) {
            if(currentOrder.getStatus() == Order.OrderStatusCompleted)
                if(db_ref_order_to_driver == null)
                    return;
                db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity.this, "Your Order has been Completed", Toast.LENGTH_SHORT).show();
                        currentOrder = null;
                        ordersInSharedRide = null;
                        orderIDs = null;
                        currentSharedRide = null;
                        currentPassengers = null;
                        finish();
                    }
                });
        }
        else{
            
            for (Order order : ordersInSharedRide){
                if(order.getStatus() == Order.OrderStatusInProgress
                        || order.getStatus() == Order.OrderStatusWaiting){
                    if(currentOrder != null && currentOrder.getOrder_id().equals(order.getOrder_id())){
                        // current order is in progress.
                        return;
                    }
                    Location dropOff = new Location("dropoff");
                    dropOff.setLatitude(order.getDropoffLat());
                    dropOff.setLongitude(order.getDropoffLong());
                    isAllOrdersCompleted = false;
                    
                    if(myLocation == null)
                        continue;
                    
                    if(totalDistance == 0) {
                        totalDistance = myLocation.distanceTo(dropOff);
                        currentOrder = order;
                    }else if(myLocation.distanceTo(dropOff) < totalDistance ) {
                        totalDistance = myLocation.distanceTo(dropOff);
                        currentOrder = order;
                    }
                }
            }
        }
        
        if(isAllOrdersCompleted){
            DatabaseReference db_ref_order_to_driver;
            db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
            db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MapsActivity.this, "Your Order has been Completed", Toast.LENGTH_SHORT).show();
                    currentOrder = null;
                    ordersInSharedRide = null;
                    orderIDs = null;
                    currentSharedRide = null;
                    currentPassengers = null;
                    finish();
                }
            });
        }
    }
    
    private void goFetchOrderById(){
        
        orderIDs = currentSharedRide.getOrderIDs();
        if(currentSharedRide.getGroup_id() == null) {
            fetchThatGroup();
            return;
        }
        goGetOrdersForGroup();
    }
    
    private void goGetOrdersForGroup() {
        for (Map.Entry<String, Boolean> entry : orderIDs.entrySet()) {
            String key = entry.getKey();
            db_ref_order.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists())
                        return;
                    Order order = dataSnapshot.getValue(Order.class);
                    if(order != null){
                        if((order.getStatus() == Order.OrderStatusWaiting
                                || order.getStatus() == Order.OrderStatusInProgress) &&
                                order.getDriver_id().equals(userMe.getUid())){
                            if(!checkIfOrderExists(order.getUser_id(), ordersInSharedRide))
                                ordersInSharedRide.add(order);
                        }
                    }
                    if(orderIDs.size() == ordersInSharedRide.size()) {
                        addMarkersForNewRiders();
                        requestNewRoute();
    
                    }
                }
            
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                
                }
            });
        }
    }
    
    
    
    private void addOrdersListener() throws NullPointerException{
        db_ref_order.child(currentOrder.getOrder_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Order order = dataSnapshot.getValue(Order.class);
                    if(order != null){
                        currentOrder = order;
                        getTheNextNearestDropOff();
                    }
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
       

    }
    
    private void updateOrderLocally(Order order) {
        try {
            int index = 0;
            boolean isToRefresh = false;
            for (Order o : ordersInSharedRide) {
                if (o.getOrder_id().equals(order.getOrder_id())) {
                    if (currentOrder.getOrder_id().equalsIgnoreCase(order.getOrder_id())
                            && order.getDriver_id().equals(userMe.getUid())) {
                        if (order.getStatus() == Order.OrderStatusInProgress) {
                            isToRefresh = true;
                        } else if (order.getStatus() == Order.OrderStatusCompleted) {
                            isToRefresh = true;
                        } else if (order.getStatus() == Order.OrderStatusPending) {
                            isToRefresh = true;
                        }
                    }
                    ordersInSharedRide.set(index, order);
                }
                index++;
            }
            if (isToRefresh) {
                initNextOrderVars();
                getTheNextNearestDropOff();
            }
        }catch (ConcurrentModificationException ignore){}
        
    }
    
    private void markOrderComplete() {
        DatabaseReference db_ref_order_to_driver;
        db_ref_order_to_driver = firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        db_ref_order_to_driver.child(userMe.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MapsActivity.this, "Your Order has been Completed", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
    
    private void addMarkersForNewRiders(){
        if(PickupMarkers == null) {
            PickupMarkers = new HashMap<>();
        }
        if(currentPassengers == null)
            currentPassengers = new ArrayList<>();
        if(ordersInSharedRide != null){
            for(Order order : ordersInSharedRide){
                if(!PickupMarkers.containsKey(order.getUser_id())){
                    Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
                    MarkerOptions options = new MarkerOptions().
                            icon(BitmapDescriptorFactory.fromBitmap(pickupPin))
                            .title(order.getPickup()).position(new LatLng(order.getPickupLat(),order.getPickupLong()));
                    PickupMarkers.put(order.getUser_id(),mMap.addMarker(options));
                    goGetUserById(order.getUser_id());
                }
            }
        }
    }
    
    private void goGetUserById(String user_id) {
        db_ref_user.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
                    if(user != null && currentPassengers != null){
                        if(!currentPassengers.contains(user))
                            currentPassengers.add(user);
                    }
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    
    /*
    *
    *
    * How route thing will work
    *  8 points route
    *
    *
    * */
    
    
    
    
  /*
    Shared Ride Fare Calculation
    */
    private void runtimeFareCalculation(){
        if(currentSharedRide != null) {
            // current ride is shared
            if(myLocation.getLatitude() == 0 && myLocation.getLongitude() == 0)
                return;
            currentSharedRide = mFareCalc.calculateFareForSharedRide(ordersInSharedRide, currentSharedRide, myLocation, currentOrder.getVehicle_id());
            checkOrderStatus(currentSharedRide.getGroup_id());
            
        }
    }
    
    private void checkOrderStatus(String group_id) {
        db_ref_group.child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    SharedRide sr = dataSnapshot.getValue(SharedRide.class);
                    if(sr != null){
                        orderIDs = sr.getOrderIDs();
                        currentSharedRide.setOrderIDs(orderIDs);
                        for (Map.Entry<String, UserFareRecord> entry : currentSharedRide.getPassengerFares().entrySet()) {
                            String key = entry.getKey();
                            double basefare = entry.getValue().getBaseFare();
                            List<RoutePoints> fareRecord = currentSharedRide.getAllJourneyPoints().get(key);
                            if(fareRecord != null) {
                                double total = 0, totalDistanceTravelled = 0;
                                for(RoutePoints rp : fareRecord){
                                    if(rp.getDistanceinmeters() > 0){
                                        totalDistanceTravelled = totalDistanceTravelled + rp.getDistanceinmeters();
                                        // if 1000m fare => basefare then 100 m fare => basefare * .1
                                        //basefare * (rp.getDistanceinmeters() / 1000)
                                        rp.setTotalKmSofar(totalDistanceTravelled / 1000);
                                        total = total + getFareThing((rp.getDistanceinmeters() / 1000), basefare,rp);
                                    }
                                }
                                setUserFareSoFar(total,key,ordersInSharedRide);
                            }
                        }
                        db_ref_group.child(currentSharedRide.getGroup_id()).setValue(currentSharedRide);
                    }
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    private double getFareThing(double kmPercentage, double basefare, RoutePoints rp){
        double currentFare;
        int totalPassengers =(int) rp.getTotalPassengers();
        
        switch (totalPassengers){
            case 1:
                if(rp.getTotalKmSofar() < 1) {// 300 m and 50   300 / 1000 = .3 * 50
                    currentFare = (basefare * kmPercentage);
                }else{
                    currentFare = (basefare * kmPercentage) * .6;
                }
                break;
            case 2:
    
                if(rp.getTotalKmSofar() < 1) { // 4.9  // 2nd passenger
                    currentFare = (basefare * kmPercentage);
                }else if(rp.getTotalKmSofar() < 2) {
                    currentFare = (basefare * kmPercentage) * .6; // discount of 40%
                }else{
                    currentFare = (basefare * kmPercentage) * .5; // discount of 50%
                }
                break; // 1.8 km for 71 (p 2)  1st -> 50rs, 2nd km .6
            case 3:
                if(rp.getTotalKmSofar() < 1) {
                    currentFare = (basefare * kmPercentage);
                }else if(rp.getTotalKmSofar() < 2) {
                    currentFare = (basefare * kmPercentage) * .6;
                }else if(rp.getTotalKmSofar() < 3) {
                    currentFare = (basefare * kmPercentage) * .5;
                }else{
                    currentFare = (basefare * kmPercentage) * .4;
                }
                break;
            default:
                if(rp.getTotalKmSofar() < 1) {
                    currentFare = (basefare * kmPercentage);
                }else if(rp.getTotalKmSofar() < 2) {
                    currentFare = (basefare * kmPercentage) * .6;
                }else if(rp.getTotalKmSofar() < 3) {
                    currentFare = (basefare * kmPercentage) * .5;
                }else if(rp.getTotalKmSofar() < 4) {
                    currentFare = (basefare * kmPercentage) * .4;
                }else{
                    currentFare = (basefare * kmPercentage) * .3;
                }
                break;
        }
        return currentFare;
    }
    
    
    boolean flagModification = false;
    private void setUserFareSoFar(double total, String key, List<Order> ordersInSharedRide) {
        if(flagModification)
            return;
        int index = 0;
        flagModification = true;
        for (Order order : ordersInSharedRide) {
            if (order.getUser_id().equals(key)) {
                db_ref_order.child(order.getOrder_id()).child("total_fare").setValue(total);
                ordersInSharedRide.set(index, order);
                break;
            }
            index++;
        }
        this.ordersInSharedRide = ordersInSharedRide;
        flagModification = false;
    }
    
    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationBroadcastReceiver, new IntentFilter(Helper.BROADCAST_LOCATION));
    }
    
    BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            myLocation = LocationManagerService.mLastLocation;
            if(myLocation != null && mDriverMarker != null){
                mDriverMarker.setPosition(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()));
                Log.i("DriverLocation",myLocation.toString());
            }
        }
    };
    
    
    
}
