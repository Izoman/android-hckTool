package com.izoman.hcktool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main view
 */
public class FullscreenActivity extends AppCompatActivity  {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/HACKED.ttf");
        ((TextView)findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textClock)).setTypeface(custom_font);
        ((Button)findViewById(R.id.buttonStart)).setTypeface(custom_font);
        ((Button)findViewById(R.id.buttonAbout)).setTypeface(custom_font);
        context = this.getApplicationContext();
    }
    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonStart) {
            CharSequence text = "Start has been clicked!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if (view.getId() == R.id.buttonAbout) {
            goAbout();
        }
    }

    protected void goAbout(){
        Intent intent = new Intent(FullscreenActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}