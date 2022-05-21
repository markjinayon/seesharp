package com.parentalcontrol.seesharp.activities.child;

import com.parentalcontrol.seesharp.R;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ChildDashboardActivity extends AppCompatActivity {

    private Button appBlocking_childDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        appBlocking_childDashboard = findViewById(R.id.appBlocking_childDashboard);
        appBlocking_childDashboard.setOnClickListener(view -> openAppBlockingActivity());
    }

    public void openAppBlockingActivity() {
        startActivity(new Intent(this, ApplicationBlockingActivity.class));
    }

}