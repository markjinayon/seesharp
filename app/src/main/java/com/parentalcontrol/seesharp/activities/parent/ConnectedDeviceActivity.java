package com.parentalcontrol.seesharp.activities.parent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectedDeviceActivity extends AppCompatActivity {

    private TextView userName;
    private ImageView userImage;
    private CardView appBlocking_connectedDevice, aiAssistant_connectedDevice, webFiltering_connectedDevice, screenTimeLimit_connectedDevice, reports_connectedDevice;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;

    private String accountId;

    private User connectedUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);

        accountId = getIntent().getExtras().get("accountId").toString();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        userName = findViewById(R.id.userName);
        userImage = findViewById(R.id.userImage);

        appBlocking_connectedDevice = findViewById(R.id.appBLocking_connectedDevice);
        appBlocking_connectedDevice.setOnClickListener(view -> openBlockedAppsActivity(accountId));

        screenTimeLimit_connectedDevice = findViewById(R.id.screenTimeLimit_connectedDevice);
        screenTimeLimit_connectedDevice.setOnClickListener(view -> openScreenTimeLimitActivity(accountId));

        webFiltering_connectedDevice = findViewById(R.id.webFiltering_connectedDevice);
        webFiltering_connectedDevice.setOnClickListener(view -> changeWebFilteringState());

        reports_connectedDevice = findViewById(R.id.reports_connectedDevice);
        reports_connectedDevice.setOnClickListener(view -> openReportsActivity());

        aiAssistant_connectedDevice = findViewById(R.id.aiAssistant_connectedDevice);
        aiAssistant_connectedDevice.setOnClickListener(view -> openAiAssistantActivity());

        firebaseDatabase.getReference("users").child(accountId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connectedUserData = snapshot.getValue(User.class);

                if (connectedUserData == null) return;

                userName.setText(connectedUserData.fullName);
                ((TextView) findViewById(R.id.deviceName_connectedDevice)).setText(connectedUserData.deviceName);

                ((TextView) findViewById(R.id.webFilteringState_connectedDevice)).setText(connectedUserData.webFilteringState ? "ENABLED":"DISABLED");

                if (connectedUserData.profilePic.isEmpty()) {
                    //userImage.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
                    userImage.setImageResource(R.drawable.student);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openReportsActivity() {
        Intent intent = new Intent(this, ReportsActivity.class);
        intent.putExtra("accountId", accountId);
        startActivity(intent);
    }

    private void openAiAssistantActivity() {
        Intent intent = new Intent(this, ParentAiAssistantActivity.class);
        intent.putExtra("accountId", accountId);
        startActivity(intent);
    }

    private void changeWebFilteringState() {
        String state = ((TextView) findViewById(R.id.webFilteringState_connectedDevice)).getText().toString();
        boolean newState = !state.equals("ENABLED");
        firebaseDatabase.getReference("users")
                .child(accountId)
                .child("webFilteringState")
                .setValue(newState)
                .addOnSuccessListener(task -> {
                    if (newState) {
                        Toast.makeText(getApplicationContext(), "Web filtering is now enabled.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Web filtering is now disabled.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openScreenTimeLimitActivity(String accountId) {
        Intent intent = new Intent(this, CheckScreenTimeActivity.class);
        intent.putExtra("accountId", accountId);
        startActivity(intent);
    }

    private void openBlockedAppsActivity(String accountId) {
        Intent intent = new Intent(this, BlockedApplicationsActivity.class);
        intent.putExtra("accountId", accountId);
        startActivity(intent);
    }
}