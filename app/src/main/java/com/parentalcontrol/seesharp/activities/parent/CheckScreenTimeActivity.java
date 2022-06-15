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
import com.parentalcontrol.seesharp.adapters.ScreenTimeListAdapter;
import com.parentalcontrol.seesharp.model.User;

import java.util.ArrayList;

public class CheckScreenTimeActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;

    private ListView appList_checkScreenTime;

    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_screentime);

        firebaseDatabase = FirebaseDatabase.getInstance();

        appList_checkScreenTime = findViewById(R.id.appList_checkScreenTime);

        accountId = getIntent().getExtras().get("accountId").toString();

        firebaseDatabase.getReference("users")
                .child(accountId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user == null) return;

                        ScreenTimeListAdapter adapter = new ScreenTimeListAdapter(getApplicationContext(), user.appTimeLimits);
                        appList_checkScreenTime.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}