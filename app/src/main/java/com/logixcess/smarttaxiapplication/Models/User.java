package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;


public class User implements Parcelable
{
    private String user_id ;
    private String name="";
    private String phone="";
    private String Address="";
    private String user_type="";
    private String join_date="";
    private String user_image_url="";
    private String user_token="";
    private String password="";
    private String email="";

    public User(){

    }

    protected User(Parcel in) {
        user_id = in.readString();
        name = in.readString();
        phone = in.readString();
        Address = in.readString();
        user_type = in.readString();
        join_date = in.readString();
        user_image_url = in.readString();
        user_token = in.readString();
        password = in.readString();
        email = in.readString();

    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getJoin_date() {
        return join_date;
    }

    public void setJoin_date(String join_date) {
        this.join_date = join_date;
    }


    public String getUser_image_url() {
        return user_image_url;
    }

    public void setUser_image_url(String user_image_url) {
        this.user_image_url = user_image_url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(Address);
        dest.writeString(user_type);
        dest.writeString(join_date);
        dest.writeString(user_image_url);
        dest.writeString(user_token);
        dest.writeString(password);
        dest.writeString(email);

    }
    public String getUser_token() {
        return user_token;
    }

    public void setUser_token(String user_token) {
        this.user_token = user_token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public Boolean getInOnline() {
//        return inOnline;
//    }
//
//    public void setInOnline(Boolean inOnline) {
//        this.inOnline = inOnline;
//    }
//
//    public double getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
}
