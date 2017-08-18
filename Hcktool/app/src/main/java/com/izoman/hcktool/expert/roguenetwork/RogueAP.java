package com.izoman.hcktool.expert.roguenetwork;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;


/**
 * Created by umuts on 10-Aug-17.
 */

public class RogueAP {
    private Context mContext;
    private Handler mHandler;
    private  TextView output;
    private WifiManager mWifiManager;
    private WifiApControl apControl;
    private  WifiConfiguration wifiConfiguration;

    public  RogueAP(Context mContext, Handler mHandler, TextView  output) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.output = output;
        apControl = WifiApControl.getInstance(mContext);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public Inet4Address getLocalIP4Address(){
        return apControl.getInet4Address();
    }

    /**
     * Method to set SSID  of Device Access Point and Start HotSpot
     *
     * @param SSID a new SSID of your Access Point
     */
    public void startAP(String SSID)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!Settings.System.canWrite(mContext))
            {
                Intent writeSettingIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                writeSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(writeSettingIntent);
            }
        }

        wifiConfiguration  = new WifiConfiguration();
        wifiConfiguration.networkId = 115566;
        wifiConfiguration .SSID = SSID ;
        wifiConfiguration .hiddenSSID = false;
        wifiConfiguration .allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
       // wifiConfiguration .allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
       // apControl.setWifiApEnabled(wifiConfiguration, true);
     //  apControl.setEnabled(wifiConfiguration, true);
        configApState(mContext);

//        mHandler.postDelayed(new Runnable() {
//            public void run() {
//                mWifiManager.addNetwork(wifiConfiguration);
//                mWifiManager.enableNetwork(wifiConfiguration.networkId, false);
//                mWifiManager.setWifiEnabled(false);
//            }
//        }, 1000 * 5);   //5 seconds


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Update UI
                output.append("+++Started AP+++\n");
            }
        });
    }

    //check whether wifi hotspot on or off
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }

    // toggle wifi hotspot on or off
    public static boolean configApState(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if(isApOn(context)) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void stopAP(String SSID) {
        /*
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (config.SSID.contains(SSID)) {
                    mWifiManager.disableNetwork(config.networkId);
                    mWifiManager.removeNetwork(config.networkId);
                }
            }
        }
        mWifiManager.saveConfiguration();
        */
        //apControl.disable();

        configApState(mContext);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Update UI
                output.append("+++Stopped AP+++\n");
            }
        });
    }


}
