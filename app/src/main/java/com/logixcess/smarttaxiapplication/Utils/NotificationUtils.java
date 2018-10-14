package com.logixcess.smarttaxiapplication.Utils;

import android.content.Context;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.Activities.MyNotificationManager;
import com.logixcess.smarttaxiapplication.BuildConfig;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.R;

/**
 * Created by Ravi on 31/03/15.
 */
public class NotificationUtils {

    private static final String CHANNEL_ID_ORDER = "channel_order";
    private static String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static int getUniqueInt() {
        return (int) System.currentTimeMillis() / 2;
    }

    public void showNotificationMessage(String title, String message, String timeStamp, Intent intent) {
        showNotificationMessage(title, message, timeStamp, intent, null);
    }

    public void showNotificationMessage(final String title, final String message, final String timeStamp, Intent intent, String imageUrl) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;


        // notification icon
        final int icon = R.mipmap.ic_launcher;

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext);

        final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + mContext.getPackageName() + "/raw/notification");

        if (!TextUtils.isEmpty(imageUrl)) {

            if (imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

                Bitmap bitmap = getBitmapFromURL(imageUrl);

                if (bitmap != null) {
                    showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound);
                } else {
                    showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound);
                }
            }
        } else {
            showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound);
            playNotificationSound();
        }
    }


    private void showSmallNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, String timeStamp, PendingIntent resultPendingIntent, Uri alarmSound) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.addLine(message);

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(inboxStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, String timeStamp, PendingIntent resultPendingIntent, Uri alarmSound) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID_BIG_IMAGE, notification);
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method checks if the app is in background or not
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void showNotificationForUserActions(Context context, String payload){
        NotificationPayload notificationPayload = null;
        try {
            notificationPayload = new Gson().fromJson(payload, NotificationPayload.class);
        }catch (Exception ignore){
            Log.e(TAG, ignore.getMessage());
        }
        if(notificationPayload == null){
            return;

        }else {



            if(isAppIsInBackground(context)){

            }
            Constants.notificationPayloadObject = notificationPayload;
            switch (notificationPayload.getType()) {
                case Helper.NOTI_TYPE_ORDER_CREATED:
                    if(!isAppIsInBackground(context))
                        return;
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    preparePendingIntentForFriendRequest(context,payload,notificationPayload);
                    break;
                case Helper.NOTI_TYPE_ORDER_CREATED_FOR_SHARED_RIDE:
                    if(!isAppIsInBackground(context))
                        return;
                    
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    preparePendingIntentForFriendRequest(context,payload,notificationPayload);
                    break;
                case Helper.NOTI_TYPE_ACCEPTANCE_FOR_SHARED_RIDE:
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    fuelUpTheBroadcastReceiver(context,Helper.BROADCAST_DRIVER_RESPONSE,payload);
                    break;
                case Helper.NOTI_TYPE_ORDER_ACCEPTED:
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    preparePendingIntentForMessage(context,payload,notificationPayload);
                    break;
                case Helper.NOTI_TYPE_ORDER_WAITING:
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    playsound(context,"beep.mp3");
                    preparePendingIntentForMessage(context,payload,notificationPayload);
                    break;
                case Helper.NOTI_TYPE_ORDER_WAITING_LONG:
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    playsound(context,"beep_beep_beep.mp3");
                    preparePendingIntentForMessage(context,payload,notificationPayload);
                    break;
                case Helper.NOTI_TYPE_ORDER_COMPLETED:
                    // friend requested=
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    preparePendingIntentForMessage(context,payload,notificationPayload);
                    break;
                case Helper.NOTI_TYPE_CALL:
                    saveDataToFirebase(notificationPayload,notificationPayload.getUser_id());
                    preparePendingIntentForCall(context, payload);
                    break;
            }
        }
    }


    private static void fuelUpTheBroadcastReceiver(Context context, String action, String data) {
        Intent intent = null;
        intent = new Intent(action);
        intent.putExtra("action", action);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }


    private static void saveDataToFirebase(NotificationPayload notificationPayload, String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child(Helper.REF_NOTIFICATIONS).child(userId).push().setValue(notificationPayload);
    }

    private static void playsound(Context context, String soundWithFileFormat){
        try {
            Uri path = Uri.parse("android.resource://"+context.getPackageName()+"/raw/".concat(soundWithFileFormat));
            Ringtone r = RingtoneManager.getRingtone(context, path);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void preparePendingIntentForMessage(Context context, String payload, NotificationPayload userData) {
        int id = getUniqueInt();
        Intent viewIntent = new Intent(context, MyNotificationManager.class);
        if(userData.getType() == Helper.NOTI_TYPE_ORDER_COMPLETED){
            viewIntent.setAction(MyNotificationManager.INTENT_FILTER_COMPETED_ORDER);
            viewIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_COMPETED_ORDER);
        }else{
            viewIntent.setAction(MyNotificationManager.INTENT_FILTER_VIEW_ORDER);
            viewIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_VIEW_ORDER);
        }
        viewIntent.putExtra("data", payload);
        viewIntent.putExtra("id", id);
        PendingIntent viewPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), viewIntent, 0);
        sendNotificationsWithPendingIntent(context, userData.getTitle(), userData.getDescription() != null ? userData.getDescription() : "", null, viewPendingIntent,id);
    }

    public static void preparePendingIntentForFriendRequest(Context context, String payload, NotificationPayload userData) {
        int id = getUniqueInt();
        Intent acceptIntent = new Intent(context, MyNotificationManager.class);
        acceptIntent.setAction(MyNotificationManager.INTENT_FILTER_ACCEPT_ORDER);
        acceptIntent.putExtra("data", payload);
        acceptIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_ACCEPT_ORDER);
        acceptIntent.putExtra("id", id);
        PendingIntent acceptPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), acceptIntent, 0);
        Intent rejectIntent = new Intent(context, MyNotificationManager.class);
        rejectIntent.setAction(MyNotificationManager.INTENT_FILTER_REJECT_ORDER);
        rejectIntent.putExtra("data", payload);
        rejectIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_REJECT_ORDER);
        rejectIntent.putExtra("id", id);
        PendingIntent rejectPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), rejectIntent, 0);
        Intent viewIntent = new Intent(context, MyNotificationManager.class);
        viewIntent.setAction(MyNotificationManager.INTENT_FILTER_VIEW_ORDER);
        viewIntent.putExtra("data", payload);
        viewIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_VIEW_ORDER);
        viewIntent.putExtra("id", id);
        PendingIntent viewPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), viewIntent, 0);
        List<NotificationCompat.Action> actions = new ArrayList<>();
        actions.add(new NotificationCompat.Action(0, "Accept", acceptPendingIntent));
        actions.add(new NotificationCompat.Action(0, "Reject", rejectPendingIntent));
        Constants.notificationPayload = payload;
        
        sendNotificationsWithPendingIntent(context, userData.getTitle(),
                userData.getDescription(), actions, viewPendingIntent,id);
    }
    public static void preparePendingIntentForReadiness(Context context) {
        int id = getUniqueInt();
        Intent acceptIntent = new Intent(context, MyNotificationManager.class);
        acceptIntent.setAction(MyNotificationManager.INTENT_FILTER_READINESS_YES);
        acceptIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_READINESS_YES);
        acceptIntent.putExtra("id", id);
        PendingIntent acceptPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), acceptIntent, 0);
        Intent rejectIntent = new Intent(context, MyNotificationManager.class);
        rejectIntent.setAction(MyNotificationManager.INTENT_FILTER_READINESS_NO);
        rejectIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_READINESS_NO);
        rejectIntent.putExtra("id", id);
        PendingIntent rejectPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), rejectIntent, 0);
        
        List<NotificationCompat.Action> actions = new ArrayList<>();
        actions.add(new NotificationCompat.Action(0, "Yes", acceptPendingIntent));
        actions.add(new NotificationCompat.Action(0, "No", rejectPendingIntent));
        
        sendNotificationsWithPendingIntent(context, "Driver is Reaching",
                "Are You Ready?", actions, null,id);
    }
    public static void preparePendingIntentForCall(Context context,String payload) {
        int id = getUniqueInt();
        Intent acceptIntent = new Intent(context, MyNotificationManager.class);
        acceptIntent.setAction(MyNotificationManager.INTENT_FILTER_CALL);
        acceptIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_CALL);
        acceptIntent.putExtra("data", payload);
        acceptIntent.putExtra("id", id);
        PendingIntent acceptPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), acceptIntent, 0);
        
        List<NotificationCompat.Action> actions = new ArrayList<>();
        actions.add(new NotificationCompat.Action(0, "Yes", acceptPendingIntent));
        actions.add(new NotificationCompat.Action(0, "No", null));
        
        sendNotificationsWithPendingIntent(context, "Driver is Reaching",
                "Are You Ready?", actions, null,id);
    }
    
    public static void preparePendingIntentDriverCall(Context context,String waitingTime
            , String  number) {
        int id = getUniqueInt();
        Intent acceptIntent = new Intent(context, MyNotificationManager.class);
        acceptIntent.setAction(MyNotificationManager.INTENT_FILTER_CALL_FORCE);
        acceptIntent.putExtra("action", MyNotificationManager.INTENT_FILTER_CALL_FORCE);
        acceptIntent.putExtra("data", number);
        acceptIntent.putExtra("id", id);
        PendingIntent acceptPendingIntent =
                PendingIntent.getBroadcast(context, getUniqueInt(), acceptIntent, 0);
        
        List<NotificationCompat.Action> actions = new ArrayList<>();
        actions.add(new NotificationCompat.Action(0, "Yes", acceptPendingIntent));
        actions.add(new NotificationCompat.Action(0, "No", null));
        
        sendNotificationsWithPendingIntent(context, "Do You want to call?",
                "User is not ready. Waiting Time is ".concat(waitingTime).concat("m"), actions, null,id);
    }

    private static void sendNotificationsWithPendingIntent(Context context,String title,
                                                           String message, List<NotificationCompat.Action> actions
            ,PendingIntent contentIntent,int id) {
        if (Build.VERSION.SDK_INT >= 27) {
            // Call some material design APIs here
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_ORDER)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);
            if(actions != null)
                for (NotificationCompat.Action action : actions) {
                    mBuilder.addAction(action);
                }
            mBuilder.notify();
        } else {
            // Implement this feature without material design
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);
            if(actions != null)
                for (NotificationCompat.Action action : actions) {
                    mBuilder.addAction(action);
                }
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());
        }

    }

    private static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


    

}
