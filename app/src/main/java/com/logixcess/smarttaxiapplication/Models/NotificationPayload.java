package com.logixcess.smarttaxiapplication.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationPayload implements Parcelable {
    private String user_id, driver_id, percentage_left, group_id, order_id;
    private String title, description;
    private int type;
    public NotificationPayload()
    {

    }

    public NotificationPayload(Parcel in) {
        user_id = in.readString();
        driver_id = in.readString();
        percentage_left = in.readString();
        group_id = in.readString();
        order_id = in.readString();
        title = in.readString();
        description = in.readString();
        type = in.readInt();
    }

    public static final Creator<NotificationPayload> CREATOR = new Creator<NotificationPayload>() {
        @Override
        public NotificationPayload createFromParcel(Parcel in) {
            return new NotificationPayload(in);
        }

        @Override
        public NotificationPayload[] newArray(int size) {
            return new NotificationPayload[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getPercentage_left() {
        return percentage_left;
    }

    public void setPercentage_left(String percentage_left) {
        this.percentage_left = percentage_left;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(driver_id);
        parcel.writeString(percentage_left);
        parcel.writeString(group_id);
        parcel.writeString(order_id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeInt(type);
    }
}
