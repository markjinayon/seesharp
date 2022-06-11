package com.parentalcontrol.seesharp.activities.child;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.parentalcontrol.seesharp.R;

public class PinLockscreenActivity extends AppCompatActivity {

    private String pin, guess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lockscreen);

        pin = getIntent().getExtras().get("pin").toString();
        guess = "";

        findViewById(R.id.n1).setOnClickListener(view -> validate("1"));
        findViewById(R.id.n2).setOnClickListener(view -> validate("2"));
        findViewById(R.id.n3).setOnClickListener(view -> validate("3"));
        findViewById(R.id.n4).setOnClickListener(view -> validate("4"));
        findViewById(R.id.n5).setOnClickListener(view -> validate("5"));
        findViewById(R.id.n6).setOnClickListener(view -> validate("6"));
        findViewById(R.id.n7).setOnClickListener(view -> validate("7"));
        findViewById(R.id.n8).setOnClickListener(view -> validate("8"));
        findViewById(R.id.n9).setOnClickListener(view -> validate("9"));
        findViewById(R.id.n0).setOnClickListener(view -> validate("0"));

        findViewById(R.id.nClr).setOnClickListener(view -> validate("clr"));
    }

    public void validate(String num) {

        if (num.equals("clr") && guess.length() > 0) {
            guess = guess.substring(0, guess.length()-1);
            fillEntry(guess.length());
        }

        if (!num.equals("clr")) {
            fillEntry(guess.length()+1);
            guess += num;
        }

        if (pin.length() == guess.length()) {
            if (pin.equals(guess)) {
                //puntang dash
                startActivity(new Intent(this, ChildDashboardActivity.class));
                finish();
            } else {
                fillEntry(guess.length());
                guess = "";
                fillEntry(guess.length());
                //clear
            }
        }
    }

    public void fillEntry(int len) {
        if (len == 0) {
            findViewById(R.id.entry1).setBackgroundResource(R.drawable.circle);
            findViewById(R.id.entry2).setBackgroundResource(R.drawable.circle);
            findViewById(R.id.entry3).setBackgroundResource(R.drawable.circle);
            findViewById(R.id.entry4).setBackgroundResource(R.drawable.circle);
        } else if (len == 1) {
            findViewById(R.id.entry1).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry2).setBackgroundResource(R.drawable.circle);
            findViewById(R.id.entry3).setBackgroundResource(R.drawable.circle);
            findViewById(R.id.entry4).setBackgroundResource(R.drawable.circle);
        } else if (len == 2) {
            findViewById(R.id.entry1).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry2).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry3).setBackgroundResource(R.drawable.circle);
            findViewById(R.id.entry4).setBackgroundResource(R.drawable.circle);
        } else if (len == 3) {
            findViewById(R.id.entry1).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry2).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry3).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry4).setBackgroundResource(R.drawable.circle);
        } else if (len == 4) {
            findViewById(R.id.entry1).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry2).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry3).setBackgroundResource(R.drawable.filled_circle);
            findViewById(R.id.entry4).setBackgroundResource(R.drawable.filled_circle);
        }
    }
}