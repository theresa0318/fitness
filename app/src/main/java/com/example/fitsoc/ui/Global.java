package com.example.fitsoc.ui;

import android.app.Application;

public class Global extends Application {
    private static String userID = null;

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String currentUserID) {
        userID = currentUserID;
    }
}
