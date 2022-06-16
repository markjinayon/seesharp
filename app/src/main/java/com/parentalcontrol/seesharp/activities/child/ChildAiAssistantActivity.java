package com.parentalcontrol.seesharp.activities.child;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Log;
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

public class ChildAiAssistantActivity extends AppCompatActivity {

    private ListView predictionList;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_ai_assistant);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        predictionList = findViewById(R.id.listPredictions_childDashboard);

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        firebaseDatabase.getReference("predictions").child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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