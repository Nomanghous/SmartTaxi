package com.logixcess.smarttaxiapplication.Utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.Interfaces.IDrivers;
import com.logixcess.smarttaxiapplication.Models.Driver;

import java.util.ArrayList;
import java.util.List;

public class FetchDriversBasedOnRadius{
    private final Location location ;
    private final IDrivers listener;
    Context mContext;
    List<Driver> mDrivers;
    DatabaseReference db_ref, ref_drivers;

    public FetchDriversBasedOnRadius(Context context, Location myLocation, IDrivers interfaceDrivers){
        mContext = context;
        mDrivers = new ArrayList<>();
        this.location = myLocation;
        this.listener = interfaceDrivers;
        db_ref = FirebaseDatabase.getInstance().getReference();
        ref_drivers = db_ref.child(Helper.REF_DRIVERS);

        ref_drivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mDrivers = new ArrayList<>();
                    for(DataSnapshot object : dataSnapshot.getChildren()){
                        Driver driver = object.getValue(Driver.class);
                        if(driver != null){
                            if(Helper.checkWithinRadius(location,
                                    new LatLng(driver.getLatitude(),driver.getLongitude()))){
                                if(!mDrivers.contains(driver)) {
                                    mDrivers.add(driver);
                                    broadcastDriver("added",driver);
                                }
                            }
                        }
                    }
                    listener.DriversListAdded(mDrivers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void broadcastDriver(String what, Driver  value) {
        Intent intent = new Intent(Helper.BROADCAST_DRIVER);
        intent.putExtra("data", value);
        intent.putExtra("what", what);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.sendBroadcast(intent);
    }
}
