package com.parentalcontrol.seesharp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.activities.child.ChildDashboardActivity;
import com.parentalcontrol.seesharp.activities.parent.ParentDashboardActivity;
import com.parentalcontrol.seesharp.model.User;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar_main = findViewById(R.id.progressBar_main);
        progressBar_main.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            openSignInActivity();
        } else {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                if (user.userType.equals("Parent")) {
                                    openParentDashboardActivity();
                                } else {
                                    openChildDashboardActivity();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                            Log.e("DatabaseError", error.toString());
                        }
                    });
        }
    }

    public void openSignInActivity() {
        progressBar_main.setVisibility(View.GONE);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    public void openParentDashboardActivity() {
        progressBar_main.setVisibility(View.GONE);
        startActivity(new Intent(this, ParentDashboardActivity.class));
        finish();
    }

    public void openChildDashboardActivity() {
        progressBar_main.setVisibility(View.GONE);
        startActivity(new Intent(this, ChildDashboardActivity.class));
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}