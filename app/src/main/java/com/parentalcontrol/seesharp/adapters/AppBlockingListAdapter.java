package com.parentalcontrol.seesharp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.model.App;
import com.parentalcontrol.seesharp.model.User;

import java.util.ArrayList;

public class AppBlockingListAdapter extends ArrayAdapter<String> {
    ArrayList<String> blockedApplications;
    public AppBlockingListAdapter(@NonNull Context context, ArrayList<String> arrayList, ArrayList<String> blockedApplications) {
        super(context, R.layout.item_list_app_blocking, arrayList);
        this.blockedApplications = blockedApplications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String packageName = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_app_blocking, parent, false);
        }


        Switch appSwitch = convertView.findViewById(R.id.appSwitch);
        ImageView appIcon = convertView.findViewById(R.id.appIcon);


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        try {
            appIcon.setImageDrawable(getContext().getPackageManager().getApplicationIcon(packageName));
            appSwitch.setText(getContext().getPackageManager().getApplicationLabel(getContext().getPackageManager().getApplicationInfo(packageName, 0)).toString());
            appSwitch.setChecked(blockedApplications.contains(packageName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        appSwitch.setOnCheckedChangeListener((compound, b) -> {
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
