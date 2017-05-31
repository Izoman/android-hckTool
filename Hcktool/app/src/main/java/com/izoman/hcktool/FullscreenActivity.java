package com.izoman.hcktool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Main view
 */
public class FullscreenActivity extends AppCompatActivity {
    Context context;
    TextView textViewBattery;
    BatteryManager bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/HACKED.ttf");
        ((TextView) findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textClock)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textViewBattery)).setTypeface(custom_font);
        ((Button) findViewById(R.id.buttonStart)).setTypeface(custom_font);
        ((Button) findViewById(R.id.buttonAbout)).setTypeface(custom_font);
        context = this.getApplicationContext();

        textViewBattery = ((TextView) findViewById(R.id.textViewBattery));
        textViewBattery.setTypeface(custom_font);
        bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        textViewBattery.setText(batLevel + "%");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // check connectivity
        checkConnection();
    }

    private void checkConnection() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // force app kill if no wifi connection is present
        if (!wifi.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You must have an active Wi-Fi connection to use this application.")
                    .setCancelable(false)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAndRemoveTask();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            textViewBattery.setText(level + "%");
        }
    };

    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonStart) {
            goStart();
        } else if (view.getId() == R.id.buttonAbout) {
            goAbout();
        }
    }

    protected void goStart() {
        Intent intent = new Intent(FullscreenActivity.this, StartActivity.class);
        startActivity(intent);
    }

    protected void goAbout() {
        Intent intent = new Intent(FullscreenActivity.this, AboutActivity.class);
        startActivity(intent);
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
