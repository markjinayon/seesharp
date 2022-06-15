package com.parentalcontrol.seesharp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.model.Device;
import com.parentalcontrol.seesharp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChildDevicesListAdapter extends ArrayAdapter<String> {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;

    private Map<String, Device> connectedDevicesData;
    private ArrayList<String> connectedDevices;
    private ArrayList<String> activeDevices;

    static class ViewHolder {
        ImageView profilePic, activeStatus;
        TextView userName;
        TextView deviceName;
    }

    public ChildDevicesListAdapter (@NonNull Context context, @NonNull ArrayList<String> arrayList, Map<String, Device> connectedDevicesData, ArrayList<String> activeDevices) {
        super(context, R.layout.item_list_connected_devices, arrayList);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        this.connectedDevices = arrayList;
        this.connectedDevicesData = connectedDevicesData;
        this.activeDevices = activeDevices;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String accountId = getItem(position);//connectedDevices.get(position);
        Device device = connectedDevicesData.get(accountId);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_connected_devices, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.profilePic = convertView.findViewById(R.id.profilePicItem);
            viewHolder.userName = convertView.findViewById(R.id.nameItem);
            viewHolder.deviceName = convertView.findViewById(R.id.deviceNameItem);
            viewHolder.activeStatus = convertView.findViewById(R.id.activeStatus);

            convertView.setTag(viewHolder);
        }

        if (device == null) return  convertView;

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.userName.setText(device.userName);
        viewHolder.deviceName.setText(device.deviceName);
        viewHolder.activeStatus.setImageResource(R.drawable.green_circle);
        viewHolder.activeStatus.setVisibility((activeDevices.contains(accountId)) ? View.VISIBLE:View.GONE);

        if (device.profilePic.isEmpty()) {
            viewHolder.profilePic.setImageResource(R.drawable.student);
        }

        return convertView;
    }


}
