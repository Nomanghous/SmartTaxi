package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.print.PageRange;

import java.util.HashMap;

public class Feedback implements Parcelable
{
    //    -Feedback1
//    -Feedback2
//    -Feedback3
//    -rating1 5.0
//    -rating2 3.0
//     -rating3 2.0
//        -CID
//-ComplainDesc
//-TripID fk
//-driver_id fk
//-customer_id fk
    HashMap<String,Float> feedback1;
    HashMap<String,Float> feedback2;
    HashMap<String,Float> feedback3;
    String fk_order_id;
    String fk_driver_id;
    String complaint;

    public Feedback()
    {}
    protected Feedback(Parcel in) {
        fk_order_id = in.readString();
    }

    public static final Creator<Feedback> CREATOR = new Creator<Feedback>() {
        @Override
        public Feedback createFromParcel(Parcel in) {
            return new Feedback(in);
        }

        @Override
        public Feedback[] newArray(int size) {
            return new Feedback[size];
        }
    };

    public HashMap<String, Float> getFeedback1() {
        return feedback1;
    }

    public void setFeedback1(HashMap<String, Float> feedback1) {
        this.feedback1 = feedback1;
    }

    public HashMap<String, Float> getFeedback2() {
        return feedback2;
    }

    public void setFeedback2(HashMap<String, Float> feedback2) {
        this.feedback2 = feedback2;
    }

    public HashMap<String, Float> getFeedback3() {
        return feedback3;
    }

    public void setFeedback3(HashMap<String, Float> feedback3) {
        this.feedback3 = feedback3;
    }

    public String getFk_order_id() {
        return fk_order_id;
    }

    public void setFk_order_id(String fk_order_id) {
        this.fk_order_id = fk_order_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fk_order_id);
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public String getFk_driver_id() {
        return fk_driver_id;
    }

    public void setFk_driver_id(String fk_driver_id) {
        this.fk_driver_id = fk_driver_id;
    }
}
