package com.parentalcontrol.seesharp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.activities.child.ChildDashboardActivity;
import com.parentalcontrol.seesharp.activities.parent.ParentDashboardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openSignInActivity();
//        if (!FirebaseMethod.isThereACurrentUser()) {
//            openSignInActivity();
//        } else {
//            FirebaseMethod.getUserDataFromRealtimeDatabase(FirebaseMethod.getCurrentUserUID(), new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    User user = snapshot.getValue(User.class);
//                    if (user != null) {
//                        if (user.userType.equals("Parent")) {
//                            openParentDashboardActivity();
//                        } else {
//                            openChildDashboardActivity();
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
//                    Log.e("DatabaseError", error.toString());
//                }
//            });
//        }
    }

    public void openSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    public void openParentDashboardActivity() {
        startActivity(new Intent(this, ParentDashboardActivity.class));
        finish();
    }

    public void openChildDashboardActivity() {
        startActivity(new Intent(this, ChildDashboardActivity.class));
        finish();
    }
}