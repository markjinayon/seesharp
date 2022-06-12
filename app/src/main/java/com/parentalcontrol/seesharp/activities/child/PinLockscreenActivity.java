package com.parentalcontrol.seesharp.activities.child;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.parentalcontrol.seesharp.R;

public class PinLockscreenActivity extends AppCompatActivity {

    private String pin, guess, newPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lockscreen);

        pin = getIntent().getExtras().get("pin").toString();
        guess = "";

        if (pin.equals("")) {
            ((TextView) findViewById(R.id.lockActivity_childSettings)).setText("Change PIN");
            newPin = getIntent().getExtras().get("newPin").toString();
            System.out.println(newPin);
            if (newPin.equals("")) {
                ((TextView) findViewById(R.id.lockSubActivity_childSettings)).setText("Enter a new PIN");
            } else {
                ((TextView) findViewById(R.id.lockSubActivity_childSettings)).setText("Confirm new PIN");
            }
        } else {
            ((TextView) findViewById(R.id.lockActivity_childSettings)).setText("");
            ((TextView) findViewById(R.id.lockSubActivity_childSettings)).setText("Enter PIN");
        }

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

        if (guess.length() == 4) return;

        if (num.equals("clr") && guess.length() > 0) {
            guess = guess.substring(0, guess.length()-1);
            fillEntry(guess.length());
        }

        if (!num.equals("clr")) {
            fillEntry(guess.length()+1);
            guess += num;
        }

        if (pin.length() != 0 && pin.length() == guess.length()) {
            if (pin.equals(guess)) {
                startActivity(new Intent(this, ChildDashboardActivity.class));
                finish();
            } else {
                fillEntry(guess.length());
                guess = "";
                fillEntry(guess.length());
            }
        }

        if (pin.equals("") && guess.length() == 4) {
            if (newPin.equals("")) {
                Intent intent = new Intent(this, PinLockscreenActivity.class);
                intent.putExtra("pin", "");
                intent.putExtra("newPin", guess);
                startActivity(intent);
                finish();
            } else {
                if (newPin.equals(guess)) {
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("pin")
                            .setValue(guess)
                            .addOnSuccessListener(task -> {
                                Toast.makeText(getApplicationContext(), "Updating pin success", Toast.LENGTH_LONG).show();
                                finish();
                            });
                } else {
                    fillEntry(guess.length());
                    guess = "";
                    fillEntry(guess.length());
                }
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