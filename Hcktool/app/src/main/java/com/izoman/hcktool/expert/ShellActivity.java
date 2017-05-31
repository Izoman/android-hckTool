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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Main view
 */
public class ShellActivity extends AppCompatActivity {
    TextView textViewBattery, textViewShellOut;
    BatteryManager bm;
    EditText editTextCommands;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shell);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/HACKED.ttf");
        ((TextView) findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textClock)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.textViewBattery)).setTypeface(custom_font);
        ((Button) findViewById(R.id.buttonExecute)).setTypeface(custom_font);

        textViewBattery = ((TextView) findViewById(R.id.textViewBattery));
        textViewBattery.setTypeface(custom_font);
        bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        textViewBattery.setText(batLevel + "%");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        textViewShellOut = ((TextView) findViewById(R.id.textViewShellOut));
        editTextCommands = ((EditText) findViewById(R.id.editTextCommands));
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            textViewBattery.setText(level + "%");
        }
    };

    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonExit) {
            this.finish();
        } else if (view.getId() == R.id.buttonExecute) {
            executeCommands();
        }
    }

    protected void executeCommands() {
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(editTextCommands.getText().toString());
            // Reads stdout.
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            textViewShellOut.setText(output.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
