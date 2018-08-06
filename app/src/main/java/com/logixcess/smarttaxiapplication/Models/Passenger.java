package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Passenger  implements Parcelable
{
    private Boolean inOnline;
    private double latitude;
    private double longitude;
    private String fk_user_id;
    private Boolean is_working_student;
    private String orgnization_name;
    private String region_name;
    private long priority_level;
    public long getPriority_level() {
        return priority_level;
    }

    public void setPriority_level(long priority_level) {
        this.priority_level = priority_level;
    }
    public Passenger()
    {

    }
    protected Passenger(Parcel in) {
        fk_user_id = in.readString();
        byte tmpIs_working_student = in.readByte();
        is_working_student = tmpIs_working_student == 0 ? null : tmpIs_working_student == 1;
        orgnization_name = in.readString();
        region_name = in.readString();
    }

    public static final Creator<Passenger> CREATOR = new Creator<Passenger>() {
        @Override
        public Passenger createFromParcel(Parcel in) {
            return new Passenger(in);
        }

        @Override
        public Passenger[] newArray(int size) {
            return new Passenger[size];
        }
    };

    public String getFk_user_id() {
        return fk_user_id;
    }

    public void setFk_user_id(String fk_user_id) {
        this.fk_user_id = fk_user_id;
    }

    public Boolean getIs_working_student() {
        return is_working_student;
    }

    public void setIs_working_student(Boolean is_working_student) {
        this.is_working_student = is_working_student;
    }

    public String getOrgnization_name() {
        return orgnization_name;
    }

    public void setOrgnization_name(String orgnization_name) {
        this.orgnization_name = orgnization_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fk_user_id);
        dest.writeByte((byte) (is_working_student == null ? 0 : is_working_student ? 1 : 2));
        dest.writeString(orgnization_name);
        dest.writeString(region_name);
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
