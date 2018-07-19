package com.logixcess.smarttaxiapplication.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Helper;

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

    public void goConfirmBooking(View view) {
        Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,MapsActivity.class));
        finish();
    }
}
