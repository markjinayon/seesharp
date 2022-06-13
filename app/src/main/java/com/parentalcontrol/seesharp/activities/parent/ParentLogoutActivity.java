package com.parentalcontrol.seesharp.activities.parent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;

public class ParentLogoutActivity extends AppCompatActivity {

    private CardView connectedDevice, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_logout);

        connectedDevice = findViewById(R.id.connectedDevice_parent);
        settings = findViewById(R.id.settings_parent);

        connectedDevice.setOnClickListener(view -> openDashboardActivity());
        settings.setOnClickListener(view -> openSettingsActivity());
    }

    private void openDashboardActivity() {
        startActivity(new Intent(this, ParentDashboardActivity.class));
    }

    private void openSettingsActivity() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}