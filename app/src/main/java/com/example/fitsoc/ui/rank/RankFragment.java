package com.example.fitsoc.ui.rank;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;
import com.example.fitsoc.databinding.FragmentHistoryBinding;
import com.example.fitsoc.databinding.FragmentRankBinding;
import com.example.fitsoc.ui.Global;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class RankFragment extends Fragment {
    private final String TAG = "Rank: --> ";
    private FragmentRankBinding binding;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_rank, container, false);
        binding = FragmentRankBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        userID = ((Global)this.getActivity().getApplication()).getUserID();
        Calendar c = Calendar.getInstance();
        String date = generateDateString(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        getDataFromDatabase(date);
        return view;
    }

    private String generateDateString(int year, int month, int day) {
        String date = year + "-" + (month+1) + "-" + day;
        Log.d(TAG, date);
        return date;
    }

    private void getDataFromDatabase(String date) {
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
                                String eachID = (String) document.getData().get("userID");
                                Long eachDistance = (Long) document.getData().get("distance");
                                if (rank <4) {
                                    if (rank == 1){
                                        binding.name1.setText(eachID);
                                        binding.dist1.setText(generateDistanceString(eachDistance));
                                    } else if (rank == 2) {
                                        binding.name2.setText(eachID);
                                        binding.dist2.setText(generateDistanceString(eachDistance));
                                    } else if (rank == 3) {
                                        binding.name3.setText(eachID);
                                        binding.dist3.setText(generateDistanceString(eachDistance));
                                    }
                                }

                                if (eachID.equals(userID)) {
                                    binding.yourRank.setText("" + rank);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private String generateDistanceString(long distance) {
        if (distance <= 1000) return distance + " M";
        else return distance / 1000 + " KM";
    }
}
