package com.parentalcontrol.seesharp.activities.parent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.adapters.PredictionListAdapter;

import java.util.ArrayList;
import java.util.Map;

public class ParentAiAssistantActivity extends AppCompatActivity {

    private String accountId;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private ListView predictionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_ai_assistant);

        accountId = getIntent().getExtras().get("accountId").toString();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        predictionList = findViewById(R.id.predictionList_aiAssistant);

        firebaseDatabase.getReference("predictions").child(accountId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object obj = snapshot.getValue();
                if (obj == null) {
                    return;
                }

                ArrayList<String> predictions = new ArrayList<>();
                for (Map.Entry<String, String> entry: ((Map<String, String>) obj).entrySet()) {
                    predictions.add(entry.toString());
                }

                PredictionListAdapter adapter = new PredictionListAdapter(getApplicationContext(), predictions);
                predictionList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}