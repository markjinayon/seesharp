package com.parentalcontrol.seesharp.activities.child;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.firebase.FirebaseMethod;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.services.accessibility.ApplicationBlockingAccessibilityService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.util.HashMap;

public class ApplicationBlockingActivity extends AppCompatActivity {

    private ConstraintLayout accessibilityEnabled_appBlocking, accessibilityDisabled_appBlocking;
    private Button enableAppBlocking_appBlocking;
    private LinearLayout applicationList_appBlocking;

    private User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_blocking);

        accessibilityEnabled_appBlocking = findViewById(R.id.accessibilityEnabled_appBlocking);
        accessibilityDisabled_appBlocking = findViewById(R.id.accessibilityDisabled_appBlocking);

        enableAppBlocking_appBlocking = findViewById(R.id.enableAppBlocking_appBlocking);
        enableAppBlocking_appBlocking.setOnClickListener(view -> openAccessibilitySettings());

        applicationList_appBlocking = findViewById(R.id.applicationList_appBlocking);

        dataListener();
    }

    public void openAccessibilitySettings() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    public void dataListener() {
        FirebaseMethod.listenToUserDataFromRealtimeDatabase(FirebaseMethod.getCurrentUserUID(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationList_appBlocking.removeAllViews();
                userData = snapshot.getValue(User.class);
                assert userData != null;
                if (userData.appBlockingState) {
                    accessibilityEnabled_appBlocking.setVisibility(View.VISIBLE);
                    accessibilityDisabled_appBlocking.setVisibility(View.GONE);
                } else {
                    accessibilityEnabled_appBlocking.setVisibility(View.GONE);
                    accessibilityDisabled_appBlocking.setVisibility(View.VISIBLE);
                }
                FirebaseMethod.updateDataFieldOfUser(FirebaseMethod.getCurrentUserUID(), "appBlockingState", DeviceHelper.isAccessibilityServiceEnabled(getApplicationContext(), ApplicationBlockingAccessibilityService.class),
                        taskOnComplete -> {
                        },
                        taskOnFailure -> Log.e("Error", taskOnFailure.toString()));
                HashMap<String, String> appsMap = new HashMap<>();
                for (String packageName: userData.installedApplications) {
                    try {
                        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch sw = new Switch(getApplicationContext());
                        Drawable icon = getPackageManager().getApplicationIcon(packageName);
                        String appLabel = (String) getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(packageName, 0));

                        appsMap.put(appLabel, packageName);

                        sw.setText(appLabel);
                        sw.setCompoundDrawables(icon, null, null, null);
                        sw.setChecked(userData.blockedApplications.contains(packageName));
                        applicationList_appBlocking.addView(sw);

                        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    userData.blockedApplications.add(appsMap.get(sw.getText().toString()));
                                } else {
                                    while (userData.blockedApplications.remove(appsMap.get(sw.getText().toString()))){}
                                }

                                FirebaseMethod.updateDataFieldOfUser(FirebaseMethod.getCurrentUserUID(), "blockedApplications", userData.blockedApplications,
                                        taskOnComplete -> {
                                        },
                                        taskOnFailure -> Log.e("Error", taskOnFailure.toString()));
                            }
                        });
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}