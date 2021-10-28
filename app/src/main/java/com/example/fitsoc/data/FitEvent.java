package com.example.fitsoc.data;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class FitEvent {
    private final String TAG = "Event -> Store";
    private final FirebaseDatabase database;
    public String date;
    public String userID;
    public long duration;
    public long distance;

    public FitEvent(RunningData data) {
        database = FirebaseDatabase.getInstance();
        date = data.date;
        userID = data.userID;
        duration = data.totalTime;
        distance = data.distance;
    }

    public void writeToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
            .whereEqualTo("userID", userID)
            .whereEqualTo("date", date)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        createNewEvent(db);
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            updateEvent(db, document);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
    }

    private void createNewEvent(FirebaseFirestore db) {
        db.collection("events")
                .add(this)
                .addOnSuccessListener(documentReference ->
                        Log.d("STORE", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.w("STORE", "Error adding document", e));
    }

    private void updateEvent(FirebaseFirestore db, QueryDocumentSnapshot document) {
        try {
            Map<String, Object> data = document.getData();
            String id = document.getId();
            long oldDuration = (long) data.get("duration");
            long oldDistance = (long) data.get("distance");
            db.collection("events").document(id)
                    .update("duration", oldDuration + this.duration,
                            "distance", oldDistance + this.distance)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e ->
                            Log.w(TAG, "Error updating document", e));
        } catch (NullPointerException e) {
            Log.d(TAG, "Wrong Data Type");
        }
    }
}
