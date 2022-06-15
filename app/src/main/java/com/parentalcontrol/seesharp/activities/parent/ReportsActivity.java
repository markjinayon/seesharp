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
import com.parentalcontrol.seesharp.adapters.ReportListAdapter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private String accountId;

    private ListView reportList_reports;

    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        accountId = getIntent().getExtras().get("accountId").toString();

        reportList_reports = findViewById(R.id.reportList_reports);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference("reports")
                .child(accountId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Object reports = snapshot.getValue();
                        if (reports == null) {
                            return;
                        }

                        ArrayList<String> listOfReport = new ArrayList<>();
                        for (Map.Entry<String, Object> entry: ((Map<String, Object>) reports).entrySet()) {
                            String[] temp = entry.toString().split("=");
                            listOfReport.add(
                                    temp[0].replaceAll(",", ".").replaceAll("\\{", "[").replaceAll("\\}", "]").replaceAll("\\|", "/")
                                            +"="+temp[1]
                            );
                        }
                        listOfReport = sort(listOfReport);

                        ReportListAdapter adapter = new ReportListAdapter(getApplicationContext(), listOfReport);
                        reportList_reports.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private ArrayList<String> sort(ArrayList<String> reports) {
        for (int i = 0; i < reports.size(); i++) {
            for (int j = i+1; j < reports.size(); j++) {
                ZonedDateTime i_val = ZonedDateTime.parse(reports.get(i).split("=")[0]);
                ZonedDateTime j_val = ZonedDateTime.parse(reports.get(j).split("=")[0]);

                if (j_val.isAfter(i_val)) {
                    String temp = reports.get(i);
                    reports.set(i, reports.get(j));
                    reports.set(j, temp);
                }
            }
        }
        return reports;
    }
}