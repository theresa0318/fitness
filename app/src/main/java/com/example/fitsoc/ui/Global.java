package com.example.fitsoc.ui;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;

public class Global extends Application {
    private static String userID = null;
    private static String password = null;
    private static FirebaseUser user;

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String currentUserID) {
        userID = currentUserID;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Global.password = password;
    }

    public static FirebaseUser getUser() {
        return user;
    }

    public static void setUser(FirebaseUser user) {
        Global.user = user;
    }

}