package com.logixcess.smarttaxiapplication.Activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class MyNotificationManager extends BroadcastReceiver {

    public static final String INTENT_FILTER_ACCEPT_ORDER = "accept_order";
    public static final String INTENT_FILTER_REJECT_ORDER = "reject_order";
    public static final String INTENT_FILTER_VIEW_ORDER = "view_order";
    public static final String INTENT_FILTER_COMPETED_ORDER = "completed_order";
    Context mContext = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getExtras() != null ? intent.getExtras().getString("action") : null;
        String data = intent.getExtras() != null ? intent.getExtras().getString("data") : null;
        if(action == null || data == null)
            return;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
            notificationManager.cancel(123);
        NotificationPayload notificationPayload = new Gson().fromJson(data,NotificationPayload.class);
        if(notificationPayload != null) {
            if(notificationPayload.getType() == Helper.NOTI_TYPE_ORDER_ACCEPTED)
                startMainActivity(data);
            else if(notificationPayload.getType() == Helper.NOTI_TYPE_ORDER_CREATED_FOR_SHARED_RIDE
                    && action.equals(INTENT_FILTER_ACCEPT_ORDER)){
                sendNotificationToRequestGroupRide(notificationPayload.getUser_id(),context,notificationPayload);
                Toast.makeText(context, "New Passenger has added to Trip", Toast.LENGTH_SHORT).show();
            }else
                fuelUpTheBroadcastReceiver(action, data);
        }

    }

    public void sendNotificationToRequestGroupRide(String passengerID,Context context, NotificationPayload payload)
    {
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference().child(Helper.REF_USERS);
        db_ref_user.child(passengerID).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User driver = dataSnapshot.getValue(User.class);
                    if(driver == null)
                        return;
                    String token = driver.getUser_token();
                    NotificationPayload notificationPayload = new NotificationPayload();
                    notificationPayload.setType(Helper.NOTI_TYPE_ACCEPTANCE_FOR_SHARED_RIDE);
                    notificationPayload.setTitle("\"Request Accepted\"");
                    notificationPayload.setDescription("\"Your Group Ride Request is Accepted\"");
                    notificationPayload.setUser_id("\""+payload.getUser_id()+"\"");
                    notificationPayload.setDriver_id("\""+payload.getDriver_id()+"\"");
                    notificationPayload.setOrder_id("\""+payload.getOrder_id()+"\"");
                    notificationPayload.setPercentage_left("\""+-1+"\"");
                    String str = new Gson().toJson(notificationPayload);
                    try {
                        JSONObject json = new JSONObject(str);
                        new PushNotifictionHelper(context).execute(token,json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(context,"Driver not found!",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fuelUpTheBroadcastReceiver(String action, String data) {
        Intent intent = null;
        intent = new Intent(action);
        intent.putExtra("action", action);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }
    private void startMainActivity(String payload) {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(INTENT_FILTER_VIEW_ORDER, payload);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
