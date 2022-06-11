package com.parentalcontrol.seesharp.services.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.safetynet.SafeBrowsingThreat;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class SeeSharpAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private User user;

    private HashMap<String, Long> previousUrlDetections = new HashMap<>();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        //info.packageNames = packageNames();
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC | AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 500;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);

        try {
            Tasks.await(SafetyNet.getClient(this).initSafeBrowsing());
        } catch (Exception e) {
            e.printStackTrace();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        user = null;

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            stopSelf();
            return;
        }

        firebaseDatabase.getReference("users")
                .child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        if (user == null) return;
                        if (!user.appBlockingState) {
                            changeAppBlockingStatus(true, "Accessibility service is now enabled");
                            changeAppTimeLimitStatus(true, "");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private String captureUrl(AccessibilityNodeInfo info, SupportedBrowserConfig config) {
        List<AccessibilityNodeInfo> nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId);
        if (nodes == null || nodes.size() <= 0) {
            return null;
        }

        AccessibilityNodeInfo addressBarNodeInfo = nodes.get(0);
        String url = null;
        if (addressBarNodeInfo.getText() != null) {
            url = addressBarNodeInfo.getText().toString();
        }
        addressBarNodeInfo.recycle();
        return url;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getPackageName() == null || user == null) return;

        String packageName = accessibilityEvent.getPackageName().toString();

        if (!user.installedApplications.contains(packageName)) return;

        if (user.appBlockingState) {
            checkAppBlocking(packageName);
        }
        if (user.appTimeLimitState) {
            checkScreenTimeLimit(packageName);
        }

        for (String app: packageNames()) {
            if (app.equals(packageName)) {
                checkWebFilter(accessibilityEvent, packageName);
            }
        }


        Log.e(TAG, "onAccessibilityEvent: " + packageName);

    }

    private void checkWebFilter(AccessibilityEvent accessibilityEvent, String packageName) {
        AccessibilityNodeInfo parentNodeInfo = accessibilityEvent.getSource();
        if (parentNodeInfo == null) {
            return;
        }

        SupportedBrowserConfig browserConfig = null;
        for (SupportedBrowserConfig supportedBrowserConfig: getSupportedBrowsers()) {
            if (supportedBrowserConfig.packageName.equals(packageName)) {
                browserConfig = supportedBrowserConfig;
            }
        }

        if (browserConfig == null) {
            return;
        }

        String capturedUrl = captureUrl(parentNodeInfo, browserConfig);
        parentNodeInfo.recycle();

        if (capturedUrl != null) {
            long evenTime = accessibilityEvent.getEventTime();
            String detectionId = packageName + ", and url " + capturedUrl;

            long lastRecordedTime = previousUrlDetections.containsKey(detectionId) ? previousUrlDetections.get(detectionId):0;
            if (evenTime - lastRecordedTime > 2000) {
                previousUrlDetections.put(detectionId, evenTime);
                analyzeCapturedUrl(capturedUrl, browserConfig.packageName);
            }
        }
    }

    private void analyzeCapturedUrl(String capturedUrl, String browserPackage) {
        String redirectUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String[] keywords = {"sex", "porn", "xxx", "hentai", "xvid", "nsfw"};

        for (String word: keywords) {
            if (capturedUrl.contains(word) && (capturedUrl.contains(".net") || capturedUrl.contains(".com"))) {
                performRedirect(redirectUrl, browserPackage);
            }
        }
    }

    private void performRedirect(String redirectUrl, String browserPackage) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
            intent.setPackage(browserPackage);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserPackage);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
            startActivity(intent);
        }
    }

    private static String[] packageNames() {
        List<String> packageNames = new ArrayList<>();
        for (SupportedBrowserConfig config: getSupportedBrowsers()) {
            packageNames.add(config.packageName);
        }
        return packageNames.toArray(new String[0]);
    }

    private static class SupportedBrowserConfig {
        public String packageName, addressBarId;
        public SupportedBrowserConfig(String packageName, String addressBarId) {
            this.packageName = packageName;
            this.addressBarId = addressBarId;
        }
    }

    private static List<SupportedBrowserConfig> getSupportedBrowsers() {
        List<SupportedBrowserConfig> browsers = new ArrayList<>();
        browsers.add(new SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"));
        browsers.add(new SupportedBrowserConfig("com.mozilla.firefox", "org.mozilla.firefox:id/url_bar_title"));
        return browsers;
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong!");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        changeAppBlockingStatus(false, "Accessibility service is now disabled");
    }

    public void checkAppBlocking(String packageName) {
        if (isOnBlockedApps(packageName)) {
            takeToHomeScreen();
            Toast.makeText(SeeSharpAccessibilityService.this, "You tried to open a blocked application!", Toast.LENGTH_LONG).show();
        }
    }

    public void checkScreenTimeLimit(String packageName) {
        String timeLimit = hasTimeLimit(packageName);

        if (timeLimit.isEmpty()) return;

        long appTimeLimit = Integer.parseInt(timeLimit) * 3600000L;
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  System.currentTimeMillis() - 1000*3600*24,  System.currentTimeMillis());

        for (UsageStats app: appList) {
            if (app.getPackageName().equals(packageName)) System.out.println(app.getTotalTimeInForeground());
            if (app.getPackageName().equals(packageName) && app.getTotalTimeInForeground() >= appTimeLimit) {
                takeToHomeScreen();
                Toast.makeText(SeeSharpAccessibilityService.this, "Application already reached its time limit!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void changeAppBlockingStatus(boolean state, String message) {
        firebaseDatabase.getReference("users")
                .child(user.accountId)
                .child("appBlockingState")
                .setValue(state)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SeeSharpAccessibilityService.this, message, Toast.LENGTH_LONG).show();
                        updateInstalledApplications();
                    }
                });
    }

    public void changeAppTimeLimitStatus(boolean state, String message) {
        firebaseDatabase.getReference("users")
                .child(user.accountId)
                .child("appTimeLimitState")
                .setValue(state)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SeeSharpAccessibilityService.this, message, Toast.LENGTH_LONG).show();
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

    public boolean isOnBlockedApps(String packageName) {
        return user != null && user.blockedApplications.contains(packageName);
    }

    public String hasTimeLimit(String packageName) {
        if (user == null) return "";

        for (String app: user.appTimeLimits) {
            String[] data = app.split("::");
            if (data[0].equals(packageName) && !data[1].equals("None")) return data[1].replace(" hrs", "");
        }

        return "";
    }

    public void takeToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
