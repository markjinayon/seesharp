package com.parentalcontrol.seesharp.services.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Browser;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.helper.DeviceHelper;

import com.parentalcontrol.seesharp.model.User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SeeSharpAccessibilityService extends AccessibilityService {
    private static final String TAG = "SeeSharp Accessibility";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private User user;

    private boolean isActive;

    private HashMap<String, Long> previousUrlDetections;

    private HashMap<String, ArrayList<String>> detectedTexts;

    private ArrayList<String> appsToMonitorText;

    private int mDebugDepth;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e(TAG, "Service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC | AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 500;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);

        previousUrlDetections = new HashMap<>();
        user = null;
        isActive = true;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // add apps to monitor text
        appsToMonitorText = new ArrayList<>();
        appsToMonitorText.add("com.facebook.orca");
        appsToMonitorText.add("com.google.android.apps.messaging");
        appsToMonitorText.add("com.facebook.katana");

        // create empty array list every apps
        detectedTexts = new HashMap<>();
        for (String app: appsToMonitorText) {
            detectedTexts.put(app, new ArrayList<>());
        }

        if (firebaseAuth.getCurrentUser() == null) {
            disableSelf();
            Toast.makeText(getApplicationContext(), "You must sign in first!", Toast.LENGTH_LONG).show();
            return;
        }

        firebaseDatabase.getReference("users").child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user == null) return;

                if (!user.appBlockingState && isActive) {
                    firebaseDatabase.getReference("users").child(user.accountId).child("appBlockingState").setValue(true);
                    firebaseDatabase.getReference("users").child(user.accountId).child("appTimeLimitState").setValue(checkUsageAccess());
                    firebaseDatabase.getReference("users").child(user.accountId).child("webFilteringState").setValue(true);
                    firebaseDatabase.getReference("users").child(user.accountId).child("installedApplications").setValue(DeviceHelper.getListOfInstalledApps(getApplicationContext()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        });

        Toast.makeText(getApplicationContext(), "SeeSharp accessibility enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Service on interrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Service on destroy");

        if (user == null) return;

        firebaseDatabase.getReference("users").child(user.accountId).child("appBlockingState").setValue(false);
        firebaseDatabase.getReference("users").child(user.accountId).child("appTimeLimitState").setValue(false);
        firebaseDatabase.getReference("users").child(user.accountId).child("webFilteringState").setValue(false);

        isActive = false;
        Toast.makeText(getApplicationContext(), "SeeSharp accessibility disabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (firebaseAuth.getCurrentUser() == null || !isActive) {
            disableSelf();
            return;
        }

        if (accessibilityEvent.getPackageName() == null ) {
            return;
        }

        String packageName = accessibilityEvent.getPackageName().toString();

        //Log.e(packageName, AccessibilityEvent.eventTypeToString(accessibilityEvent.getEventType()));

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            monitorTexts(accessibilityEvent);
        }

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (user.appBlockingState) {
                checkAppBlocking(packageName);
            }

            if (user.appTimeLimitState) {
                checkScreenTimeLimit(packageName);
            } else {
                Toast.makeText(getApplicationContext(), "Enable SeeSharp's usage access!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
                firebaseDatabase.getReference("users").child(user.accountId).child("appTimeLimitState").setValue(checkUsageAccess());
            }
        }

        if (user.webFilteringState) {
            for (String app: packageNames()) {
                if (app.equals(packageName)) {
                    checkWebFilter(accessibilityEvent, packageName);
                }
            }
        }
    }

    private void monitorTexts(AccessibilityEvent accessibilityEvent) {
        mDebugDepth = 0;
        AccessibilityNodeInfo mNodeInfo = accessibilityEvent.getSource();
        //Log.e("Text Monitoring", "START");
        printAllViews(mNodeInfo);
        //Log.e("Text Monitoring", "END");
    }

    private void printAllViews(AccessibilityNodeInfo mNodeInfo) {
        if (mNodeInfo == null) return;

        String log ="";
        for (int i = 0; i < mDebugDepth; i++) {
            log += ".";
        }

        if (mNodeInfo.getPackageName() != null) {
            String packageName = mNodeInfo.getPackageName().toString();
            boolean shouldAdd = false;

            if (appsToMonitorText.contains(packageName) && mNodeInfo.getText() != null) {
                String text = mNodeInfo.getText().toString();

                if (packageName.equals("com.facebook.orca") && log.contains("...") && !(text.contains("PM") || text.contains("AM"))) {
                    shouldAdd = true;
                } else if (packageName.equals("com.facebook.katana") && log.equals("")
                    && !text.contains("reactions") && !text.contains("comments") && !text.contains("shares")
                    && !text.contains("Reply") && !text.contains("Like")) {
                    shouldAdd = true;
                } else if (packageName.equals("com.google.android.apps.messaging")
                        && mNodeInfo.getViewIdResourceName() != null
                        && mNodeInfo.getViewIdResourceName().equals("com.google.android.apps.messaging:id/compose_message_text")) {
                    shouldAdd = true;
                }

                if (shouldAdd && !detectedTexts.get(packageName).contains(text)) {
                    detectedTexts.get(packageName).add(text);
                    Log.e(packageName, text);
                }

            }
        }


//        log+="("+mNodeInfo.getText() +" <-- "+ mNodeInfo.getViewIdResourceName()+ "<--" + mNodeInfo.getPackageName() + ")";
//        Log.d(TAG, log);

        if (mNodeInfo.getChildCount() < 1) return;
        mDebugDepth++;

        for (int i = 0; i < mNodeInfo.getChildCount(); i++) {
            printAllViews(mNodeInfo.getChild(i));
        }
        mDebugDepth--;
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
        String redirectUrl = "https://www.google.com/";
        String[] keywords = {"sex", "porn", "xxx", "hentai", "xvid", "nsfw", "onlyfans"};

        for (String word: keywords) {
            if (capturedUrl.contains(word) && (capturedUrl.contains(".net") || capturedUrl.contains(".com"))) {
                addToNotifications("Website Filtering", "Tried to access " + capturedUrl);
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
        browsers.add(new SupportedBrowserConfig("com.mozilla.firefox", "org.mozilla.firefox:id/url_bar_title")); //di sure
        return browsers;
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

    private void checkAppBlocking(String packageName) {
        if (isOnBlockedApps(packageName)) {
            addToNotifications("Application Blocking", "Tried to open " + packageName);
            takeToHomeScreen();
            Toast.makeText(SeeSharpAccessibilityService.this, "You tried to open a blocked application!", Toast.LENGTH_LONG).show();
        }
    }

    private void checkScreenTimeLimit(String packageName) {
        String timeLimit = hasTimeLimit(packageName);

        if (timeLimit.isEmpty()) return;

        long appTimeLimit = Integer.parseInt(timeLimit) * 3600000L;
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  System.currentTimeMillis() - 1000*3600*24,  System.currentTimeMillis());

        for (UsageStats app: appList) {
            if (app.getPackageName().equals(packageName) && app.getTotalTimeInForeground() >= appTimeLimit) {
                takeToHomeScreen();
                Toast.makeText(SeeSharpAccessibilityService.this, "Application already reached its time limit!", Toast.LENGTH_LONG).show();
                addToNotifications("Screen Application Time Limit", packageName+" reached its time limit");
            }
        }
    }

    private boolean isOnBlockedApps(String packageName) {
        return user != null && user.blockedApplications.contains(packageName);
    }

    private String hasTimeLimit(String packageName) {
        if (user == null) return "";

        for (String app: user.appTimeLimits) {
            String[] data = app.split("::");
            if (data[0].equals(packageName) && !data[1].equals("None")) return data[1].replace(" hrs", "");
        }

        return "";
    }

    private void takeToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void addToNotifications(String activity, String message) {
        if (firebaseAuth.getCurrentUser() == null) return;

        ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Asia/Manila"));
        firebaseDatabase.getReference("reports")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child(zdt.toString().replaceAll("\\.", ",").replaceAll("\\[", "{").replaceAll("]", "}").replaceAll("/", "|"))
                .setValue(activity+"::"+message);
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
