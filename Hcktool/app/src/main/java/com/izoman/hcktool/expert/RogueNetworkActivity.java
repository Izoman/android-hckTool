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


/**
 * Main view
 */
public class RogueNetworkActivity extends AppCompatActivity {
    TextView textViewBattery, textViewOutputRogue;
    BatteryManager bm;
    private RogueServer rogueServer;
    private RogueAP rogueAP;
    private Handler mHandler;
    private String SSID = "TELENETHOMESPOTX ";


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
                    rogueAP.stopAP(SSID);
                } else {
                    ((Button) view).setText("Stop");
                    textViewOutputRogue.append("-------Preparing AP-------\n");
                    rogueAP = new RogueAP(this.getApplicationContext(), mHandler, textViewOutputRogue);
                    rogueAP.startAP(SSID);
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            rogueServer = new RogueServer(8080, mHandler, textViewOutputRogue);
                            try {
                                rogueServer.start();
                                textViewOutputRogue.append("-------Rogue server has started-------\n");
                                textViewOutputRogue.append("Server address: http://" + getLocalNetIP() +  ":" + rogueServer.getListeningPort() + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 5000);   //5 seconds
                }
        }
    }

    private String getLocalNetIP() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
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
