package com.example.fitsoc.ui;

import android.app.Application;

import com.example.fitsoc.data.model.DailyTask;

public class Global extends Application {
    private static String userID = null;
    private static String password = null;
    private static DailyTask dailyTask = null;
    private static int fitPoint = 0;

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

    public static int getFitPoint() {
        return fitPoint;
    }

    public static void setFitPoint(int fitPoint) {
        Global.fitPoint = fitPoint;
    }

    public static DailyTask getDailyTask() {
        return dailyTask;
    }

    public static void setDailyTask(DailyTask inputDailyTask) {
        dailyTask = inputDailyTask;
    }

}
