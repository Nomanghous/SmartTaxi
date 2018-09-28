/*
 * Copyright (C) Logixcess, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Noman Ghous <Nomanghous@hotmail.com>, Copyright (c) 2018.
 *
 */

package com.logixcess.smarttaxiapplication.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.logixcess.smarttaxiapplication.Fragments.MapFragment;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class MiniMapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    public Activity c;
    public Dialog d;
    Button yes;
    List<LatLng> latLngs = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        c = MiniMapActivity.this;
        latLngs = Helper.invitationLatlngs;
        setContentView(R.layout.activity_mini_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_mini);
        mapFragment.getMapAsync(this);
        yes =  findViewById(R.id.btn_close);
        yes.setOnClickListener(this);
        
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                c.finish();
                break;
            default:
                break;
        }
    }
    
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0),12f));
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).width(5f).color(Color.GREEN);
        googleMap.addPolyline(polylineOptions);
    }
    
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    
    }
}
