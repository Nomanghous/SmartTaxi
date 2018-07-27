package com.logixcess.smarttaxiapplication.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.logixcess.smarttaxiapplication.DriverModule.MapsActivity;

public class MyNotificationManager extends BroadcastReceiver {

    public static final int REQUEST_CODE_ACCEPT_ORDER = 1001;
    public static final int REQUEST_CODE_REJECT_ORDER = 1002;
    public static final int REQUEST_CODE_VIEW_ORDER = 1003;
    public static final String INTENT_FILTER_ACCEPT_ORDER = "accept_order";
    public static final String INTENT_FILTER_REJECT_ORDER = "reject_order";
    public static final String INTENT_FILTER_VIEW_ORDER = "view_order";
    Context mContext = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getExtras() != null ? intent.getExtras().getString("action") : null;
        String data = intent.getExtras() != null ? intent.getExtras().getString("data") : null;
        if(action == null || data == null)
            return;
        fuelUpTheBroadcastReceiver(action, data);
    }

    private void fuelUpTheBroadcastReceiver(String action, String data) {
        Intent intent = null;
        intent = new Intent(action);
        intent.putExtra("action", action);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
