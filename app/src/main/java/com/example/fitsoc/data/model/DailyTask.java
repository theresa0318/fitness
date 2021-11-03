package com.example.fitsoc.data.model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class DailyTask {
    public static final String TAG = "Task: --> ";
    public String userID;
    public ArrayList<FitTask> todayTasks;
    public String date;


    public DailyTask(ArrayList<FitTask> fitTasks, String date, String userID) {
        todayTasks = new ArrayList<>(fitTasks);
        this.date = date;
        this.userID = userID;
    }

    public void writeToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("dailyTasks")
                .whereEqualTo("userID", userID)
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            db.collection("dailyTasks")
                                    .add(this)
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("STORE", "DocumentSnapshot added with ID: " + documentReference.getId()))
                                    .addOnFailureListener(e ->
                                            Log.w("STORE", "Error adding document", e));
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                updateTasks(db, document);
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public FitTask findDistanceTask() {
        for (FitTask task : todayTasks) {
            if(task.type.equals("distance")) return task;
        }
        return null;
    }

    public FitTask findTimeTask() {
        for (FitTask task : todayTasks) {
            if(task.type.equals("time")) return task;
        }
        return null;
    }

    public FitTask findTargetTask() {
        for (FitTask task : todayTasks) {
            if(task.type.equals("target")) return task;
        }
        return null;
    }

    public FitTask getHardTask() {
        for (FitTask task : todayTasks) {
            if(task.level < 4) return task;
        }
        return null;
    }

    public FitTask getMidTask() {
        for (FitTask task : todayTasks) {
            if(task.level >= 4 && task.level<=7) return task;
        }
        return null;
    }
    public FitTask getSimpleTask() {
        for (FitTask task : todayTasks) {
            if(task.level >7 ) return task;
        }
        return null;
    }

    private void updateTasks(FirebaseFirestore db, QueryDocumentSnapshot document) {
        try {
            String id = document.getId();
            db.collection("dailyTasks").document(id)
                    .update("simpleTask.isAccepted", getSimpleTask().isAccepted,
                            "midTask.isAccepted", getMidTask().isAccepted,
                            "hardTask.isAccepted", getHardTask().isAccepted)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e ->
                            Log.w(TAG, "Error updating document", e));
        } catch (NullPointerException e) {
            Log.d(TAG, "Wrong Data Type");
        }
    }
}
