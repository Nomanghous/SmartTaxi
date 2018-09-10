package com.logixcess.smarttaxiapplication.Utils;


import com.logixcess.smarttaxiapplication.Fragments.MapFragment;

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



}
