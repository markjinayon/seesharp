package com.parentalcontrol.seesharp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.FirebaseDatabase;
import com.parentalcontrol.seesharp.R;

import java.util.ArrayList;

public class BlockedAppsListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> blockedApps;
    private String accountId;

    FirebaseDatabase firebaseDatabase;

    static class ViewHolder {
        TextView appLabel;
        Switch appSwitch;
    }

    public BlockedAppsListAdapter(@NonNull Context context, String accountId, ArrayList<String> installedApps, ArrayList<String> blockedApps) {
        super(context, R.layout.item_list_blocked_app, installedApps);

        this.blockedApps = blockedApps;
        this.accountId = accountId;

        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_blocked_app, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.appLabel = convertView.findViewById(R.id.appLabel_blockedApp);
            viewHolder.appSwitch = convertView.findViewById(R.id.appSwitch_blockedApp);

            convertView.setTag(viewHolder);
        }

        String packageName = getItem(position);

        try {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.appLabel.setText(getContext().getPackageManager().getApplicationLabel(getContext().getPackageManager().getApplicationInfo(packageName, 0)).toString());
            viewHolder.appSwitch.setChecked(false);
            viewHolder.appSwitch.setChecked(blockedApps.contains(packageName));

            viewHolder.appSwitch.setOnCheckedChangeListener((compound, b) -> {
                System.out.println("creepy");
                if (b) {
                    blockedApps.add(packageName);
                } else {
                    while(blockedApps.remove(packageName)){}
                }

                firebaseDatabase.getReference("users")
                        .child(accountId)
                        .child("blockedApplications")
                        .setValue(blockedApps);
            });
        } catch (Exception e) {
            //e.printStackTrace();
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.appLabel.setText("Not your app");

        }


        return convertView;
    }
}
