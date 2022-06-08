package com.parentalcontrol.seesharp.activities.parent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.adapters.BlockedAppsListAdapter;
import com.parentalcontrol.seesharp.model.User;

import java.util.ArrayList;

public class BlockedApplicationsActivity extends AppCompatActivity {

    private ListView appList_blockedApps;

    private FirebaseDatabase firebaseDatabase;

    private User connectedUserData;

    private ArrayList<String> lastInstalledApps, lastBlockedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_applications);

        firebaseDatabase = FirebaseDatabase.getInstance();

        appList_blockedApps = findViewById(R.id.appList_blockedApps);

        String accountId = getIntent().getExtras().get("accountId").toString();
        lastInstalledApps = lastBlockedApps = new ArrayList<>();
        firebaseDatabase.getReference("users").child(accountId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connectedUserData = snapshot.getValue(User.class);

                if (connectedUserData == null) return;

                if (!lastInstalledApps.equals(connectedUserData.installedApplications) || !lastBlockedApps.equals(connectedUserData.blockedApplications) ) {
                    BlockedAppsListAdapter adapter = new BlockedAppsListAdapter(getApplicationContext(), connectedUserData.accountId, connectedUserData.installedApplications, connectedUserData.blockedApplications);
                    appList_blockedApps.setAdapter(adapter);
                    lastInstalledApps = connectedUserData.installedApplications;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}