package com.parentalcontrol.seesharp.services.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.firebase.FirebaseMethod;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.services.intent.MonitorAppBlockingAccessibilityService;

public class ApplicationBlockingAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private User userData;

    @Override
    public void onCreate() {
        super.onCreate();

        userData = null;
        FirebaseMethod.listenToUserDataFromRealtimeDatabase(FirebaseMethod.getCurrentUserUID(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userData = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        String packageName = accessibilityEvent.getPackageName().toString();
        Log.e(TAG, "onAccessibilityEvent: " + packageName);
        Log.e(TAG, "blocked: " + userData.blockedApplications.toString());
        if (userData != null && userData.blockedApplications.contains(packageName)) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            Toast.makeText(ApplicationBlockingAccessibilityService.this, "You tried to open a blocked application!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong!");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 500;
        setServiceInfo(info);

        startService(new Intent(this, MonitorAppBlockingAccessibilityService.class));


        FirebaseMethod.updateDataFieldOfUser(FirebaseMethod.getCurrentUserUID(), "appBlockingState", true,
                taskOnComplete -> {
                    if (taskOnComplete.isSuccessful()) {
                        Toast.makeText(ApplicationBlockingAccessibilityService.this, "Application blocking is now enabled", Toast.LENGTH_LONG).show();
                        FirebaseMethod.updateDataFieldOfUser(FirebaseMethod.getCurrentUserUID(), "installedApplications", DeviceHelper.getListOfInstalledApps(getApplicationContext()),
                                taskOnComplete1 -> {

                                },
                                taskOnFailure1 -> {

                                });
                    }
                },
                taskOnFailure -> {
                    Log.e("Error", taskOnFailure.toString());
                });
    }
}
