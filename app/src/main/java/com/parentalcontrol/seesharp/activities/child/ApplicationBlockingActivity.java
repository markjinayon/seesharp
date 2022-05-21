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
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

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

                for (String packageName: userData.installedApplications) {
                    try {
                        Drawable icon = getPackageManager().getApplicationIcon(packageName);
                        Switch sw = new Switch(getApplicationContext());
                        sw.setText(packageName);
                        sw.setCompoundDrawables(icon, null, null, null);
                        ImageView temp = new ImageView(getApplicationContext());
                        temp.setImageDrawable(icon);
                        applicationList_appBlocking.addView(sw);
                        applicationList_appBlocking.addView(temp);
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