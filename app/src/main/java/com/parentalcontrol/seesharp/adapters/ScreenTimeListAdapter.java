package com.parentalcontrol.seesharp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.parentalcontrol.seesharp.R;

import java.util.ArrayList;

public class ScreenTimeListAdapter extends ArrayAdapter<String> {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;

    ArrayList<String> appTimeLimits;

    static class ViewHolder {
        public TextView appLabel;
        public ImageView appIcon;
        Spinner spinner;
    }

    public ScreenTimeListAdapter(@NonNull Context context, @NonNull ArrayList<String> arrayList) {
        super(context, R.layout.item_list_screen_time, arrayList);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        this.appTimeLimits = arrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String value = getItem(position);
        String[] appData = value.split("::");

        String packageName = appData[0];
        String timeLimit = appData[1];

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_screen_time, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.appLabel = convertView.findViewById(R.id.appLabel_screenTime);
            viewHolder.appIcon = convertView.findViewById(R.id.appIcon_screenTime);
            viewHolder.spinner = convertView.findViewById(R.id.spinner_screenTime);

            convertView.setTag(viewHolder);
        }

        try {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.appIcon.setImageDrawable(getContext().getPackageManager().getApplicationIcon(packageName));
            viewHolder.appLabel.setText(getContext().getPackageManager().getApplicationLabel(getContext().getPackageManager().getApplicationInfo(packageName, 0)).toString());
            viewHolder.spinner.setOnItemSelectedListener(null);
            selectValue(viewHolder.spinner, timeLimit);

            int selectedPosition = viewHolder.spinner.getSelectedItemPosition();
            viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (firebaseUser != null && i != selectedPosition) {
                        while (appTimeLimits.remove(value)) {}
                        appTimeLimits.add(packageName+"::"+viewHolder.spinner.getSelectedItem().toString());
                        firebaseDatabase.getReference("users")
                                .child(firebaseUser.getUid())
                                .child("appTimeLimits")
                                .setValue(appTimeLimits);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return convertView;
    }

    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
