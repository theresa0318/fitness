package com.example.fitsoc.data;

import android.location.Location;
import android.util.Log;

import com.example.fitsoc.ui.Global;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.util.Calendar;

public class RunningData {
    public String userID;
    public String date;
    public Location startLocation;
    public Location endLocation;
    public Timestamp startTime;
    public Timestamp endTime;
    public long speedAVG;
    public long distance;
    public long totalTime;

    public RunningData(String userID) {
        this.userID = userID;
        date = generateDateString();
        startLocation = null;
        endLocation = null;
        startTime = null;
        endTime = null;
        speedAVG = 0;
        distance = 0;
        totalTime = 0;
    }

    public RunningData() {
        this("abcdefg@gmail.com");
    }

    private String generateDateString() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public void setSpeedAVG(long speedAVG) {
        this.speedAVG = speedAVG;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public void writeToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("running data")
            .add(this)
            .addOnSuccessListener(documentReference ->
                    Log.d("STORE", "DocumentSnapshot added with ID: " + documentReference.getId()))
            .addOnFailureListener(e ->
                    Log.w("STORE", "Error adding document", e));
    }

}
