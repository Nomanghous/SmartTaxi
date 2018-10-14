package com.logixcess.smarttaxiapplication.Activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Requests;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.Models.WaitingTime;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.currentOrder;
import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.currentUser;

public class MyNotificationManager extends BroadcastReceiver {
    // Filters to check the purpose of notification.
    public static final String INTENT_FILTER_ACCEPT_ORDER = "accept_order";
    public static final String INTENT_FILTER_REJECT_ORDER = "reject_order";
    public static final String INTENT_FILTER_VIEW_ORDER = "view_order";
    public static final String INTENT_FILTER_COMPETED_ORDER = "completed_order";
    public static final String INTENT_FILTER_READINESS_YES = "ready";
    public static final String INTENT_FILTER_READINESS_NO= "not_ready";
    public static final String INTENT_FILTER_CALL = "call";
    public static final String INTENT_FILTER_CALL_FORCE = "force_call";
    Context mContext = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getExtras() != null ? intent.getExtras().getString("action") : null;
        String data = intent.getExtras() != null ? intent.getExtras().getString("data") : null;
        int id = intent.getExtras() != null ? intent.getExtras().getInt("id") : -1;
        if(action == null || (data == null && (!action.equals(INTENT_FILTER_READINESS_YES) && !action.equals(INTENT_FILTER_READINESS_YES))) || id == -1)
            return;
    
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
            notificationManager.cancel(id);
        
        
        if(action.equals(INTENT_FILTER_CALL_FORCE)){
            callPhone(data,context);
            return;
        }
        
        
        
        NotificationPayload notificationPayload = new Gson().fromJson(data,NotificationPayload.class);
        if(notificationPayload != null) {

            if (notificationPayload.getType() == Helper.NOTI_TYPE_ORDER_ACCEPTED) {
                startMainActivity(data);
                //Shared Ride Request -> driver accepted
            } else if (notificationPayload.getType() == Helper.NOTI_TYPE_ORDER_CREATED_FOR_SHARED_RIDE
                    || notificationPayload.getType() == Helper.NOTI_TYPE_ACCEPTANCE_FOR_SHARED_RIDE) {
                sendNotificationToRequestGroupRide(notificationPayload.getUser_id(), context, notificationPayload, action);
                Toast.makeText(context, "Request Accepted", Toast.LENGTH_SHORT).show();
                //Single Ride Request -> driver accepted
            }else if(notificationPayload.getType() == Helper.NOTI_TYPE_ORDER_CREATED) {
                sendNotificationToRequestGroupRide(notificationPayload.getUser_id(), context, notificationPayload, action);
                Toast.makeText(context, "Request Accepted", Toast.LENGTH_SHORT).show();
            // driver want to call the passenger if waiting
            } else if(action.equals(INTENT_FILTER_CALL)){
                callPhone(notificationPayload.getUser_id(),context);
            }else
                fuelUpTheBroadcastReceiver(action, data);
        }else {
            /// user is ready for ride
            if(action.equals(INTENT_FILTER_READINESS_YES)){
                Constants.userIsReady = true;
                // user isn't ready yet.
            }else if(action.equals(INTENT_FILTER_READINESS_NO)){
                Constants.userIsReady = false;
                sendNotificationForCall(context);
            }
        }

    }
    
    private void callPhone(String phone, Context context) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)).addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    // sending response back to passenger that user has accepted the shared ride.
    public void sendNotificationToRequestGroupRide(String passengerID, Context context, NotificationPayload payload, String action)
    {
        boolean isAccepted = action.equals(INTENT_FILTER_ACCEPT_ORDER);
        updateRequest(payload.getDriver_id(),passengerID,isAccepted ? Requests.STATUS_ACCEPTED : Requests.STATUS_REJECTED);
    }
    // sending notification to driver for the call.
    private void sendNotificationForCall(Context context){
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference().child(Helper.REF_USERS);
        db_ref_user.child(currentOrder.getDriver_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User passenger = dataSnapshot.getValue(User.class);
                    if(passenger == null)
                        return;
                    String token = passenger.getUser_token();
                    NotificationPayload notificationPayload = new NotificationPayload();
                    notificationPayload.setType(Helper.NOTI_TYPE_CALL);
                    notificationPayload.setTitle("\"User is not ready yet.\"");
                    notificationPayload.setDescription("\"Do you want to call?\"");
                    notificationPayload.setUser_id("\""+ currentUser.getPhone() +"\"");
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
                    Toast.makeText(context,"Passenger not found!",Toast.LENGTH_SHORT).show();
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    // sending notification to driver for the waiting time.
    public static void sendNotificationForWaiting(Context context, String waitTime){
        
        DatabaseReference db_waiting_time = FirebaseDatabase.getInstance().getReference().child(Helper.REF_WAITING_TIME);
        db_waiting_time.child(currentOrder.getOrder_id()).setValue(new WaitingTime(waitTime,currentUser.getPhone()));
        
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference().child(Helper.REF_USERS);
        db_ref_user.child(currentOrder.getDriver_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User passenger = dataSnapshot.getValue(User.class);
                    if(passenger == null)
                        return;
                    String token = passenger.getUser_token();
                    NotificationPayload notificationPayload = new NotificationPayload();
                    notificationPayload.setType(Helper.NOTI_TYPE_CALL);
                    notificationPayload.setTitle("\"User is not ready yet.\"");
                    notificationPayload.setDescription("\"Please wait for "+ waitTime +" minutes\"");
                    notificationPayload.setUser_id("\""+ currentUser.getPhone() +"\"");
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
                    Toast.makeText(context,"Passenger not found!",Toast.LENGTH_SHORT).show();
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    
    private void updateRequest(String driverId, String userId, int status){
        Requests requests = new Requests(driverId,userId,status);
        String res_id = Helper.getConcatenatedID(userId,driverId);
        FirebaseDatabase firebase_db = FirebaseDatabase.getInstance();
        DatabaseReference db_ref_requests = firebase_db.getReference().child(Helper.REF_REQUESTS);
        db_ref_requests.child(res_id).setValue(requests).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

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
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
    
    
    
  
    
    
}
