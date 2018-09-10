package com.logixcess.smarttaxiapplication.Utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.logixcess.smarttaxiapplication.Fragments.MapFragment;
import com.logixcess.smarttaxiapplication.R;

public class FareCalculation
{
    public double getBaseFare(String vehicle)
    {
        double base_fair = 0;
        switch(vehicle)
        {
            case Helper.VEHICLE_CAR:
                base_fair =  50;
                break;
            case Helper.VEHICLE_MINI:
                base_fair =  30;
                break;
            case Helper.VEHICLE_NANO:
                base_fair =  20;
                break;
            case Helper.VEHICLE_VIP:
                base_fair =  60;
                break;
            case Helper.VEHICLE_THREE_WHEELER:
                base_fair =  30;
                break;
        }
        return base_fair;
    }
    public double getCost()
    {
        return Constants.BASE_FAIR_PER_KM * Double.parseDouble(MapFragment.new_order.getTotal_kms());
    }
    public double getUserDiscountedPrice(int passenger_count)
    {
        double discounted_price = 0;
        double basic_fair = getCost();
        if(passenger_count == 0)
        {
            discounted_price = basic_fair; // ono one is available for share ride
        }
        else if(passenger_count == 1)
        {
            discounted_price = basic_fair - ((basic_fair / 100.0f) *10 ); // 10%
        }
        else if(passenger_count >= 2)
        {
            discounted_price = basic_fair - ((basic_fair / 100.0f) *5 ); // 5%
        }
        return discounted_price;
    }

    public double getPassengerDiscountedPrice(double basic_fair,int passenger_count)
    {
        double discounted_price = 0;
        if(passenger_count == 0)
        {
            discounted_price = basic_fair - ((basic_fair / 100.0f) *20 ); // 20%
        }
        else if(passenger_count == 1)
        {
            discounted_price = basic_fair - ((basic_fair / 100.0f) *10 ); // 10%
        }
        else if(passenger_count >= 2)
        {
            discounted_price = basic_fair - ((basic_fair / 100.0f) *5 ); // 5%
        }
        return discounted_price;
    }

    
    public MarkerOptions getVehicleMarkerOptions(Context context,LatLng latLng, String vehicleType){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Driver");
        markerOptions.icon(getDrawableByType(context,vehicleType));
        return markerOptions;
    }
    
    private BitmapDescriptor getDrawableByType(Context context, String vehicleType) {
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_option_nano);
        switch (vehicleType){
            case Helper.VEHICLE_CAR:
                drawable = context.getResources().getDrawable(R.drawable.ic_option_car);
                break;
            case Helper.VEHICLE_MINI:
                drawable = context.getResources().getDrawable(R.drawable.ic_option_mini);
                break;
            case Helper.VEHICLE_NANO:
                drawable = context.getResources().getDrawable(R.drawable.ic_option_nano);
                break;
            case Helper.VEHICLE_THREE_WHEELER:
                drawable = context.getResources().getDrawable(R.drawable.ic_option_three_wheeler);
                break;
            case Helper.VEHICLE_VIP:
                drawable = context.getResources().getDrawable(R.drawable.ic_option_vip);
                break;
        }
        Bitmap driverPin = Helper.convertToBitmap(drawable, 100, 100);
        return BitmapDescriptorFactory.fromBitmap(driverPin);
    }
    
    
}
