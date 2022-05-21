package com.parentalcontrol.seesharp.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.model.User;

public class FirebaseMethod {
    public static void createNewUser(User user, OnCompleteListener onCompleteListener, OnFailureListener onFailureListener) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    public static void addUserDataToRealtimeDatabase(User user, OnCompleteListener onCompleteListener, OnFailureListener onFailureListener) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(user.accountId)
                .setValue(user)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    public static void signInWithEmailAndPassword(String email, String password, OnCompleteListener onCompleteListener, OnFailureListener onFailureListener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    public static String getCurrentUserUID() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static void getUserDataFromRealtimeDatabase(String accountId, ValueEventListener valueEventListener) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(accountId)
                .addListenerForSingleValueEvent(valueEventListener);
    }

    public static boolean isThereACurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void signOutCurrentUser() {
        FirebaseAuth.getInstance().signOut();
    }

    public static void resetPassword(String email, OnCompleteListener onCompleteListener, OnFailureListener onFailureListener) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    public static void listenToUserDataFromRealtimeDatabase(String accountId, ValueEventListener valueEventListener) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(accountId)
                .addValueEventListener(valueEventListener);
    }

    public static void updateDataFieldOfUser(String accountId, String fieldName, Object data, OnCompleteListener onCompleteListener, OnFailureListener onFailureListener) {
        FirebaseDatabase.getInstance().getReference("users").child(accountId).child(fieldName).setValue(data)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }
}
