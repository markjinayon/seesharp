package com.parentalcontrol.seesharp.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class App {
    public Context context;
    public String label, packageName;
    public Drawable icon;

    public App(Context context, String packageName) {
        this.context = context;
        this.packageName = packageName;
        try {
            this.icon = context.getPackageManager().getApplicationIcon(this.packageName);
            this.label = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
