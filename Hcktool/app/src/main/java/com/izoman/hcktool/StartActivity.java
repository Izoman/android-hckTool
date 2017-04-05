package com.izoman.hcktool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;


/**
 * Main view
 */
public class StartActivity extends AppCompatActivity  {
    Context context;
    LinearLayout toolsContainer;
    int bgColorRed, bgColorGreen, bgColorBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);
        // Set font hacked
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/HACKED.ttf");
        ((TextView)findViewById(R.id.textViewTitle)).setTypeface(custom_font);
        ((TextView)findViewById(R.id.textClock)).setTypeface(custom_font);
        ((Button)findViewById(R.id.buttonBack)).setTypeface(custom_font);
        context = this.getApplicationContext();
        bgColorRed = ContextCompat.getColor(context, R.color.colorHackingRed);
        bgColorGreen = ContextCompat.getColor(context, R.color.colorHackingGreen);
        bgColorBlue = ContextCompat.getColor(context, R.color.colorHackingBlue);
        toolsContainer = (LinearLayout) findViewById(R.id.tools_container);
    }

    public void buttonClicked(View view) {
        if (view.getId() == R.id.buttonFilterBeginner) {
            // bgColorGreen = beginner
            filterList("beginner");
        } else if (view.getId() == R.id.buttonFilterIntermediate) {
            // bgColorBlue = intermediate
            filterList("intermediate");
        }
        else if (view.getId() == R.id.buttonFilterExpert) {
            // bgColorRed = expert
            filterList("expert");
        }
        else if(view.getId() == R.id.buttonFilterAll) {
            // All
            filterList("0");
        }
        else if (view.getId() == R.id.buttonBack) {
            this.finish();
        }
    }

    /**
     * Filters hacking tool list according to tag (beginner, intermediate, expert)
     * When given 0, everything is shown = no filter
     * @param tag
     */
    protected void filterList(String tag) {
        for (int i = 0; i < toolsContainer.getChildCount(); i++) {
            View v = toolsContainer.getChildAt(i);
            if (v instanceof Button) {
                if(tag != "0") {
                    if(v.getTag().equals(tag)) v.setVisibility(View.VISIBLE);
                    else v.setVisibility(View.GONE);
                } else v.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void goWepTool(){
        Intent intent = new Intent(StartActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}
