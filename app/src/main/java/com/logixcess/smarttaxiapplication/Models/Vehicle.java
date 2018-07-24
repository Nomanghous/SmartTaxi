package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Vehicle implements Parcelable{

    String vechicle_id, vehicle_brand, vehicle_type, cost_per_km;
    int seating_capacity;
    float base_fare;

    protected Vehicle(Parcel in) {
        vechicle_id = in.readString();
        vehicle_brand = in.readString();
        vehicle_type = in.readString();
        cost_per_km = in.readString();
        seating_capacity = in.readInt();
        base_fare = in.readFloat();
    }

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

    public String getVechicle_id() {
        return vechicle_id;
    }

    public void setVechicle_id(String vechicle_id) {
        this.vechicle_id = vechicle_id;
    }

    public String getVehicle_brand() {
        return vehicle_brand;
    }

    public void setVehicle_brand(String vehicle_brand) {
        this.vehicle_brand = vehicle_brand;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getCost_per_km() {
        return cost_per_km;
    }

    public void setCost_per_km(String cost_per_km) {
        this.cost_per_km = cost_per_km;
    }

    public int getSeating_capacity() {
        return seating_capacity;
    }

    public void setSeating_capacity(int seating_capacity) {
        this.seating_capacity = seating_capacity;
    }

    public float getBase_fare() {
        return base_fare;
    }

    public void setBase_fare(float base_fare) {
        this.base_fare = base_fare;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vechicle_id);
        dest.writeString(vehicle_brand);
        dest.writeString(vehicle_type);
        dest.writeString(cost_per_km);
        dest.writeInt(seating_capacity);
        dest.writeFloat(base_fare);
    }
}

