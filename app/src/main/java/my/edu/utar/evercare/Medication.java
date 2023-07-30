package my.edu.utar.evercare;

import java.io.Serializable;

public class Medication implements Serializable {
    private String medicineName;
    private String dosage;

    public Medication() {
        // Empty constructor required for Firebase
    }

    public Medication(String medicineName, String dosage) {
        this.medicineName = medicineName;
        this.dosage = dosage;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
