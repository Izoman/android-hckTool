package com.izoman.hcktool;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Main view
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/HACKED.ttf");
        ((TextView)findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textClock)).setTypeface(custom_font);
        ((Button)findViewById(R.id.buttonBack)).setTypeface(custom_font);
    }

    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonBack) {
            this.finish();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}
