package com.logixcess.smarttaxiapplication.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.logixcess.smarttaxiapplication.Activities.MyNotificationManager;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.R;

import static com.logixcess.smarttaxiapplication.Activities.MyNotificationManager.sendNotificationForWaiting;
import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.currentOrder;
import static com.logixcess.smarttaxiapplication.Services.FirebaseDataSync.currentUser;

public class Constants
{
    public static String Database_Path = "https://smarttaxi-7ebdc.firebaseio.com/";
    public static String USER_TOKEN = "";
    public static Uri FilePathUri ;
    public static Uri FilePathUri2 ;
    public static String Storage_Path = "images" ;
    public static long date_selected_expiry;
    public static long date_selected_issue;
    public static double BASE_FAIR_PER_KM = 50;
    public static String selected_vehicle = Helper.VEHICLE_CAR;
    public static String user_image_path = "";
    public static String group_id = "";
    public static String notificationPayload = "";
    public static NotificationPayload notificationPayloadObject = null;
    public static int group_radius = 0;
    public static boolean userIsReady = false;
    
    
    public static void playNotificationSound(Context context, int id){
        try {
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if(Settings.Global.getInt(context.getContentResolver(), "zen_mode") == 0) {
                    DeviceInfoUtils.increaseDeviceSound(context);
                    if(id != -1) {
                        final MediaPlayer mp_down = MediaPlayer.create(context, id);
                        mp_down.start();
                    }else {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(context, notification);
                        r.play();
                    }
                }
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    
    
    public static boolean checkDontDisturbMode(Context context){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return Settings.Global.getInt(context.getContentResolver(), "zen_mode") == 0;
            }
            else return false;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    public void showWaitDialog(Activity context){
        CustomDialogClass customDialogClass = new CustomDialogClass(context);
        customDialogClass.show();
    }
    
    
    public class CustomDialogClass extends Dialog implements
            android.view.View.OnClickListener {
        
        public Activity c;
        public Dialog d;
        Button yes;
        EditText wait;
        
        public CustomDialogClass(Activity a) {
            super(a);
            this.c = a;
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_waiting_time);
            wait = findViewById(R.id.editText);
            yes =  findViewById(R.id.button);
            yes.setOnClickListener(this);
        }
        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button:
                    if(TextUtils.isEmpty(wait.getText())) {
                        Toast.makeText(c, "wait time cannot be empty", Toast.LENGTH_SHORT).show();
                        c.finish();
                        
                    }else if(Double.valueOf(wait.getText().toString()) > 30) {
                        Toast.makeText(c, "Time must be less than 30 minutes", Toast.LENGTH_SHORT).show();
                    } else
                        sendWaitTimeNotification(c,wait.getText().toString());
                    c.finish();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    
        
    }
    
    private void sendWaitTimeNotification(Context context,String s) {
        sendNotificationForWaiting(context,s);
    }
}
