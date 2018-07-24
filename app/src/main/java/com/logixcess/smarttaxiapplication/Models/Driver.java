package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Driver implements Parcelable
{
    String driving_license_url, user_nic_url , fk_user_id, fk_vehicle_id, driving_issue,date, driving_expiry_date;


    protected Driver(Parcel in) {
        driving_license_url = in.readString();
        user_nic_url = in.readString();
        fk_user_id = in.readString();
        fk_vehicle_id = in.readString();
        driving_issue = in.readString();
        date = in.readString();
        driving_expiry_date = in.readString();
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

    public String getDriving_issue() {
        return driving_issue;
    }

    public void setDriving_issue(String driving_issue) {
        this.driving_issue = driving_issue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
        dest.writeString(driving_issue);
        dest.writeString(date);
        dest.writeString(driving_expiry_date);
    }


}
