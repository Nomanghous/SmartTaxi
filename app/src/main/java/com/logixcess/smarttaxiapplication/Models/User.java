package com.logixcess.smarttaxiapplication.Models;

public class User
{
    private String user_id ;
    private String name;
    private String phone;
    private String Address;
    private String user_type;
    private String join_date;
    private long priority_level;
    private String user_image_url;
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

    public long getPriority_level() {
        return priority_level;
    }

    public void setPriority_level(long priority_level) {
        this.priority_level = priority_level;
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
}
