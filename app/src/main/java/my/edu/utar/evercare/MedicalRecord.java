package my.edu.utar.evercare;

public class MedicalRecord {
    private String elderlyName;
    private String medicineName;
    private String dosage;

    public MedicalRecord(String elderlyName, String medicineName, String dosage) {
        this.elderlyName = elderlyName;
        this.medicineName = medicineName;
        this.dosage = dosage;
    }

    public String getElderlyName() {
        return elderlyName;
    }

    public void setElderlyName(String elderlyName) {
        this.elderlyName = elderlyName;
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


