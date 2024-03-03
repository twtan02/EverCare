package my.edu.utar.evercare.Statistics;

public class ViewPagerItem {

    private String profileImageUrl; // New field for profile image URL
    private String username;
    private String dateOfBirth;
    private String userId;

    public ViewPagerItem(String userId, String profileImageUrl, String username, String dateOfBirth) {
        this.userId = userId;
        this.profileImageUrl = profileImageUrl;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
    }


    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }
    public String getUserId() {
        return userId;
    }
}
