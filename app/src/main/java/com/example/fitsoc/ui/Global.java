package com.example.fitsoc.ui;

import android.app.Application;

public class Global extends Application {
    private static String userID = null;
    private static String password = null;

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

}