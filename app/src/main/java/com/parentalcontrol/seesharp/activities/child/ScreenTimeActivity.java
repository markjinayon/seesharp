package com.parentalcontrol.seesharp.activities.child;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.adapters.ScreenTimeListAdapter;
import com.parentalcontrol.seesharp.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class ScreenTimeActivity extends AppCompatActivity {

    ListView appList_screenTime;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_time);

        appList_screenTime = findViewById(R.id.appList_screenTime);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseDatabase.getReference("users")
                    .child(firebaseUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User userData = snapshot.getValue(User.class);

                            if (userData == null) return;

                            if (userData.appTimeLimits.isEmpty()) {
                                userData.appTimeLimits = initAppTimeLimits(userData.installedApplications);
                                firebaseDatabase.getReference("users")
                                        .child(firebaseUser.getUid())
                                        .setValue(userData);
                            }

                            ScreenTimeListAdapter screenTimeListAdapter = new ScreenTimeListAdapter(getApplicationContext(), userData.appTimeLimits);
                            appList_screenTime.setAdapter(screenTimeListAdapter);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

    private ArrayList<String> initAppTimeLimits(ArrayList<String> installedApps) {
        ArrayList<String> newData = new ArrayList<>();

        for (String app: installedApps) {
            newData.add(app+"::"+"None");
        }

        return newData;
    }
}