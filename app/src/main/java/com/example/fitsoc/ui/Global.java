package com.example.fitsoc.ui;

import android.app.Application;

public class Global extends Application {
    private static String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
