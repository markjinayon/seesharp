package com.parentalcontrol.seesharp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.parentalcontrol.seesharp.R;

import java.util.ArrayList;

public class AppBlockingListAdapter extends ArrayAdapter<String> {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;

    ArrayList<String> blockedApplications;

    static class ViewHolder {
        ImageView appIcon;
        TextView appLabel, appPackageName;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch appSwitch;
    }

    public AppBlockingListAdapter(@NonNull Context context, ArrayList<String> arrayList, ArrayList<String> blockedApplications) {
        super(context, R.layout.item_list_app_blocking, arrayList);
        this.blockedApplications = blockedApplications;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String packageName = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_app_blocking, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.appSwitch = convertView.findViewById(R.id.appSwitch_appBlocking);
            viewHolder.appIcon = convertView.findViewById(R.id.appIcon_appBlocking);
            viewHolder.appLabel = convertView.findViewById(R.id.appLabel_appBlocking);
            viewHolder.appPackageName = convertView.findViewById(R.id.appPackageName_appBlocking);

            convertView.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        Drawable appIcon = null;
        String appLabel = null;

        try {
            appIcon = getContext().getPackageManager().getApplicationIcon(packageName);
            appLabel = getContext().getPackageManager().getApplicationLabel(getContext().getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            appLabel = "Not installed";
        }

        viewHolder.appIcon.setImageDrawable(appIcon);
        viewHolder.appLabel.setText(appLabel);
        viewHolder.appPackageName.setText(packageName);

        viewHolder.appSwitch.setOnCheckedChangeListener(null);
        viewHolder.appSwitch.setChecked(blockedApplications.contains(packageName));

        viewHolder.appSwitch.setOnCheckedChangeListener((compound, b) -> {
            System.out.println("clicked");
            if (b) {
                blockedApplications.add(packageName);
            } else {
                while(blockedApplications.remove(packageName)){}
            }

            firebaseDatabase.getReference("users")
                    .child(firebaseUser.getUid())
                    .child("blockedApplications")
                    .setValue(blockedApplications);
        });

        return  convertView;
    }
}
