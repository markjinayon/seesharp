package com.parentalcontrol.seesharp.adapters;

import android.content.Context;
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
        }

        Switch appSwitch = convertView.findViewById(R.id.appSwitch_appBlocking);
        ImageView appIcon = convertView.findViewById(R.id.appIcon_appBlocking);
        TextView appLabel = convertView.findViewById(R.id.appLabel_appBlocking);

        try {
            appIcon.setImageDrawable(getContext().getPackageManager().getApplicationIcon(packageName));
            appLabel.setText(getContext().getPackageManager().getApplicationLabel(getContext().getPackageManager().getApplicationInfo(packageName, 0)).toString());
            appSwitch.setChecked(blockedApplications.contains(packageName));

            appSwitch.setOnCheckedChangeListener((compound, b) -> {
                System.out.println("na click syaaa");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  convertView;
    }
}
