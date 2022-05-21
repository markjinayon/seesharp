package com.parentalcontrol.seesharp.services.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.firebase.FirebaseMethod;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.services.intent.MonitorAppBlockingAccessibilityService;

public class ApplicationBlockingAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.e(TAG, "onAccessibilityEvent: ");
        String packageName = accessibilityEvent.getPackageName().toString();
        Log.e(TAG, "package name is : " + packageName);
        PackageManager packageManager = this.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence applicationLabel = packageManager.getApplicationLabel(applicationInfo);
            Log.e(TAG, "app name is : " + applicationLabel);

            if (applicationLabel.equals("Messenger")) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
