package com.parentalcontrol.seesharp.activities.child;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.services.accessibility.SeeSharpAccessibilityService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChildDashboardActivity extends AppCompatActivity {

    private CardView appBlocking_childDashboard, screenTimeLimit_childDashboard, webFiltering_childDashboard;

    private Button settings_childDashboard;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        appBlocking_childDashboard = findViewById(R.id.appBLocking_childDashboard);
        appBlocking_childDashboard.setOnClickListener(view -> openAppBlockingActivity());

        screenTimeLimit_childDashboard = findViewById(R.id.screenTimeLimit_childDashboard);
        screenTimeLimit_childDashboard.setOnClickListener(view -> openScreenTimeActivity());

        webFiltering_childDashboard = findViewById(R.id.webFiltering_childDashboard);
        webFiltering_childDashboard.setOnClickListener(view -> changeWebFilteringState());

        settings_childDashboard = findViewById(R.id.settings_childDashboard);
        settings_childDashboard.setOnClickListener(view -> openSettings());

        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        updateDeviceName();

        firebaseDatabase.getReference("users")
                .child(firebaseUser.getUid())
                .child("appBlockingState")
                .setValue(DeviceHelper.isAccessibilityServiceEnabled(getApplicationContext(), SeeSharpAccessibilityService.class));

        firebaseDatabase.getReference("users")
                .child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user == null) return;

                        if (!user.appBlockingState) {
                            Toast.makeText(getApplicationContext(), "You must enable SeeSharpAccessibilityService!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
                        }

                        ((TextView) findViewById(R.id.userName)).setText(user.fullName);

                        ((TextView) findViewById(R.id.webFilteringState)).setText(user.webFilteringState ? "ENABLED":"DISABLED");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void updateDeviceName() {
        firebaseDatabase.getReference("users")
                .child(firebaseUser.getUid())
                .child("deviceName")
                .setValue(DeviceHelper.getDeviceName());
    }

    private void changeWebFilteringState() {
        String state = ((TextView) findViewById(R.id.webFilteringState)).getText().toString();
        boolean newState = !state.equals("ENABLED");
        firebaseDatabase.getReference("users")
                .child(firebaseUser.getUid())
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

    private void openSettings() {
        startActivity(new Intent(this, ChildSettingsActivity.class));
    }

    private void openAppBlockingActivity() {
        startActivity(new Intent(this, ApplicationBlockingActivity.class));
    }

    private void openScreenTimeActivity() {
        startActivity(new Intent(this, ScreenTimeActivity.class));
    }

}