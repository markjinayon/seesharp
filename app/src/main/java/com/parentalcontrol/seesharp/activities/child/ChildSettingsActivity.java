package com.parentalcontrol.seesharp.activities.child;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;

public class ChildSettingsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;

    private Button logout_childSettings, getId_childSettings, changePin_childSettings, editProfile_childSettings, aboutUs_childSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        logout_childSettings = findViewById(R.id.logout_childSettings);
        logout_childSettings.setOnClickListener(view -> logout());

        getId_childSettings = findViewById(R.id.getId_childSettings);
        getId_childSettings.setOnClickListener(view -> getId());

        changePin_childSettings = findViewById(R.id.changePin_childSettings);
        changePin_childSettings.setOnClickListener(view -> openChangePinActivity());

        editProfile_childSettings = findViewById(R.id.editProfile_childSettings);
        editProfile_childSettings.setOnClickListener(view -> openChaneProfileActivity());

        aboutUs_childSettings = findViewById(R.id.aboutUs_childSettings);
        aboutUs_childSettings.setOnClickListener(view -> openAboutUsActivity());
    }

    private void openAboutUsActivity() {
        startActivity(new Intent(this, ChildAboutUsActivity.class));
    }

    private void openChaneProfileActivity() {
        startActivity(new Intent(this, ChildEditProfileActivity.class));
    }

    private void openChangePinActivity() {
        Intent intent = new Intent(this, PinLockscreenActivity.class);
        intent.putExtra("pin", "");
        intent.putExtra("newPin", "");
        startActivity(intent);
    }

    private void getId() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", firebaseUser.getUid());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Copied", Toast.LENGTH_LONG).show();
    }

    private void logout() {
        firebaseAuth.signOut();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}