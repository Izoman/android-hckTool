package com.izoman.hcktool.beginner.networkscan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by umuts on 10/06/2017.
 */

public class NetworkScanTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "nstask";

    private WeakReference<Context> mContextRef;

    private ArrayList<ArpItem> arpItems;
    private LinearLayout output;


    public NetworkScanTask(Context context, LinearLayout output) {
        mContextRef = new WeakReference<>(context);
        this.output = output;
    }

    // Reads ARP addresses from internal ARP table
    private void readArpAddresses() {
        arpItems = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    //^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$
                    //..:..:..:..:..:..
                    if (mac.matches("..:..:..:..:..:..")) {
                        ArpItem item = new ArpItem(ip, mac);
                        arpItems.add(item);
                        addProgress("Found mac address: " + mac + "(" + ip + ")");
                        Log.d("MAC", "Found mac address: " + mac + "(" + ip + ")");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Context context = mContextRef.get();

            if (context != null) {

                // First let's fill the ARP arraylist
                readArpAddresses();

                // Get the local network devices
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo = wm.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);


                Log.d(TAG, "activeNetwork: " + String.valueOf(activeNetwork));
                Log.d(TAG, "ipString: " + String.valueOf(ipString));

                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: " + prefix);

                for (int i = 0; i < 255; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    String testIp = prefix + String.valueOf(i);

                    InetAddress address = InetAddress.getByName(testIp);
                    boolean reachable = address.isReachable(300);
                    String hostName = address.getCanonicalHostName();

                    if (reachable) {
                        boolean arpFound = false;
                        for (ArpItem arpI : arpItems) {
                            if (arpI.ip.toString().equals(testIp.toString())) {
                                Log.i(TAG, "MAC:" + arpI.mac + "(" + String.valueOf(testIp) + ") is reachable!");
                                arpFound = true;
                                break;
                            }
                        }
                        if(!arpFound){
                            Log.i(TAG, "Hostaddress: " + address.getHostAddress()
                                    + "\nAddress: " + address.getAddress()
                                    + "\nHostname: " + address.getHostName());
                        }

                        //Log.i(TAG, "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is reachable!");
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Well that's not good.", t);
        }

        return null;
    }

    private void addProgress(String msg) {
        TextView txt = new TextView(mContextRef.get());
        txt.setText(msg);
        txt.setBackgroundColor(0);
        txt.setTextColor(0xff00ddff);
        txt.setPadding(20, 20, 20, 20);
        output.addView(txt);
    }
}
