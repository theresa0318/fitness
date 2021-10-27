package com.example.fitsoc.data.model;

public class RegisteredUser {
    private String userId;
    private String password;

    public RegisteredUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public RegisteredUser(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

}
