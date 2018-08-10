package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Driver implements Parcelable
{
    String driving_license_url,
            user_nic_url ,
            fk_user_id,
            fk_vehicle_id,
            driving_issue_date,
            driving_expiry_date,region_name;
    private Boolean inOnline;
    private double latitude;
    private double longitude;
    private int status; //0 free 1 driver single ride 2 driver shared ride
    public Driver()
    {}
    protected Driver(Parcel in) {
        driving_license_url = in.readString();
        user_nic_url = in.readString();
        fk_user_id = in.readString();
        fk_vehicle_id = in.readString();
        driving_issue_date = in.readString();
        driving_expiry_date = in.readString();
        region_name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel in) {
            return new Driver(in);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    public String getDriving_license_url() {
        return driving_license_url;
    }

    public void setDriving_license_url(String driving_license_url) {
        this.driving_license_url = driving_license_url;
    }

    public String getUser_nic_url() {
        return user_nic_url;
    }

    public void setUser_nic_url(String user_nic_url) {
        this.user_nic_url = user_nic_url;
    }

    public String getFk_user_id() {
        return fk_user_id;
    }

    public void setFk_user_id(String fk_user_id) {
        this.fk_user_id = fk_user_id;
    }

    public String getFk_vehicle_id() {
        return fk_vehicle_id;
    }

    public void setFk_vehicle_id(String fk_vehicle_id) {
        this.fk_vehicle_id = fk_vehicle_id;
    }

    public String getDriving_issue_date() {
        return driving_issue_date;
    }

    public void setDriving_issue(String driving_issue_date) {
        this.driving_issue_date = driving_issue_date;
    }
    public String getDriving_expiry_date() {
        return driving_expiry_date;
    }

    public void setDriving_expiry_date(String driving_expiry_date) {
        this.driving_expiry_date = driving_expiry_date;
    }






    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(driving_license_url);
        dest.writeString(user_nic_url);
        dest.writeString(fk_user_id);
        dest.writeString(fk_vehicle_id);
        dest.writeString(driving_issue_date);
        dest.writeString(driving_expiry_date);
        dest.writeString(region_name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }


    public Boolean getInOnline() {
        return inOnline;
    }

    public void setInOnline(Boolean inOnline) {
        this.inOnline = inOnline;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRegion_name() {
        return region_name;
    }

    public void setRegion_name(String region_name) {
        this.region_name = region_name;
    }
}
