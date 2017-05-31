package com.izoman.hcktool.expert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.izoman.hcktool.R;

import java.util.ArrayList;

import jpcap.NetworkInterface;


/**
 * Main view
 */
public class ArpSpoofer extends AppCompatActivity {
    TextView textViewBattery;
    EditText editTextIP1, editTextIP2;
    BatteryManager bm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_arpspoofer);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/HACKED.ttf");
        ((TextView) findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textClock)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textViewBattery)).setTypeface(custom_font);
        editTextIP1 = ((EditText) findViewById(R.id.editTextIP1));
        editTextIP2 = ((EditText) findViewById(R.id.editTextIP2));

        ((Button) findViewById(R.id.buttonBack)).setTypeface(custom_font);


        textViewBattery = ((TextView) findViewById(R.id.textViewBattery));
        textViewBattery.setTypeface(custom_font);
        bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        textViewBattery.setText(batLevel + "%");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        NetworkInterface[] device=jpcap.JpcapCaptor.getDeviceList();
        String usrA = editTextIP1.getText().toString();
        String usrB = editTextIP2.getText().toString();
        ArrayList<String> users = new ArrayList<>();
        users.add(usrA);
        users.add(usrB);

        ARPSpoof spoof = new ARPSpoof(device[0], users);
        spoof.start();

    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            textViewBattery.setText(level + "%");
        }
    };


    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonBack) {
            this.finish();
        } else if (view.getId() == R.id.buttonStart) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister battery stat receiver
        this.unregisterReceiver(this.mBatInfoReceiver);
    }
}
