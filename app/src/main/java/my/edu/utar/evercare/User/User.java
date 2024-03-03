package my.edu.utar.evercare.User;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String username;
    private String profileImageUrl;
    private int age;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String username, String profileImageUrl, int age) {
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.age = age;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl != null ? profileImageUrl : "";
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
