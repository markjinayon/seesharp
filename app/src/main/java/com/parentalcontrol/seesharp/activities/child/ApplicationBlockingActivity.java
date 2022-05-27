package com.parentalcontrol.seesharp.activities.child;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class ApplicationBlockingActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private ConstraintLayout accessibilityEnabled_appBlocking, accessibilityDisabled_appBlocking;
    private Button enableAppBlocking_appBlocking;
    private LinearLayout applicationList_appBlocking;


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

        dataListener();
    }

    public void dataListener() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(ApplicationBlockingActivity.this, "Unable to retrieved user data! No user found.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

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

                        displayApps(user);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void displayApps(User user) {
        applicationList_appBlocking.removeAllViews();
        for (String packageName: user.installedApplications) {
            try {
                @SuppressLint("UseSwitchCompatOrMaterialCode") Switch appSwitch = new Switch(getApplicationContext());
                Drawable icon = getPackageManager().getApplicationIcon(packageName);
                String appLabel = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(packageName, 0)).toString();

                appSwitch.setText(appLabel);
                appSwitch.setCompoundDrawables(icon, null, null, null);
                appSwitch.setChecked(user.blockedApplications.contains(packageName));

                applicationList_appBlocking.addView(appSwitch);

                appSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (b) {
                        user.blockedApplications.add(packageName);
                    } else {
                        while (user.blockedApplications.remove(packageName)) {}
                    }

                    firebaseDatabase.getReference("users")
                            .child(user.accountId)
                            .child("blockedApplications")
                            .setValue(user.blockedApplications);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}