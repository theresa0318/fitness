package com.example.fitsoc.data.model;

public class FitTask {
    public String type;
    public int value;
    public boolean isCompleted;
    public boolean isAccepted;
    public int level;

    public FitTask(String type, int value, int level) {
        this.type = type;
        this.value = value;
        isCompleted = false;
        isAccepted = false;
        this.level = level;
    }

    public FitTask(String type, long value, long level, boolean isCompleted, boolean isAccepted) {
        this.type = type;
        this.value = new Long(value).intValue();
        this.isCompleted = isCompleted;
        this.level = new Long(level).intValue();
        this.isAccepted = isAccepted;
    }

    public String toTextString() {
        if (type.equals("distance")) {
            if (value < 1000) {
                return "Distance: " + value + " M";
            } else {
                return "Distance: " + (float) value/1000 + " KM";
            }
        } else if (type.equals("time")) {
            return "Running Time: " + value + " Minutes";
        }else {
            return "Get to the target point!";
        }
    }
}
