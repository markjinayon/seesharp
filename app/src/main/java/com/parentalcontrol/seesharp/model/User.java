package com.parentalcontrol.seesharp.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class User {
    public String accountId, email, password, fullName, userType, deviceName;

    public boolean appBlockingState;
    public ArrayList<String> blockedApplications;
    public ArrayList<String> installedApplications;

    public User() {
        this.blockedApplications = new ArrayList<>();
        this.installedApplications = new ArrayList<>();
    }

    public User(String accountId, String email, String password, String fullName, String userType, String deviceName) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.userType = userType;
        this.deviceName = deviceName;
        this.appBlockingState = false;
        this.blockedApplications = new ArrayList<>();
        this.installedApplications = new ArrayList<>();
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "accountId='" + accountId + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType='" + userType + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
