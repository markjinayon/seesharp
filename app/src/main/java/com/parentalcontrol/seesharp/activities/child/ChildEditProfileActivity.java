package com.parentalcontrol.seesharp.activities.child;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.SignInActivity;
import com.parentalcontrol.seesharp.model.User;

public class ChildEditProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;

    private TextInputLayout fullName_childEditProfile;
    private ImageView profilePic_childEditProfile;
    private Button save_childEditProfile;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_edit_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        fullName_childEditProfile = findViewById(R.id.fullName_childEditProfile);

        profilePic_childEditProfile = findViewById(R.id.profilePic_childEditProfile);

        save_childEditProfile = findViewById(R.id.save_childEditProfile);
        save_childEditProfile.setOnClickListener(view -> saveProfile());

        if (firebaseAuth.getUid() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }

        firebaseDatabase.getReference("users")
                .child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        if (user == null) {
                            return;
                        }

                        fullName_childEditProfile.getEditText().setText(user.fullName);

                        if (user.profilePic.equals("")) {
                            profilePic_childEditProfile.setImageResource(R.drawable.student);
                        } else {
//                            firebaseStorage.getReferenceFromUrl(user.profilePic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    profilePic_childEditProfile.setImageURI(uri);
//                                }
//                            });
                            profilePic_childEditProfile.setImageURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/seesharp-5913f.appspot.com/o/Untitled-1.jpg?alt=media&token=f64451b0-899a-4027-8920-3afe64792ac6"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void saveProfile() {
        firebaseDatabase.getReference("users")
                .child(firebaseAuth.getUid())
                .child("fullName")
                .setValue(fullName_childEditProfile.getEditText().getText().toString())
                .addOnSuccessListener(task -> {
                    Toast.makeText(getApplicationContext(), "Profile successfully updated.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}