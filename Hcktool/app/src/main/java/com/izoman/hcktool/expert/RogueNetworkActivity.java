package com.izoman.hcktool.expert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.izoman.hcktool.R;
import com.izoman.hcktool.expert.roguenetwork.RogueAP;
import com.izoman.hcktool.expert.roguenetwork.RogueServer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * Main view
 */
public class RogueNetworkActivity extends AppCompatActivity {
    TextView textViewBattery, textViewOutputRogue;
    BatteryManager bm;
    private RogueServer rogueServer;
    private RogueAP rogueAP;
    private Handler mHandler;
    private String SSID = "TELENETHOMESPOTV2";
    private Context mContext;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_roguenetwork);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/HACKED.ttf");
        ((TextView)findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textClock)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textViewBattery)).setTypeface(custom_font);
        ((Button)findViewById(R.id.buttonExit)).setTypeface(custom_font);

        textViewBattery = ((TextView)findViewById(R.id.textViewBattery));
        textViewBattery.setTypeface(custom_font);
        bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        textViewBattery.setText(batLevel + "%");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        textViewOutputRogue =   ((TextView)findViewById(R.id.textViewOutputRogue));
        textViewOutputRogue.setMovementMethod(new ScrollingMovementMethod());
        mHandler = new Handler();
        mContext = this.getApplicationContext();
        wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        if(!isAPEnabled()) {
            textViewOutputRogue.append("-------Hotspot/AP is not enabled-------\n");
        }


    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            textViewBattery.setText(level + "%");
        }
    };

    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonExit) {
            this.finish();
        } else if (view.getId() == R.id.buttonRogueServerStart) {
                if(rogueServer != null && rogueServer.isAlive()) {
                    ((Button) view).setText("Start");
                    rogueServer.stop();
                    textViewOutputRogue.append("-----Rogue server has stopped-------\n");
                    //rogueAP.stopAP(SSID);
                } else {
                            try {
                                // Because Android API doesn't let programmers access AP/hotspot from Android 6.0+, the user needs to enable it manually from settings...
                                if(isAPEnabled()) {
                                    ((Button) view).setText("Stop");
                                    rogueServer = new RogueServer(8080, mHandler, textViewOutputRogue, mContext);
                                /*
                                // Secure SSL(https settings) - Can't be used for local network (gives certificate errors)
                                File keystoreFile = new File("src/main/resources/keystore.jks");
                                System.setProperty("javax.net.ssl.trustStore", keystoreFile.getAbsolutePath());
                                rogueServer.setServerSocketFactory(new SecureServerSocketFactory(NanoHTTPD.makeSSLSocketFactory("/" + keystoreFile.getName(), "hacktool".toCharArray()), null));
                                */
                                    rogueServer.start();
                                    textViewOutputRogue.append("-------Rogue server has started-------\n");
                                    textViewOutputRogue.append("Server address: http://192.168.43.1:" + rogueServer.getListeningPort() + "\n");
                                } else {
                                    textViewOutputRogue.append("-------Please enable hotspot/AP and try again-------\n");
                                    startActivity(
                                            new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                                }
                                //textViewOutputRogue.append("-------Preparing AP-------\n");
                               // rogueAP = new RogueAP(mContext, mHandler, textViewOutputRogue);
                                //rogueAP.startAP(SSID);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                }
        }
    }

    boolean isAPEnabled() {
        int apState = 0;
        try {
            apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (apState == 13) {
           return true;
        }
        return  false;
    }

    private String getLocalNetIP() {
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // Unregister battery stat receiver
        this.unregisterReceiver(this.mBatInfoReceiver);
        if(rogueServer != null) {
            rogueServer.stop();
        }
    }
}
