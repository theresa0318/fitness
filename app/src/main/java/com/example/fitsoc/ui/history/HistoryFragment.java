package com.example.fitsoc.ui.history;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;
import com.example.fitsoc.databinding.FragmentHistoryBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private final String TAG = "History -> data";
    private FragmentHistoryBinding binding;
    private String userID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_history, container, false);
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        userID = "abcdefg@gmail.com";
        setListeners();
        Calendar c = Calendar.getInstance();
        String date = generateDateString(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        getDataFromDatabase(date);
        return binding.getRoot();
    }

    private void setListeners() {
        binding.calendarHistory.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Log.d(TAG, String.valueOf(year));
            Log.d(TAG, String.valueOf(month));
            Log.d(TAG, String.valueOf(dayOfMonth));
            String date = generateDateString(year, month, dayOfMonth);
            getDataFromDatabase(date);
        });
    }

    private String generateDateString(int year, int month, int day) {
        String date = year + "-" + (month+1) + "-" + day;
        Log.d(TAG, date);
        return date;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(Map<String, Object> data, int rank) {
        double distance = (long)data.get("distance") / 1.0;

        if(distance < 1000){
            String distanceString = "" + (int)distance;
            binding.textDistance.setText("Distance: " +
                    distanceString + " M");
        }else{
            binding.textDistance.setText("Distance: " +
                    String.format("%.2f", distance / 1000) + " KM");
        }
        long duration = Math.round((long) data.get("duration") / 1000);
        String durationString = "" + duration;
        binding.textTime.setText("Time: " + durationString + " min");
        binding.textRank.setText("Rank: " + rank);
    }


    public void getDataFromDatabase(String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("date", date)
                .orderBy("distance", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d(TAG, "No getting documents: ", task.getException());
                        } else {
                            int rank = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                rank++;
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (document.getData().get("userID").equals(userID)) {
                                    updateUI(document.getData(), rank);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}
