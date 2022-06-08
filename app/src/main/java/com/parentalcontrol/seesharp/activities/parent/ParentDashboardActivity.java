package com.parentalcontrol.seesharp.activities.parent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.adapters.ChildDevicesListAdapter;
import com.parentalcontrol.seesharp.model.Device;
import com.parentalcontrol.seesharp.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParentDashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;

    private FloatingActionButton addChildAccountId_childDashboard;

    private ListView listDevices_parentDashboard;

    User userData;

    ArrayList<String> lastConnectedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        addChildAccountId_childDashboard = findViewById(R.id.addChildAccountId_parentDashboard);
        addChildAccountId_childDashboard.setOnClickListener(view -> openDialog());

        listDevices_parentDashboard = findViewById(R.id.listDevices_parentDashboard);

        userData = null;
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            lastConnectedDevices = new ArrayList<>();
            firebaseDatabase.getReference("users")
                    .child(firebaseUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userData = snapshot.getValue(User.class);

                            if (userData == null) return;

                            if (!userData.connectedDevices.equals(lastConnectedDevices)) {
                                firebaseDatabase.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Object values = snapshot.getValue();
                                        if (values == null) return;
                                        Map<String, Device> connectedDevicesData = new HashMap<>();
                                        for (Map.Entry<String, Object> entry: ((Map<String,Object>) values).entrySet()) {
                                            Map user = (Map) entry.getValue();
                                            if (userData.connectedDevices.contains((String) user.get("accountId"))) {
                                                connectedDevicesData.put((String) user.get("accountId"), new Device((String) user.get("profilePic"), (String) user.get("fullName"), (String) user.get("deviceName")));
                                            }
                                        }
                                        ChildDevicesListAdapter adapter = new ChildDevicesListAdapter(getApplicationContext(), userData.connectedDevices, connectedDevicesData);
                                        listDevices_parentDashboard.setAdapter(adapter);
                                        listDevices_parentDashboard.setOnItemClickListener((parent, view, position, id) -> {
                                            openDevice(userData.connectedDevices.get(position));
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    public void openDevice(String accountId) {
        Intent intent = new Intent(this, ConnectedDeviceActivity.class);
        intent.putExtra("accountId", accountId);
        startActivity(intent);
    }

    public void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add child account ID");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if (userData != null) {
                    firebaseDatabase.getReference("users").child(text).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                if (userData.connectedDevices.contains(text)) {
                                    Toast.makeText(ParentDashboardActivity.this, "Account id already exists!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                userData.connectedDevices.add(text);
                                firebaseDatabase.getReference("users")
                                        .child(firebaseUser.getUid())
                                        .child("connectedDevices")
                                        .setValue(userData.connectedDevices);
                            } else {
                                Toast.makeText(ParentDashboardActivity.this, "Account id does not exists!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ParentDashboardActivity.this, "Account id does not exists!", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}