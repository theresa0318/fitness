package com.example.fitsoc.data.model;

public class RegisteredUser {
    private String userId;
    //private String password;
    private String gender;
    private int age;
    private int height;
    private int weight;
    private String imageUrl;

    public RegisteredUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public RegisteredUser(String userId) {
        this.userId = userId;
        this.gender = null;
        this.imageUrl= "@drawable/profile_icon";
    }

    public String getUserId() {
        return userId;
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public String getImageUrl() { return imageUrl; }

    public void setGender(String gender) { this.gender = gender;}

    public void setAge(int age) { this.age = age;}

    public void setHeight(int height) { this.height = height;}

    public void setWeight(int weight) { this.weight = weight;}

    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }

}
