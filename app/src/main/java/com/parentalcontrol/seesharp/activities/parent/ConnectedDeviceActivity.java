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

public class ConnectedDeviceActivity extends AppCompatActivity {

    private TextView userName;
    private ImageView userImage;
    private CardView appBlocking_connectedDevice, webFiltering_connectedDevice, screenTimeLimit_connectedDevice, aiAssistant_connectedDevice;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;

    private User connectedUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);

        String accountId = getIntent().getExtras().get("accountId").toString();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        userName = findViewById(R.id.userName);
        userImage = findViewById(R.id.userImage);

        appBlocking_connectedDevice = findViewById(R.id.appBLocking_connectedDevice);
        appBlocking_connectedDevice.setOnClickListener(view -> openBlockedAppsActivity(accountId));

        firebaseDatabase.getReference("users").child(accountId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connectedUserData = snapshot.getValue(User.class);

                if (connectedUserData == null) return;

                userName.setText(connectedUserData.fullName);

                if (connectedUserData.profilePic.isEmpty()) {
                    userImage.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void openBlockedAppsActivity(String accountId) {
        Intent intent = new Intent(this, BlockedApplicationsActivity.class);
        intent.putExtra("accountId", accountId);
        startActivity(intent);
    }
}