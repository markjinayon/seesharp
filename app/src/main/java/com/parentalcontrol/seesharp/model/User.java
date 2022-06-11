package com.parentalcontrol.seesharp.model;

import androidx.annotation.NonNull;

import com.parentalcontrol.seesharp.helper.StringHelper;

import java.util.ArrayList;

public class User {
    public String accountId, email, password, fullName, userType, deviceName, profilePic;

    public ArrayList<String> connectedDevices;

    public ArrayList<String> installedApplications;

    public boolean appBlockingState;
    public ArrayList<String> blockedApplications;

    public ArrayList<String> appTimeLimits;
    public boolean appTimeLimitState;

    public String pin;

    public boolean webFilteringState;

    public User() {
        this.installedApplications = new ArrayList<>();

        this.appBlockingState = false;
        this.blockedApplications = new ArrayList<>();

        this.appTimeLimitState = false;
        this.appTimeLimits = new ArrayList<>();

        this.connectedDevices = new ArrayList<>();

        this.pin = "1234";

        this.webFilteringState = false;
    }

    public User(String accountId, String email, String password, String fullName, String userType, String deviceName) {
        this.profilePic = "";
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.userType = userType;
        this.deviceName = deviceName;
        this.appBlockingState = false;
        this.appTimeLimitState = false;
        this.blockedApplications = new ArrayList<>();
        this.installedApplications = new ArrayList<>();
        this.appTimeLimits = new ArrayList<>();
        this.connectedDevices = new ArrayList<>();
        this.pin = StringHelper.generatePin();
        this.webFilteringState = false;
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
