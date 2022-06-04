package com.parentalcontrol.seesharp.activities.child;

import com.parentalcontrol.seesharp.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

public class ChildDashboardActivity extends AppCompatActivity {

    private CardView appBlocking_childDashboard, screenTimeLimit_childDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        appBlocking_childDashboard = findViewById(R.id.appBLocking_childDashboard);
        appBlocking_childDashboard.setOnClickListener(view -> openAppBlockingActivity());

        screenTimeLimit_childDashboard = findViewById(R.id.screenTimeLimit_childDashboard);
        screenTimeLimit_childDashboard.setOnClickListener(view -> openScreenTimeActivity());
    }

    public void openAppBlockingActivity() {
        startActivity(new Intent(this, ApplicationBlockingActivity.class));
    }

    public void openScreenTimeActivity() {
        startActivity(new Intent(this, ScreenTimeActivity.class));
    }

}