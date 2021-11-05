package com.example.fitsoc.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TaskList {
    int[] distanceArray = {500, 800, 1000, 1200, 1500, 2000, 2500, 3000, 4000, 5000};
    int[] timeArray = {15, 30, 45, 60, 75, 90, 105, 120, 150, 180};
    int[] targetArray = {100, 150, 200, 250, 300, 400, 500, 800, 1000, 1500};

    public TaskList() {

    }

    public DailyTask createTodayTasks(String userID) {
        Random random = new Random();
        int simpleTaskID = random.nextInt(3);
        int midTaskID = random.nextInt(4) + 3;
        int hardTaskID = random.nextInt(3) + 7;
        int[] tasksID = {simpleTaskID, midTaskID, hardTaskID};
        List<String> list = new ArrayList<>(Arrays.asList("distance", "time", "target"));
        Collections.shuffle(list);
        ArrayList<FitTask> fitTasks = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            String type = list.get(i);
            int value;
            if (type.equals("distance")) {
                value = distanceArray[tasksID[i]];
            } else if (type.equals("time")) {
                value = timeArray[tasksID[i]];
            } else {
                value = targetArray[tasksID[i]];
            }
            FitTask fitTask = new FitTask(type, value, tasksID[i]+1);
            fitTasks.add(fitTask);
        }
        String dateString = generateDateString();
        return new DailyTask(fitTasks, dateString, userID);
    }

    private String generateDateString() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }

}
