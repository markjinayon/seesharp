package com.parentalcontrol.seesharp.activities;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.firebase.FirebaseMethod;
import com.parentalcontrol.seesharp.helper.DeviceHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

    private Button signIn_signUp, signUp_signUp;
    private TextInputLayout email_signUp, password_signUp, confirmPassword_signUp, fullName_signUp;
    private RadioGroup userType_signUp;
    private ProgressBar progressBar_signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email_signUp = findViewById(R.id.email_signUp);
        password_signUp = findViewById(R.id.password_signUp);
        confirmPassword_signUp = findViewById(R.id.confirmPassword_signUp);
        fullName_signUp = findViewById(R.id.fullName_signUp);

        userType_signUp = findViewById(R.id.userType_signUp);

        progressBar_signUp = findViewById(R.id.progressBar_signUp);

        signIn_signUp = findViewById(R.id.signIn_signUp);
        signIn_signUp.setOnClickListener(view -> openSignInActivity());

        signUp_signUp = findViewById(R.id.signUp_signUp);
        signUp_signUp.setOnClickListener(view -> signUp());
    }

    public void signUp() {
        String email = email_signUp.getEditText().getText().toString().trim();
        String password = password_signUp.getEditText().getText().toString().trim();
        String confirmPassword = confirmPassword_signUp.getEditText().getText().toString().trim();
        String fullName = fullName_signUp.getEditText().getText().toString().trim();
        String userType = ((RadioButton) findViewById((userType_signUp).getCheckedRadioButtonId())).getText().toString().trim();

        if (email.isEmpty()) {
            email_signUp.setError("Enter your email!");
            email_signUp.getEditText().requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_signUp.setError("Enter a valid email!");
            email_signUp.getEditText().requestFocus();
            return;
        } else {
            email_signUp.setError("");
            email_signUp.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            password_signUp.setError("Enter your password!");
            password_signUp.getEditText().requestFocus();
            return;
        } else if (password.length() < 6) {
            password_signUp.setError("Minimum password length is 6!");
            password_signUp.getEditText().requestFocus();
            return;
        } else {
            password_signUp.setError("");
            password_signUp.setErrorEnabled(false);
        }

        if (confirmPassword.isEmpty()) {
            confirmPassword_signUp.setError("Re-enter your password!");
            confirmPassword_signUp.getEditText().requestFocus();
            return;
        } else if (!password.equals(confirmPassword)) {
            confirmPassword_signUp.setError("Mismatched password!");
            confirmPassword_signUp.getEditText().requestFocus();
            return;
        } else {
            confirmPassword_signUp.setError("");
            confirmPassword_signUp.setErrorEnabled(false);
        }

        if (fullName.isEmpty()) {
            fullName_signUp.setError("Enter your full name!");
            fullName_signUp.getEditText().requestFocus();
            return;
        } else {
            fullName_signUp.setError("");
            fullName_signUp.setErrorEnabled(false);
        }

        progressBar_signUp.setVisibility(View.VISIBLE);

        User user = new User("", email, password, fullName, userType, DeviceHelper.getDeviceName());
        FirebaseMethod.createNewUser(user,
                taskOnComplete -> {
                    if (taskOnComplete.isSuccessful()) {
                        user.accountId = FirebaseAuth.getInstance().getUid();
                        FirebaseMethod.addUserDataToRealtimeDatabase(user,
                                task1OnComplete -> {
                                    if (task1OnComplete.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "User has been registered successfully", Toast.LENGTH_LONG).show();
                                        openSignInActivity();
                                    }
                                },
                                task1OnFailure -> {
                                    Toast.makeText(SignUpActivity.this, "Failed to register user", Toast.LENGTH_LONG).show();
                                    Log.e("FirebaseDatabase Failure", task1OnFailure.toString());
                                });
                    }
                    progressBar_signUp.setVisibility(View.GONE);
                },
                taskOnFailure -> {
                    Toast.makeText(SignUpActivity.this, "Failed to register user", Toast.LENGTH_LONG).show();
                    Log.e("FirebaseAuth Failure", taskOnFailure.toString());
                    if (taskOnFailure instanceof FirebaseAuthUserCollisionException) {
                        email_signUp.setError("Email already exists!");
                        email_signUp.getEditText().requestFocus();
                    }
                });
    }

    public void openSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }



}