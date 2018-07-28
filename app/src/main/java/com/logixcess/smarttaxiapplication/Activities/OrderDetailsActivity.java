package com.logixcess.smarttaxiapplication.Activities;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Passenger;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.logixcess.smarttaxiapplication.Utils.Constants.SELECTED_RADIUS;
import static com.logixcess.smarttaxiapplication.Utils.Constants.USER_CURRENT_LOCATION;

public class OrderDetailsActivity extends AppCompatActivity {
    TextView tv_pickup, tv_destination, tv_shared, tv_distance, tv_cost, tv_time, tv_vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
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
        //TODO: SHOW SEARCH DIALOG
        // Driver search in the given radius

        //TODO: Select Driver from list
        //Select Driver from list

        //TODO: GET DRIVER IDs
        //get driver ids and send the notifications


        //TODO: CHECK RESPONSE
        // driver who accepts first , will only be entertained.


        //TODO: SHOW DRIVER DETAILS
        // show driver details and start map activity to see live process


        //TODO: DISMISS SEARCH DIALOG
        Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,MapsActivity.class));
        finish();
    }
}
