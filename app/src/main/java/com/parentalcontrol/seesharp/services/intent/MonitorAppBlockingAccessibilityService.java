package com.parentalcontrol.seesharp.services.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.parentalcontrol.seesharp.firebase.FirebaseMethod;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.services.accessibility.ApplicationBlockingAccessibilityService;

public class MonitorAppBlockingAccessibilityService extends IntentService {
    public MonitorAppBlockingAccessibilityService() {
        super("MonitorAppBlockingAccessibilityService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        myFunction();
    }

    private void myFunction() {
        while (DeviceHelper.isAccessibilityServiceEnabled(getApplicationContext(), ApplicationBlockingAccessibilityService.class)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        FirebaseMethod.updateDataFieldOfUser(FirebaseMethod.getCurrentUserUID(), "appBlockingState", false,
                taskOnComplete -> {
                    if (taskOnComplete.isSuccessful()) {
                        Toast.makeText(MonitorAppBlockingAccessibilityService.this, "Application blocking is now disabled", Toast.LENGTH_LONG).show();
                    }
                    stopSelf();
                },
                taskOnFailure -> Log.e("Error", taskOnFailure.toString()));
    }

}
