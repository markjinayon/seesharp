package com.parentalcontrol.seesharp.activities.child;

import com.google.firebase.auth.FirebaseAuth;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ChildDashboardActivity extends AppCompatActivity {

    private CardView appBlocking_childDashboard, screenTimeLimit_childDashboard;

    private Button settings_childDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        appBlocking_childDashboard = findViewById(R.id.appBLocking_childDashboard);
        appBlocking_childDashboard.setOnClickListener(view -> openAppBlockingActivity());

        screenTimeLimit_childDashboard = findViewById(R.id.screenTimeLimit_childDashboard);
        screenTimeLimit_childDashboard.setOnClickListener(view -> openScreenTimeActivity());

        settings_childDashboard = findViewById(R.id.settings_childDashboard);
        settings_childDashboard.setOnClickListener(view -> openSettings());
    }

    public void openSettings() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void openAppBlockingActivity() {
        startActivity(new Intent(this, ApplicationBlockingActivity.class));
    }

    public void openScreenTimeActivity() {
        startActivity(new Intent(this, ScreenTimeActivity.class));
    }

}