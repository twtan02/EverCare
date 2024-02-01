package my.edu.utar.evercare;

import java.io.Serializable;

public class CaregiverUser {
    private String userId;
    private String username;
    private String profileImageUrl;
    private int age; // Add age field

    public CaregiverUser() {
        // Default constructor required for Firebase
    }

    public CaregiverUser(String userId, String username, String profileImageUrl, int age) {
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.age = age;
    }

    // Getter and setter methods for all fields (userId, username, profileImageUrl, and age)

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
        if (profileImageUrl != null) {
            return profileImageUrl;
        } else {
            return "";
        }
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
