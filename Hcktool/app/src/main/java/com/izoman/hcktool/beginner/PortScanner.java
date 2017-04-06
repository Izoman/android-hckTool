package com.izoman.hcktool.beginner;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.izoman.hcktool.R;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;


/**
 * Main view
 */
public class PortScanner extends AppCompatActivity {
    TextView textViewBattery;
    BatteryManager bm;
    boolean scanning;
    LinearLayout containerScan;
    private ProgressDialog dialog;
    AsyncTask task;
    Context ctx;

    ArrayList<Integer> userChosenPortList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_portscanner);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/HACKED.ttf");
        ((TextView)findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textClock)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textViewBattery)).setTypeface(custom_font);
        ((Button)findViewById(R.id.buttonBack)).setTypeface(custom_font);
        ctx = this.getApplicationContext();

        userChosenPortList = new ArrayList<>();
        userChosenPortList.add(443);
        userChosenPortList.add(80);
        userChosenPortList.add(8080);

        containerScan = (LinearLayout) findViewById(R.id.scan_container);
        dialog = new ProgressDialog(this);
        textViewBattery = ((TextView)findViewById(R.id.textViewBattery));
        textViewBattery.setTypeface(custom_font);
        bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        textViewBattery.setText(batLevel + "%");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        scanning = false;
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            textViewBattery.setText(level + "%");
        }
    };

    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonBack) {
            this.finish();
        } else if (view.getId() == R.id.buttonScan) {
            if (scanning) {
                scanning = false;
                task.cancel(true);
            } else {
                containerScan.removeAllViews();
                scanning = true;
                task = new PortScannerTask();
                task.execute();
            }
        }
    }

    private class PortScannerTask extends AsyncTask<Object, String, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(result.size() > 0) {
                Log.d("result", result.get(0));
            } else {
                Toast.makeText(ctx, "No results found", Toast.LENGTH_SHORT);
            }

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.d("Progress update", progress[0]);
            addNewScan(progress[0]);
        }

        @Override
        protected ArrayList<String> doInBackground(Object... params) {
            ArrayList<String> result = new ArrayList<>();
            String host = "localhost";
            InetAddress inetAddress;
            String hostName = "";
            try {
                inetAddress = InetAddress.getLocalHost();
                hostName = inetAddress.getHostName();
                Log.d("Found hostname", hostName);
            } catch (UnknownHostException e) {
               // e.printStackTrace();
                Log.d("Unknown host", e.getMessage());
            }

            if(hostName != "") {
                // Start scanning
                while (scanning) {
                    for (int port = 5000; port <= 6000; port++) {
                        try {
                            Socket socket = new Socket(hostName, port);
                            String text = hostName + " is listening on port " + port;
                            publishProgress(text);
                            result.add(text);
                            result.add("test");
                            socket.close();
                            //System.out.println("Port " + port + " is open");
                        } catch (Exception ex) {
                            publishProgress("Exception socket - " + ex.getMessage() + " caused by - "  + ex.getCause());
                            Log.d("Exception socket", ex.getMessage() + " caused by - "  + ex.getCause());
                        }
                    }
                    scanning = false;
                }
            }
            return result;
        }

        protected void addNewScan(String scanresult) {
            TextView textView1 = new TextView(ctx);
             textView1.setText(scanresult);
            textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
            textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
            containerScan.addView(textView1);
        }

        /**
         * Start or stops portscan
         * While scanning it also adds found results in container (scrollview)
         */
        protected void loadPortscan() {
            // Stop scanning if it was active

        }
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
    }
}
