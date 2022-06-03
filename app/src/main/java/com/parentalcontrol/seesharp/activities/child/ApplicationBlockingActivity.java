package com.parentalcontrol.seesharp.activities.child;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.adapters.AppBlockingListAdapter;
import com.parentalcontrol.seesharp.helper.DeviceHelper;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.services.accessibility.ApplicationBlockingAccessibilityService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class ApplicationBlockingActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private ConstraintLayout accessibilityEnabled_appBlocking, accessibilityDisabled_appBlocking;
    private Button enableAppBlocking_appBlocking;
    private ListView applicationList_appBlocking;

    private ArrayList<String> last_installedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_blocking);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        accessibilityEnabled_appBlocking = findViewById(R.id.accessibilityEnabled_appBlocking);
        accessibilityEnabled_appBlocking.setVisibility(View.GONE);
        accessibilityDisabled_appBlocking = findViewById(R.id.accessibilityDisabled_appBlocking);
        accessibilityDisabled_appBlocking.setVisibility(View.GONE);

        enableAppBlocking_appBlocking = findViewById(R.id.enableAppBlocking_appBlocking);
        enableAppBlocking_appBlocking.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        applicationList_appBlocking = findViewById(R.id.applicationList_appBlocking);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseDatabase.getReference("users")
                    .child(firebaseUser.getUid())
                    .child("appBlockingState")
                    .setValue(DeviceHelper.isAccessibilityServiceEnabled(getApplicationContext(), ApplicationBlockingAccessibilityService.class))
                    .addOnCompleteListener(task -> {

                    });

            last_installedApps = new ArrayList<>();
            firebaseDatabase.getReference("users")
                    .child(firebaseUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);

                            if (user == null) return;

                            if (user.appBlockingState) {
                                accessibilityEnabled_appBlocking.setVisibility(View.VISIBLE);
                                accessibilityDisabled_appBlocking.setVisibility(View.GONE);
                            } else {
                                accessibilityEnabled_appBlocking.setVisibility(View.GONE);
                                accessibilityDisabled_appBlocking.setVisibility(View.VISIBLE);
                            }

                            if (!last_installedApps.equals(user.installedApplications)) {
                                AppBlockingListAdapter adapter = new AppBlockingListAdapter(getApplicationContext(), user.installedApplications, user.blockedApplications);
                                applicationList_appBlocking.setAdapter(adapter);
                                last_installedApps = user.installedApplications;
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}