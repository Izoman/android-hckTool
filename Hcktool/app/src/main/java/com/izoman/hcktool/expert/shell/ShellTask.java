package com.izoman.hcktool.expert.shell;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellTask extends AsyncTask<String, String, String> {
    Activity act;
    Context ctx;
    TextView textViewOutput;
    String input;
    Process process;
    BufferedReader reader;


    public ShellTask(Activity act, Context ctx, String input, TextView output) {
        this.act = act;
        this.ctx = ctx;
        this.textViewOutput = output;
        this.input = input;
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            // Executes the command.
            process = Runtime.getRuntime().exec(input.toLowerCase());

            // Reads stdout.
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            final char[] buffer = new char[4096];
            final StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
                act.runOnUiThread(new Runnable() {
                    public void run() {
                        // some code #3 (Write your code here to run in UI thread)
                        textViewOutput.setText(output.toString());
                    }
                });
                if (isCancelled()) {
                    break;
                }
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

        }
        catch (Exception e) {
            final String err = e.getMessage();
            act.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ctx.getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return "";
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (process != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            process.destroy();
        }
    }
}