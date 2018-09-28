/*
 * Copyright (C) Logixcess, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Noman Ghous <Nomanghous@hotmail.com>, Copyright (c) 2018.
 *
 */

package com.logixcess.smarttaxiapplication.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static com.logixcess.smarttaxiapplication.Utils.Constants.checkDontDisturbMode;

public class DeviceInfoUtils {
    public static boolean isPlugged(Context context) {
        boolean isPlugged = false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }
    
    public static int getBatteryLevel(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        
        int batteryPct = 100;
        // Calculate Battery Pourcentage ...
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        
        if (level != -1 && scale != -1) {
            batteryPct = (int) ((level / (float) scale) * 100f);
        }
        return batteryPct;
        
    }
    
    
    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }
    
    public static int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
        
    }
    
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int signalStrength(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
// for example value of first element
        int strength;
        try {
            CellInfoGsm cellinfogsm = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
            }
            CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
            strength = cellSignalStrengthGsm.getLevel();
            
            
        } catch (Exception e) {
            
            strength = -1;
        }
        
        return strength;
    }
    
    
    public static void increaseDeviceSound(Context mContext) {
        if (!checkDontDisturbMode(mContext))
            return;
        AudioManager mobileMode = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        assert mobileMode != null;
        mobileMode.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);
        mobileMode.setStreamVolume(AudioManager.STREAM_RING, 100, 0);
        switch (mobileMode.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                
                mobileMode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mobileMode.setStreamVolume(AudioManager.STREAM_RING, mobileMode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                mobileMode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mobileMode.setStreamVolume(AudioManager.STREAM_RING, mobileMode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                break;
            
        }
    }
    
}