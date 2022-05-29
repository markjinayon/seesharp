package com.parentalcontrol.seesharp.services.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.model.User;

public class ApplicationBlockingAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private User user;

    @Override
    public void onCreate() {
        super.onCreate();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        user = null;

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            stopSelf();
            return;
        }

        firebaseDatabase.getReference("users")
                .child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 500;
        setServiceInfo(info);

        changeAppBlockingStatus(true, "Application blocking is now enabled");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        String packageName = accessibilityEvent.getPackageName().toString();
        Log.e(TAG, "onAccessibilityEvent: " + packageName);
        Log.e(TAG, "blocked: " + user.blockedApplications.toString());
        if (user != null && user.blockedApplications.contains(packageName)) {
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
    public void onDestroy() {
        super.onDestroy();
        changeAppBlockingStatus(false, "Application blocking is now disabled");
    }

    public void changeAppBlockingStatus(boolean state, String message) {
        firebaseDatabase.getReference("users")
                .child(user.accountId)
                .child("appBlockingState")
                .setValue(state)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ApplicationBlockingAccessibilityService.this, message, Toast.LENGTH_LONG).show();
                        updateInstalledApplications();
                    }
                });
    }

    public void updateInstalledApplications() {
        firebaseDatabase.getReference("users")
                .child(user.accountId)
                .child("installedApplications")
                .setValue(DeviceHelper.getListOfInstalledApps(getApplicationContext()));
    }
}
