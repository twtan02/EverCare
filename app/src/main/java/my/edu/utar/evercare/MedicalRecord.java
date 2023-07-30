package my.edu.utar.evercare;

import java.util.List;

public class MedicalRecord {
    private String elderlyId;
    private String elderlyName;
    private String profilePicUrl;
    private List<Medication> medications;
    private ElderlyUser elderlyUser;

    public MedicalRecord() {
        // Empty constructor needed for Firebase
    }

    public MedicalRecord(String elderlyId, String elderlyName, String profilePicUrl, List<Medication> medications) {
        this.elderlyId = elderlyId;
        this.elderlyName = elderlyName;
        this.profilePicUrl = profilePicUrl;
        this.medications = medications;
    }

    public String getElderlyId() {
        return elderlyId;
    }

    public void setElderlyId(String elderlyId) {
        this.elderlyId = elderlyId;
    }

    public String getElderlyName() {
        return elderlyName;
    }

    public void setElderlyName(String elderlyName) {
        this.elderlyName = elderlyName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public ElderlyUser getElderlyUser() {
        return elderlyUser;
    }

    public void setElderlyUser(ElderlyUser elderlyUser) {
        this.elderlyUser = elderlyUser;
    }
}
