package com.logixcess.smarttaxiapplication.Utils;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.logixcess.smarttaxiapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;



public class PushNotifictionHelper extends AsyncTask {
    public final static String AUTH_KEY_FCM = "AAAAcpdY6GE:APA91bH_ZNukpG-ADK06fZJX76BLpWXoyVgK0XkojQZNvIdCeSgiXY7_NGeWZyeOtvyAwGe7cSn3ln4Oa-s22qGTRKMTaGyts4QABrE9M1kgxbAvXTftx4S4FOz9gLKlZmU8hIWqlU7ssO2b5ps5wkqLeBZjrqUAJw";//"Your api key";
    public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
    private Context mContext;
    public PushNotifictionHelper(Context context){
        mContext = context;
    }
    public static String sendPushNotification(String deviceToken, JSONObject jsonPayload)
            throws IOException {
        String result = "";
        URL url = new URL(API_URL_FCM);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM);
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject json = new JSONObject();
        try {
            json.put("to", deviceToken.trim());
            json.put("data", jsonPayload);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            OutputStreamWriter wr = new OutputStreamWriter(
                    conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            result = "Successfully Sent";//CommonConstants.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            result = "Failed to Send";//CommonConstants.FAILURE;
        }
        System.out.println("GCM Notification is sent successfully");

        return result;
    }

    public void sendNotification(String title, String message,Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            sendPushNotification(objects[0].toString(),new JSONObject(objects[1].toString()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(Object o) {
        if(o != null){
            Toast.makeText(mContext, o.toString(), Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(o);
    }
}
