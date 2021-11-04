package com.example.fitsoc.ui.task;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;
import com.example.fitsoc.data.model.DailyTask;
import com.example.fitsoc.data.model.FitTask;
import com.example.fitsoc.data.model.TaskList;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TaskFragment extends Fragment {
    private CheckBox easyButton;
    private CheckBox mediumButton;
    private CheckBox hardButton;
    private Button acceptBtn;
    private DailyTask dailyTask;
    private AlertDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("Get Your Task Today!")
                .setTitle("Task");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              updateUI();
            }
        });
        dialog = builder.create();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = "rua";
        db.collection("dailyTasks")
                .whereEqualTo("userID", userID)
                .whereEqualTo("date", generateDateString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            dailyTask = new TaskList().createTodayTasks(userID);
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                Map<String, Object> data = document.getData();
                                generateDailyTask(data);
                            }
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void generateDailyTask(Map<String, Object> data) {
        try {
            String dateString = (String) data.get("date");
            String userIDString = (String) data.get("userID");
            ArrayList<FitTask> fitTasks = new ArrayList<>();
            if ((HashMap<String, Object>) data.get("simpleTask") != null) {
                HashMap<String, Object> simpleTaskMap = (HashMap<String, Object>) data.get("simpleTask");
                String simpleType = (String) simpleTaskMap.get("type");
                long simpleValue = (long) simpleTaskMap.get("value");
                long simpleLevel = (long) simpleTaskMap.get("level");
                boolean simpleIsCompleted = (boolean) simpleTaskMap.get("isCompleted");
                boolean simpleIsAccepted = (boolean) simpleTaskMap.get("isAccepted");
                FitTask simpleTask = new FitTask(simpleType, simpleValue, simpleLevel, simpleIsCompleted, simpleIsAccepted);
                fitTasks.add(simpleTask);
            }
            if ((HashMap<String, Object>) data.get("midTask") != null) {
                HashMap<String, Object> midTaskMap = (HashMap<String, Object>) data.get("midTask");
                String midType = (String) midTaskMap.get("type");
                long midValue = (long) midTaskMap.get("value");
                long midLevel = (long) midTaskMap.get("level");
                boolean midIsCompleted = (boolean) midTaskMap.get("isCompleted");
                boolean midIsAccepted = (boolean) midTaskMap.get("isAccepted");
                FitTask midTask = new FitTask(midType, midValue, midLevel, midIsCompleted, midIsAccepted);
                fitTasks.add(midTask);
            }
            if ((HashMap<String, Object>) data.get("hardTask") != null) {
                HashMap<String, Object> hardTaskMap = (HashMap<String, Object>) data.get("hardTask");
                String hardType = (String) hardTaskMap.get("type");
                long hardValue = (long) hardTaskMap.get("value");
                long hardLevel = (long) hardTaskMap.get("level");
                boolean hardIsCompleted = (boolean) hardTaskMap.get("isCompleted");
                boolean hardIsAccepted = (boolean) hardTaskMap.get("isAccepted");
                FitTask hardTask = new FitTask(hardType, hardValue, hardLevel, hardIsCompleted,hardIsAccepted);
                fitTasks.add(hardTask);
            }
            dailyTask = new DailyTask(fitTasks, dateString, userIDString);
        } catch (NullPointerException e) {
            Log.d("Error: ", e.getMessage());
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task, container, false);

        CardView cardView1 = (CardView) view.findViewById(R.id.taskCard1);
        cardView1.setRadius(20);
        cardView1.setCardElevation(20);
        cardView1.setContentPadding(10,10,10,10);

        CardView cardView2 = (CardView) view.findViewById(R.id.taskCard2);
        cardView2.setRadius(20);
        cardView2.setCardElevation(20);
        cardView2.setContentPadding(10,10,10,10);

        CardView cardView3 = (CardView) view.findViewById(R.id.taskCard3);
        cardView3.setRadius(20);
        cardView3.setCardElevation(20);
        cardView3.setContentPadding(10,10,10,10);

        easyButton = (CheckBox) view.findViewById(R.id.easyButton);
        mediumButton = (CheckBox) view.findViewById(R.id.mediumButton);
        hardButton = (CheckBox) view.findViewById(R.id.hardButton);
        acceptBtn = view.findViewById(R.id.settings_save);

        acceptBtn.setOnClickListener(v -> {
            if (easyButton.isChecked()) {
                dailyTask.getSimpleTask().isAccepted = true;
            }
            if (mediumButton.isChecked()) {
                dailyTask.getMidTask().isAccepted = true;
            }
            if (hardButton.isChecked()) {
                dailyTask.getHardTask().isAccepted = true;
            }
            dailyTask.writeToDatabase();
        });

        dialog.show();
        // Inflate the layout for this fragment
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        View view = requireView();
        TextView hardText = (TextView) view.findViewById(R.id.hardTask);
        hardText.setText(dailyTask.getHardTask().toTextString() + "   Bonus: 5 point");
        TextView hardStatus = (TextView) view.findViewById(R.id.hardTaskStatus);
        if (dailyTask.getHardTask().isCompleted) {
            hardStatus.setText("Result: finished");
        } else {
            hardStatus.setText("Result: Unfinished");
        }

        TextView midText = (TextView) view.findViewById(R.id.midTask);
        midText.setText(dailyTask.getMidTask().toTextString() + "   Bonus: 3 point");
        TextView midStatus = (TextView) view.findViewById(R.id.midTaskStatus);
        if (dailyTask.getMidTask().isCompleted) {
            midStatus.setText("Result: finished");
        } else {
            midStatus.setText("Result: Unfinished");
        }

        TextView simpleText = (TextView) view.findViewById(R.id.simpleTask);
        simpleText.setText(dailyTask.getSimpleTask().toTextString() + "   Bonus: 1 point");
        TextView simpleStatus = (TextView) view.findViewById(R.id.simpleTaskStatus);
        if (dailyTask.getSimpleTask().isCompleted) {
            simpleStatus.setText("Result: finished");
        } else {
            simpleStatus.setText("Result: Unfinished");
        }

    }

    private String generateDateString() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }

}
