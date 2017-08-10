package com.izoman.hcktool.expert.roguenetwork;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;

import java.util.List;

import cc.mvdan.accesspoint.WifiApControl;

/**
 * Created by umuts on 10-Aug-17.
 */

public class RogueAP {
    private Context mContext;
    private Handler mHandler;
    private  TextView output;
    private WifiManager mWifiManager;
    private WifiApControl apControl;

    public  RogueAP(Context mContext, Handler mHandler, TextView  output) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.output = output;
        apControl = WifiApControl.getInstance(mContext);
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

                // Works when calling directly from MainActivity.java,but not from Android Library.WHY??
                // writeSettingIntent.setData(Uri.parse("package: " + mContext.getPackageName()));
                writeSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(writeSettingIntent);
            }
        }
        WifiConfiguration wifiConfiguration  = new WifiConfiguration();
        wifiConfiguration .SSID = SSID;
        wifiConfiguration .hiddenSSID = false;
        wifiConfiguration .allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration .allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        apControl.setEnabled(wifiConfiguration , true);
        final List<WifiApControl.Client> clients = apControl.getClients();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Update UI
                output.append("+++Started AP+++\n");
                for (WifiApControl.Client c: clients) {
                    output.append("AP Client" + c.toString()+ "\n");
                }
            }
        });
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
        apControl.disable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Update UI
                output.append("+++Stopped AP+++\n");
            }
        });
    }


}
