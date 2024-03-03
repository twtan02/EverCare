package my.edu.utar.evercare.PillReminder;

public class PillReminder {
    private String documentId;
    private String pillName;
    private int dosage;
    private String frequency;
    private String reminderDate;
    private String reminderTime;
    private String elderlyUser;

    public PillReminder() {
        // Required empty constructor for Firestore serialization
    }

    public PillReminder(String pillName, int dosage, String frequency, String reminderDate, String reminderTime, String elderlyUser) {
        this.pillName = pillName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.elderlyUser = elderlyUser;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
    }

    public int getDosage() {
        return dosage;
    }

    public void setDosage(int dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getElderlyUser() {
        return elderlyUser;
    }

    public void setElderlyUser(String elderlyUser) {
        this.elderlyUser = elderlyUser;
    }
}
