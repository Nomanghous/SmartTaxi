package com.logixcess.smarttaxiapplication.Models;



public class WaitingTime{
    String waiting_time;
    String phone_number;
    
    public WaitingTime(){
    
    }
    
    public WaitingTime(String waiting_time, String phone_number) {
        this.waiting_time = waiting_time;
        this.phone_number = phone_number;
    }
    
    public String getWaiting_time() {
        return waiting_time;
    }
    
    public void setWaiting_time(String waiting_time) {
        this.waiting_time = waiting_time;
    }
    
    public String getPhone_number() {
        return phone_number;
    }
    
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}


