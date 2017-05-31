package com.izoman.hcktool.beginner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.izoman.hcktool.R;
import com.izoman.hcktool.beginner.md5.AsyncResponse;
import com.izoman.hcktool.beginner.md5.MD5Api;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Main view
 */
public class MD5Activity extends AppCompatActivity implements AsyncResponse {
    TextView textViewBattery;
    BatteryManager bm;

    private TextView hashText;
    private LinearLayout output;
    private Button decrypt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_md5);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/HACKED.ttf");
        ((TextView) findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textClock)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textViewBattery)).setTypeface(custom_font);
        ((Button) findViewById(R.id.buttonBack)).setTypeface(custom_font);

        textViewBattery = ((TextView) findViewById(R.id.textViewBattery));
        textViewBattery.setTypeface(custom_font);
        bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        textViewBattery.setText(batLevel + "%");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        hashText = ((TextView) findViewById(R.id.hashText));
        output = ((LinearLayout) findViewById(R.id.output));
        decrypt = ((Button) findViewById(R.id.decryptBtn));
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
        } else if (view.getId() == R.id.decryptBtn) {
            if (checkFields()) decrypt();
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

    private boolean checkFields() {
        String hash = this.hashText.getText().toString();
        if (hash.isEmpty()) {
            showError("Enter a valid MD5 hash.");
            return false;
        } else if (hash.length() != 32) {
            showError("Invalid hash length. An MD5 hash consists of 32 characters.");
            return false;
        }
        return true;
    }

    private void decrypt() {
        String hash = this.hashText.getText().toString();
        addProgress("Attempting to decrypt hash\n" + hash);
        MD5Api api = new MD5Api(hash, this);
        api.execute();
        decrypt.setEnabled(false);
    }

    private void addProgress(String msg) {
        TextView txt = new TextView(getApplicationContext());
        txt.setText(msg);
        txt.setBackgroundColor(0);
        txt.setTextColor(0xff00ddff);
        txt.setPadding(20, 20, 20, 20);
        output.addView(txt);
    }

    private void showError(String msg) {
        Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void processFinish(String result) {
        if (result.isEmpty())
            addProgress("No entry found. The hash could not be decrypted.");
        else
            addProgress("Entry found. Plain text: " + result);
        decrypt.setEnabled(true);
    }
}
