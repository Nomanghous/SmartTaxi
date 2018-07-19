package com.logixcess.smarttaxiapplication.Utils;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.logixcess.smarttaxiapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PushNotifictionHelper {
    public final static String AUTH_KEY_FCM = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCfGQHG5gjiCZ/YUBVwyoE71wzVk49/SBCHk1C4RlKAjtWDDeA/UtLWwUAkZhovVXSsRxVhJW/FtDtW3vzRQX4KmXSisICzecRMkEmy9p1cM5LeCZ11GTTNxAD/LUXhCwI+pP0FLGh5qyhPNcuqdfd4Iv8M9Ge+xZaGKEjcgDuuNHxAQZoO/Wjq/oyJlvi5ysCloD+Em0P7ZeazkU4g2MK422DorkAk0GCIJa+ZCTKTiYwEc4mNfsP/nTXlZK3g8zBJwIIJpRVZXF1a7lk06Nwh9R/Ei3MsewNY8igdQxWb2CjAKVSfaz2a40RjpOE6FZSOJ+Dci5BMhKvoovxonbWHAgMBAAECggEAEzE0k4ECIaZl58sisv8IH6cIZNFOq7I5UD7iAMDMSLbc+djC1vtk9MQjj+CDQyIswhrhxjIlGB/u/UKcE/AxxhHkozrl46tfle7dUnSeigCstGf2MdSPF/gPjg4w56i+JhQdc7ImOPB/8II9pea3f4C/7MOk/5CdYQ9bWWzcsN3Hd2eFQzhUYFqLE2JdcH/xVvwhyJ1xV9oq/UHKpAZHPfDEXz3vKFonO8wCPbwQRdMVws4c8y4ceLwGY9Z+KNn+sMQ2tdYhXLhpxbGRYqKmRPEOnKbZi/o5LHgTifp39N/GHRnwPFV9ZiX/u2zGjDm7HCT19Opqx4or5mP1rRkNWQKBgQDbb4XOCpJfr1XGnTLj8YBJPoDniHnFDa7RLoU4ltUVPSYtU7fYxv3+PmUVh0wouRUQQpZKQBTuH9HKAg0egEIQeUz/tTs4hsKoT1b9T/IZ44Clandq4mBsK/n2OagRB+W3fM0NAbcmd0EqzD2iINGnZn8HwwW4vaejs0voMKcOqQKBgQC5m6UldP5cno8u+yMXr1QuOUhNc/i379ST35eNlmA0HfKpXje4Rbb7NxDjwWmaUfDQQQo3aHimeoRr9nCeJgdHQyHVc1ZYtP1fCZgcqeIj9N/bhVy/pd/FSn42/iRDGahVSjO2ISXAYVVZhmtM52DoTqOHY83KwhNm57B1qDMwrwKBgE4RezU2paTNKGMlAjn+bXmhdxLohwoIOC3LszHS17RasP5nTn0dLrgJUsU6AWprjobeacG40rl1MJoB6ND1KIvb2/0NaShPKWnf8D7m9mcnfVlca2fnag6SBrgHAzgr6xpAmGMMPTC+NL0ZNMQ9kNlRqdgFFkAHmWCwJU1SId0hAoGBAJxeiYKGDymel8if4Y+5sXUD42sFUjw8fF5cWAT79G7T8DVKYC0J5r+8Q6ltr/dII1aABQhrJoIy489FHtnN9gHOh/mZIty3FmLz5Uf4w8FBMztxHpODPoOrX6wa7hftzMiWK/y77l8mYU58FXgD0rvETLbqmVzBprDibuKg4weXAoGAHU+MDKGrTvG9tu22lSx+302hJQ1QTi8tHMLbKADDcfVCEr3PmOywptm+si8tkHXVa1yv1ZaVgWdPBl2cNBK1mCftamqMC8QZKJ6yj3qwNrc3XG4nTL/Gonz0X08c5GAHMcd1Ka2hO5jTSXOfVFYetgQPUIy3PrIiPaBB4Vep6H0=";//"Your api key";
        public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

        public static String sendPushNotification(String deviceToken)
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
            JSONObject info = new JSONObject();
            try {
                json.put("to", deviceToken.trim());
                info.put("title", "notification title"); // Notification title
                info.put("body", "message body"); // Notification
                // body
                json.put("notification", info);
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
}
