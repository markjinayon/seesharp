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

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ChildDashboardActivity extends AppCompatActivity {

    private CardView appBlocking_childDashboard, screenTimeLimit_childDashboard, webFiltering_childDashboard, aiAssistant_childDashboard, settings_childDashboard;
    private ImageView userImage2;

    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        userImage2 = findViewById(R.id.userImage2);

        appBlocking_childDashboard = findViewById(R.id.appBLocking_childDashboard);
        appBlocking_childDashboard.setOnClickListener(view -> openAppBlockingActivity());

        screenTimeLimit_childDashboard = findViewById(R.id.screenTimeLimit_childDashboard);
        screenTimeLimit_childDashboard.setOnClickListener(view -> openScreenTimeActivity());

        aiAssistant_childDashboard = findViewById(R.id.aiAssistant_childDashboard);
        aiAssistant_childDashboard.setOnClickListener(view -> openAiAssistantActivity());

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

                        if (firebaseAuth.getCurrentUser() != null) {
                            if (!DeviceHelper.isAccessibilityServiceEnabled(getApplicationContext(), SeeSharpAccessibilityService.class)) {
                                //Toast.makeText(getApplicationContext(), "Enable SeeSharp's accessibility service!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
                            }

                            if (!checkUsageAccess()) {
                                //Toast.makeText(getApplicationContext(), "Enable SeeSharp's usage access!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
                            }
                        }

                        ((TextView) findViewById(R.id.userName)).setText(user.fullName);
                        ((TextView) findViewById(R.id.deviceName_childDashboard)).setText(user.deviceName);

                        ((TextView) findViewById(R.id.webFilteringState)).setText(user.webFilteringState ? "ENABLED":"DISABLED");

                        if (user.profilePic.isEmpty()) {
                            //userImage.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
                            userImage2.setImageResource(R.drawable.student);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void openAiAssistantActivity() {
        startActivity(new Intent(this, ChildAiAssistantActivity.class));
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

    private boolean checkUsageAccess() {
        Context context = getApplicationContext();
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}