
/*
 * Copyright (C) Logixcess, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by M. Noman <Nomanghous@hotmail.com>, Copyright (c) 2018.
 *
 */

package com.logixcess.smarttaxiapplication.CustomerModule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Feedback;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FareCalculation;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.currentDriver;
import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.currentOrder;
import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.driverLocation;
@SuppressLint("StaticFieldLeak")
public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    public static final String KEY_CURRENT_SHARED_RIDE = "key_shared_ride";
    public static GoogleMap mMap;
    
    
    private DatabaseReference db_ref_order, db_ref_group;

    private LatLng  pickup;
    private LatLng start, end;
    private ArrayList<LatLng> wayPoints;
    private boolean IS_ROUTE_ADDED = false;
    public static final String KEY_CURRENT_ORDER = "current_order";
    public static Marker mDriverMarker;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};

    private double totalDistance = 0; // total time in minutes
    private double distanceRemaining = 0;
    private LatLng driver = null;
    public static TextView total_fare;
    public static Button btn_waiting_time;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FirebaseDatabase firebase_db = FirebaseDatabase.getInstance();
        db_ref_order = firebase_db.getReference().child(Helper.REF_ORDERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(KEY_CURRENT_ORDER)){
            if(currentOrder != null && currentOrder.getShared()){
            
            }else if(currentOrder != null) {
            
            }else{
                finishAffinity();
                Intent returnIntent = new Intent(CustomerMapsActivity.this,MainActivity.class);
                startActivity(returnIntent);
                return;
            }
            askLocationPermission();
            setContentView(R.layout.activity_maps_customer);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            if(driverLocation == null)
                driverLocation = new Location("driver");

            
        }else{
            // driver id not provided
            finishAffinity();
            Intent returnIntent = new Intent(CustomerMapsActivity.this,MainActivity.class);
            startActivity(returnIntent);
        }
    
        btn_waiting_time = findViewById(R.id.btn_waiting_time);
        
       
        total_fare = findViewById(R.id.total_fare);
//        if(currentOrder.getStatus() == Order.OrderStatusInProgress)
//            total_fare.setText(String.valueOf(currentOrder.getTotal_fare()));
    }

    private void requestNewRoute() {

        if(driverLocation == null || IS_ROUTE_ADDED)
            return;
        driver = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
        if(pickup == null)
            pickup = new LatLng(currentOrder.getPickupLat(), currentOrder.getPickupLong());
        List<LatLng> points = new ArrayList<>();
        points.add(driver);
        points.add(pickup);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(points)
                .build();
        routing.execute();
    }

    private void populateMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askLocationPermission();
                return;
            }
        }
        
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if(mDriverMarker != null)
            return;
        addRoute();
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }
    public void addRoute() {
        wayPoints = new ArrayList<>();
        getRoutePoints();
        start = wayPoints.get(0);
        end = wayPoints.get(wayPoints.size() - 1);

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(start,12);
        mMap.animateCamera(center);
        PolylineOptions line = new PolylineOptions().addAll(wayPoints);
        mMap.addPolyline(line);
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude()));
        driver = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
        mDriverMarker = mMap.addMarker(new FareCalculation().getVehicleMarkerOptions(CustomerMapsActivity.this, driver, currentOrder.getVehicle_id()));
        Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
        Bitmap dropoffPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.dropoff_pin),70,120);
        options.title(currentOrder.getPickup()).position(start);
        options.icon(BitmapDescriptorFactory.fromBitmap(pickupPin));
        mMap.addMarker(options);
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.title(currentOrder.getDropoff());
        options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
        mMap.addMarker(options);




    }

    private void getRoutePoints() {
        for (RoutePoints points : currentOrder.getSelectedRoute()){
            wayPoints.add(new LatLng(points.getLatitude(),points.getLongitude()));
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        //Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

//        polylines = new ArrayList<>();
        //add route(s) to the map.
        //add route(s) to the map.
        IS_ROUTE_ADDED = true;
        Route shortestRoute = route.get(shortestRouteIndex);
        if (totalDistance < 0 || distanceRemaining > totalDistance)
            totalDistance = shortestRoute.getDistanceValue();
        int colorIndex = shortestRouteIndex % COLORS.length;
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10 + shortestRouteIndex * 3);
        polyOptions.addAll(shortestRoute.getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        if (driver == null && driverLocation != null)
            driver = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
        distanceRemaining = shortestRoute.getDistanceValue();

        if(mDriverMarker != null && driver != null && driverLocation != null){
        
        }


    }

    public void markOrderAsComplete(View view) {
        // change the order status
        if(currentOrder.getShared()){
            DatabaseReference db_temp = FirebaseDatabase.getInstance().getReference().child(Helper.REF_GROUPS).child(currentOrder.getGroup_id());
                    db_temp.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                SharedRide sharedRide = dataSnapshot.getValue(SharedRide.class);
                                if(sharedRide != null && sharedRide.getOrderIDs() != null){
                                      HashMap<String,Boolean> orderId = sharedRide.getOrderIDs();
                                      if(orderId.containsKey(currentOrder.getOrder_id())){
                                          orderId.put(currentOrder.getOrder_id(), false);
                                          sharedRide.setOrderIDs(orderId);
                                          db_temp.setValue(sharedRide);
                                      }
                                      
                                    
                                }
                            }
                        }
    
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
        
                        }
                    });
        }
        db_ref_order.child(currentOrder.getOrder_id()).child("status").setValue(Order.OrderStatusCompleted).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CustomerMapsActivity.this, "Order Successfully Completed", Toast.LENGTH_SHORT).show();
                    showRatingDialog(CustomerMapsActivity.this,currentOrder);
                    
                }
            }
        });
    }

    
    
    
    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1001){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                populateMap();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showDataOnMap() {
        if(mMap != null && currentDriver != null){
            // show User
            pickup = new LatLng(currentOrder.getPickupLat(), currentOrder.getPickupLong());
            Bitmap pickupPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.pickup_pin),70,120);
            Bitmap dropoffPin = Helper.convertToBitmap(getResources().getDrawable(R.drawable.dropoff_pin),70,120);
            MarkerOptions options = new MarkerOptions();
            options.title(currentOrder.getPickup()).position(start);
            options.icon(BitmapDescriptorFactory.fromBitmap(pickupPin));
            mMap.addMarker(options);
            // End marker
            options = new MarkerOptions();
            options.position(end);
            options.title(currentOrder.getDropoff());
            options.icon(BitmapDescriptorFactory.fromBitmap(dropoffPin));
            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
            // show Driver
        }
    }

    

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                //driverLocation = location;
                //if(driver == null && SELECTED_DRIVER != null)
                  //  requestNewRoute();
            }
        });

        populateMap();
//        showDataOnMap();
    }

    RatingBar rb_review1,rb_review2,rb_review3;
    TextView tv_Destination,tv_Pickup,tv_driver_name;
    Button btn_feedback;
    EditText et_complaint;
    HashMap<String,Float> feedback11 = new HashMap<>();
    HashMap<String,Float> feedback22 = new HashMap<>();
    HashMap<String,Float> feedback33 = new HashMap<>();
    public void showRatingDialog(final Context context, final Order order) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_feedback, null, false);
        btn_feedback = view.findViewById(R.id.btn_feedback);
        Button btn_close = view.findViewById(R.id.btn_close);
        rb_review1 = view.findViewById(R.id.rb_review1);
        rb_review2 = view.findViewById(R.id.rb_review2);
        rb_review3 = view.findViewById(R.id.rb_review3);
        tv_Destination = view.findViewById(R.id.tv_Destination);
        et_complaint = view.findViewById(R.id.et_complaint);
        tv_Pickup = view.findViewById(R.id.tv_Pickup);
        tv_driver_name = view.findViewById(R.id.tv_driver_name);
        tv_Pickup.setText(order.getPickup());
        tv_Destination.setText(order.getDropoff());
        tv_driver_name.setText(order.getDriver_name());
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishAffinity();
                Intent returnIntent = new Intent(CustomerMapsActivity.this,MainActivity.class);
                startActivity(returnIntent);
                
            }
        });
        
        btn_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(tv_Pickup.getText().toString().equalsIgnoreCase("empty") || tv_Destination.getText().toString().equalsIgnoreCase("empty"))
                {
                    Toast.makeText(CustomerMapsActivity.this,"No Driver Found",Toast.LENGTH_SHORT).show();
                    return;
                }
                Feedback feedback = new Feedback();
                feedback.setFk_driver_id(order.getDriver_id());
                feedback.setFk_order_id(order.getOrder_id());
                if(!TextUtils.isEmpty(et_complaint.getText()))
                    feedback.setComplaint(et_complaint.getText().toString());
                feedback11.put(getString(R.string.review1),rb_review1.getRating());
                feedback22.put(getString(R.string.review2),rb_review2.getRating());
                feedback33.put(getString(R.string.review3),rb_review3.getRating());
                feedback.setFeedback1(feedback11);
                feedback.setFeedback2(feedback22);
                feedback.setFeedback3(feedback33);
                DatabaseReference db_ref_feedback = FirebaseDatabase.getInstance().getReference().child(Helper.REF_FEEBACK).child(order.getOrder_id());
                DatabaseReference db_ref_order = FirebaseDatabase.getInstance().getReference().child(Helper.REF_ORDERS).child(order.getOrder_id());
                db_ref_feedback.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                        
                        }
                        else
                        {
                            db_ref_feedback.setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(CustomerMapsActivity.this, "Thank you for your Feedback !", Toast.LENGTH_SHORT).show();
                                    db_ref_order.child("status").setValue(Order.OrderStatusCompletedReview).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                Toast.makeText(CustomerMapsActivity.this, "Order Mark As Complete !", Toast.LENGTH_SHORT).show();
                                            finishAffinity();
                                            Intent returnIntent = new Intent(CustomerMapsActivity.this,MainActivity.class);
                                            startActivity(returnIntent);
                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Toast.makeText(CustomerMapsActivity.this,"Published",Toast.LENGTH_SHORT).show();
            }
        });
        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(R.color.colorAccent);
        window.setGravity(Gravity.CENTER);
        dialog.show();
    }
    
    public void showWaitingTimeDialog(View view) {
        Constants constants = new Constants();
        constants.showWaitDialog(this);
    }
    
    public void startOrder(View view) {
        String group_id = currentOrder.getGroup_id();
        db_ref_group.child(group_id).child("orderIDs").child(currentOrder.getOrder_id()).setValue(true);
        db_ref_order.child(currentOrder.getOrder_id()).child("status").setValue(Order.OrderStatusInProgress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CustomerMapsActivity.this, "Order Started", Toast.LENGTH_SHORT).show();
                    view.setVisibility(View.GONE);
                    findViewById(R.id.btn_mark_complete).setVisibility(View.VISIBLE);
                }
            }
        });
        
    }
}
