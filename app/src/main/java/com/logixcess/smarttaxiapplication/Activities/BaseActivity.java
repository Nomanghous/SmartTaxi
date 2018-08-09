package com.logixcess.smarttaxiapplication.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.logixcess.smarttaxiapplication.Interfaces.IDrivers;
import com.logixcess.smarttaxiapplication.Models.Driver;

import java.util.List;

public class BaseActivity extends AppCompatActivity{
    protected List<Driver> DriversInRadius;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



}
